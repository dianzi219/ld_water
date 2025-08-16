package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.mq.InvokeReqDto;
import com.fastbee.common.enums.DeviceLogTypeEnum;
import com.fastbee.common.enums.FunctionReplyStatus;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.enums.scenemodel.SceneModelTagOpreationEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.CaculateVariableAndNumberUtils;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.date.LocalDateTimeUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.cache.SceneModelTagCache;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.iot.data.service.IRuleEngine;
import com.fastbee.iot.domain.*;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.iot.mapper.SceneTagPointsMapper;
import com.fastbee.iot.model.scenemodel.SceneModelTagCacheVO;
import com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO;
import com.fastbee.iot.service.*;
import com.fastbee.iot.tdengine.service.ILogService;
import com.fastbee.common.core.mq.message.ReportDataBo;
import com.fastbee.mqtt.manager.MqttRemoteManager;
import com.fastbee.mqtt.model.PushMessageBo;
import com.fastbee.mqttclient.PubMqttClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 上报数据处理方法集合
 * @author bill
 */
@Service
@Slf4j
public class DataHandlerImpl implements IDataHandler {


    @Resource
    private IDeviceService deviceService;
    @Resource
    private IEventLogService eventLogService;
    @Resource
    private IMqttMessagePublish messagePublish;
    @Resource
    private IRuleEngine ruleEngine;
    @Resource
    private MqttRemoteManager remoteManager;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private PubMqttClient mqttClient;

    @Resource
    private SpeakerService speakerService;

    @Resource
    private SceneTagPointsMapper sceneTagPointsMapper;

    @Resource
    private SceneModelTagCache sceneModelTagCache;

    @Resource
    private ILogService logService;

    @Resource
    private ISceneModelTagService sceneModelTagService;

    @Resource
    private IFunctionLogService functionLogService;

    /**
     * 上报属性或功能处理
     *
     * @param bo 上报数据模型
     */
    @Override
    public void reportData(ReportDataBo bo) {

        try {
            List<ThingsModelSimpleItem> thingsModelSimpleItems = bo.getDataList();
            if (CollectionUtils.isEmpty(bo.getDataList()) || bo.getDataList().size() == 0) {
                thingsModelSimpleItems = JSON.parseArray(bo.getMessage(), ThingsModelSimpleItem.class);
            }
            if (CollectionUtils.isEmpty(thingsModelSimpleItems)) return;
            ThingsModelValuesInput input = new ThingsModelValuesInput();
            input.setProductId(bo.getProductId());
            // 这里上报设备编号是转的大写，后面存缓存也是使用大写的，所以在查询物模型的值时添加把设备编号转大写后取值
            input.setDeviceNumber(bo.getSerialNumber().toUpperCase());
            input.setThingsModelValueRemarkItem(thingsModelSimpleItems);
            List<ThingsModelSimpleItem> result = deviceService.reportDeviceThingsModelValue(input, bo.getType(), bo.isShadow());
            // 只有设备上报进入规则引擎流程
            if (bo.isRuleEngine() && !bo.getSerialNumber().startsWith("server-")){
                ruleEngine.ruleMatch(bo);
            }
            //发送至前端
            PushMessageBo messageBo = new PushMessageBo();
            messageBo.setTopic(topicsUtils.buildTopic(bo.getProductId(), bo.getSerialNumber(), TopicType.WS_SERVICE_INVOKE));
            JSONObject pushObj = new JSONObject();
            pushObj.put("message", result);
            pushObj.put("sources",bo.getSources());
            messageBo.setMessage(JSON.toJSONString(pushObj));
            remoteManager.pushCommon(messageBo);
            if (!Objects.isNull(bo.getGwDeviceBo())){
                messageBo.setTopic(topicsUtils.buildTopic(bo.getGwDeviceBo().getProductId(),bo.getGwDeviceBo().getSerialNumber(),TopicType.WS_SERVICE_INVOKE));
                remoteManager.pushCommon(messageBo);
            }
            // 上报属性给小度音箱，接入小度音箱后可放开
//            try {
//                List<String> identifierList = thingsModelSimpleItems.stream().map(ThingsModelSimpleItem::getId).collect(Collectors.toList());
//                speakerService.reportDuerosAttribute(bo.getSerialNumber(), identifierList);
//            } catch (Exception e) {
//                log.error("=>上报属性信息给小度音箱异常", e);
//            }
        } catch (Exception e) {
            log.error("接收属性数据，解析数据时异常 message={},e={}", e.getMessage(),e);
        }
    }


