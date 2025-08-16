package com.fastbee.iot.data.job;

import com.alibaba.fastjson2.JSON;
import com.fastbee.base.service.ISessionStore;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.iot.data.ruleEngine.SceneContext;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.iot.domain.DeviceJob;
import com.fastbee.iot.domain.Scene;
import com.fastbee.iot.mapper.SceneMapper;
import com.fastbee.iot.model.Action;
import com.fastbee.iot.model.modbus.ModbusPollJob;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IModbusJobService;
import com.fastbee.iot.service.IModbusParamsService;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.mqtt.manager.MqttRemoteManager;
import com.fastbee.ruleEngine.core.FlowLogExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行工具
 *
 * @author kerwincui
 */
@Slf4j
public class DeviceJobInvoke {

    /**
     * 获取消息推送接口
     */
    private static IMqttMessagePublish messagePublish = SpringUtils.getBean(IMqttMessagePublish.class);

    private static SceneMapper sceneMapper = SpringUtils.getBean(SceneMapper.class);

    private static FlowLogExecutor flowExecutor = SpringUtils.getBean(FlowLogExecutor.class);

    private static IDataHandler dataHandler = SpringUtils.getBean(IDataHandler.class);

    private static IDeviceService deviceService = SpringUtils.getBean(IDeviceService.class);

    private static IModbusJobService modbusJobService = SpringUtils.getBean(IModbusJobService.class);

    private static IModbusParamsService modbusParamsService = SpringUtils.getBean(IModbusParamsService.class);

    private static ISessionStore sessionStore = SpringUtils.getBean(ISessionStore.class);

    private static MqttRemoteManager mqttRemoteManager = SpringUtils.getBean(MqttRemoteManager.class);

    private static RedisCache redisCache = SpringUtils.getBean(RedisCache.class);

    /**
     * 执行方法
     *
     * @param deviceJob 系统任务
     */
    public static void invokeMethod(DeviceJob deviceJob) throws Exception {
        if (deviceJob.getJobType() == 1) {
            System.out.println("------------------------执行定时任务-----------------------------");
            List<Action> actions = JSON.parseArray(deviceJob.getActions(), Action.class);
            List<ThingsModelSimpleItem> propertys = new ArrayList<>();
            List<ThingsModelSimpleItem> functions = new ArrayList<>();
            for (int i = 0; i < actions.size(); i++) {
                ThingsModelSimpleItem model = new ThingsModelSimpleItem();
                model.setId(actions.get(i).getId());
                model.setValue(actions.get(i).getValue());
                model.setRemark("设备定时");
                if (actions.get(i).getType() == 1) {
                    propertys.add(model);
                } else if (actions.get(i).getType() == 2) {
                    functions.add(model);
                }
            }
            // 发布属性
            if (propertys.size() > 0) {
                messagePublish.publishProperty(deviceJob.getProductId(), deviceJob.getSerialNumber(), propertys, 0);
            }
            // 发布功能
            if (functions.size() > 0) {
                messagePublish.publishFunction(deviceJob.getProductId(), deviceJob.getSerialNumber(), functions, 0);
            }

        } else if (deviceJob.getJobType() == 3) {
            System.out.println("------------------[定时执行场景联动]---------------------");
            Scene scene = sceneMapper.selectSceneBySceneId(deviceJob.getSceneId());
            // 执行场景规则,异步非阻塞
            SceneContext context = new SceneContext("", 0L,0,null);
            flowExecutor.execute2Future(String.valueOf(scene.getChainName()), null, context);
        } else if (4 == deviceJob.getJobType()) {
            System.out.println("------------------[定时执行场景运算型变量]---------------------");
            String s = dataHandler.calculateSceneModelTagValue(deviceJob.getDatasourceId());
            if (StringUtils.isEmpty(s)) {
                System.out.println("------------------[定时执行场景运算型变量失败：+" + s + "]---------------------");
            }
        }else if (5 == deviceJob.getJobType()){
            log.info("----------------执行modbus轮训指令----------------------");
            ModbusPollJob job = new ModbusPollJob();
            job.setType(2);
            job.setDeviceJob(deviceJob);
            MessageProducer.sendPropFetch(job);
        }
    }



}