    /**
     * 上报事件
     *
     * @param bo 上报数据模型
     */
    @Override
    public void reportEvent(ReportDataBo bo) {
        try {
            List<ThingsModelSimpleItem> thingsModelSimpleItems = JSON.parseArray(bo.getMessage(), ThingsModelSimpleItem.class);
            Device device = deviceService.selectDeviceBySerialNumber(bo.getSerialNumber());
            List<EventLog> results = new ArrayList<>();
            for (int i = 0; i < thingsModelSimpleItems.size(); i++) {
                // 添加到设备日志
                EventLog event = new EventLog();
                event.setDeviceId(device.getDeviceId());
                event.setDeviceName(device.getDeviceName());
                event.setLogValue(thingsModelSimpleItems.get(i).getValue());
                event.setRemark(thingsModelSimpleItems.get(i).getRemark());
                event.setSerialNumber(device.getSerialNumber());
                event.setIdentity(thingsModelSimpleItems.get(i).getId());
                event.setLogType(3);
                event.setIsMonitor(0);
                event.setUserId(device.getTenantId());
                event.setUserName(device.getTenantName());
                event.setTenantId(device.getTenantId());
                event.setTenantName(device.getTenantName());
                event.setCreateTime(DateUtils.getNowDate());
                // 1=影子模式，2=在线模式，3=其他
                event.setMode(2);
                results.add(event);
                //eventLogService.insertEventLog(event);
            }
            eventLogService.saveBatch(results);
            if (bo.isRuleEngine()){
                ruleEngine.ruleMatch(bo);
            }
        } catch (Exception e) {
            log.error("接收事件，解析数据时异常 message={}", e.getMessage());
        }
    }

    /**
     * 上报设备信息
     */
    public void reportDevice(ReportDataBo bo) {
        try {
            // 设备实体
            Device deviceEntity = deviceService.selectDeviceBySerialNumber(bo.getSerialNumber());
            // 上报设备信息
            Device device = JSON.parseObject(bo.getMessage(), Device.class);
            device.setProductId(bo.getProductId());
            device.setSerialNumber(bo.getSerialNumber());
            deviceService.reportDevice(device, deviceEntity);
            // 发布设备状态
            messagePublish.publishStatus(bo.getProductId(), bo.getSerialNumber(), 3, deviceEntity.getIsShadow(), device.getRssi());
        } catch (Exception e) {
            log.error("接收设备信息，解析数据时异常 message={}", e.getMessage());
            throw new ServiceException(e.getMessage(), 1);
        }
    }

    @Override
    public String calculateSceneModelTagValue(Long id) {
        LocalDateTime now = LocalDateTime.now();
        SceneModelTag sceneModelTag = sceneModelTagService.selectSceneModelTagById(id);
        if (null == sceneModelTag) {
            return "场景运算型变量计算错误：变量为空";
        }
        if ((StringUtils.isEmpty(sceneModelTag.getAliasFormule()) && org.apache.commons.collections4.CollectionUtils.isEmpty(sceneModelTag.getTagPointsList()))) {
            return "场景运算型变量计算错误：没有计算公式";
        }
        String checkMsg = sceneModelTagService.checkAliasFormule(sceneModelTag);
        if (StringUtils.isNotEmpty(checkMsg)) {
            return "场景运算型变量计算错误：" + checkMsg;
        }
        List<SceneTagPoints> sceneTagPointsList = sceneTagPointsMapper.selectListByTagId(sceneModelTag.getId());
        Map<String, String> replaceMap = new HashMap<>(2);
        // 计算周期
        SceneModelTagCycleVO sceneModelTagCycleVO = new SceneModelTagCycleVO();
        boolean b = sceneTagPointsList.stream().anyMatch(s -> !SceneModelTagOpreationEnum.ORIGINAL_VALUE.getCode().equals(s.getOperation()));
        if (b) {
            sceneModelTagCycleVO = sceneModelTagService.handleTimeCycle(sceneModelTag.getCycleType(), sceneModelTag.getCycle(), now);
        }
        // 需不需要判断每个变量启用了没有 todo
        for (SceneTagPoints sceneTagPoints : sceneTagPointsList) {
            String value;
            value = sceneModelTagService.getSceneModelDataValue(sceneTagPoints, sceneModelTagCycleVO);
            // value没值先兜底0
            if (StringUtils.isEmpty(value)) {
                value = "0";
            }
            replaceMap.put(sceneTagPoints.getAlias(), value);
        }
        BigDecimal execute = CaculateVariableAndNumberUtils.execute(sceneModelTag.getAliasFormule(), replaceMap);
        String resultValue = execute.toPlainString();
        this.saveSceneModelTagValue(sceneModelTag, resultValue, now);
        return resultValue;
    }

    @Override
    public void invokeSceneModelTagValue(InvokeReqDto reqDto, String messageId) {
        LocalDateTime now = LocalDateTime.now();
        String sceneModelTagId = reqDto.getIdentifier();
        Map<String, Object> remoteCommand = reqDto.getRemoteCommand();
        String value = remoteCommand.get(sceneModelTagId).toString();
        FunctionLog functionLog = new FunctionLog();
        functionLog.setIdentify(reqDto.getIdentifier());
        functionLog.setFunType(4);
        functionLog.setFunValue(value);
        functionLog.setMessageId(messageId);
        functionLog.setSerialNumber(reqDto.getSceneModelId().toString());
        functionLog.setMode(3);
        functionLog.setModelName(reqDto.getModelName());
        SceneModelTag sceneModelTag = sceneModelTagService.selectSceneModelTagById(Long.valueOf(sceneModelTagId));
        if (null == sceneModelTag) {
            functionLog.setResultCode(FunctionReplyStatus.FAIl.getCode());
            functionLog.setResultMsg(FunctionReplyStatus.FAIl.getMessage());
            functionLogService.insertFunctionLog(functionLog);
            return;
        }
        this.saveSceneModelTagValue(sceneModelTag, value, now);
        functionLog.setResultCode(FunctionReplyStatus.SUCCESS.getCode());
        functionLog.setResultMsg(FunctionReplyStatus.SUCCESS.getMessage());
        functionLogService.insertFunctionLog(functionLog);
    }

    /**
     * 保存场景变量值
     * @param sceneModelTag 变量类
     * @param: value 值
     * @param: now 执行时间
     * @return void
     */
    private void saveSceneModelTagValue(SceneModelTag sceneModelTag, String value, LocalDateTime now) {
        // 保存运算型变量值,存缓存
        SceneModelTagCacheVO sceneModelTagCacheVO = new SceneModelTagCacheVO();
        sceneModelTagCacheVO.setId(sceneModelTag.getId().toString());
        sceneModelTagCacheVO.setTs(LocalDateTimeUtils.localDateTimeToStr(now, LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS));
        sceneModelTagCacheVO.setValue(value);
        sceneModelTagCache.addSceneModelTagValue(sceneModelTag.getSceneModelId(), sceneModelTagCacheVO);
        // 是否历史存储
        if (1 == sceneModelTag.getStorage()) {
            DeviceLog deviceLog = new DeviceLog();
            deviceLog.setIdentity(sceneModelTag.getId().toString());
            deviceLog.setModelName(sceneModelTag.getName());
            deviceLog.setLogType(DeviceLogTypeEnum.SCENE_VARIABLE_REPORT.getType());
            deviceLog.setLogValue(value);
            deviceLog.setIsMonitor(0);
            deviceLog.setMode(3);
            deviceLog.setCreateTime(new Date());
            logService.saveDeviceLog(deviceLog);
        }
        //发送至前端
        List<SceneModelTagCacheVO> sendMsg = new ArrayList<>();
        sendMsg.add(sceneModelTagCacheVO);
        PushMessageBo messageBo = new PushMessageBo();
        // /场景id/变量id/scene/report（对应变量标识）
        messageBo.setTopic(TopicsUtils.buildSceneReportTopic(sceneModelTag.getSceneModelId(), sceneModelTag.getSceneModelDeviceId()));
        messageBo.setMessage(JSON.toJSONString(sendMsg));
        remoteManager.pushCommon(messageBo);
    }


}
