package com.fastbee.iot.data.ruleEngine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.mq.InvokeReqDto;
import com.fastbee.common.core.notify.AlertPushParams;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.SceneThingsModelItem;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.model.AlertSceneSendVO;
import com.fastbee.iot.model.SceneTerminalUserVO;
import com.fastbee.iot.model.ScriptTemplate;
import com.fastbee.iot.model.ThingsModels.ValueItem;
import com.fastbee.iot.service.*;
import com.fastbee.iot.data.service.IFunctionInvoke;
import com.fastbee.iot.data.service.IRuleEngine;
import com.fastbee.notify.core.service.NotifySendService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.compile;

@Data
@Slf4j
public class SceneContext {

    /**
     * 上报信息的设备编号
     */
    private String deviceNum;

    /**
     * 上报信息的设备所属产品ID
     */
    private Long productId;

    /**
     * 上报信息的设备信息类型 1=属性， 2=功能，3=事件，4=设备升级，5=设备上线，6=设备下线
     */
    private int type;

    /**
     * 上报的物模型集合
     */
    private List<ThingsModelSimpleItem> thingsModelSimpleItems;

    /**
     * 触发成功的物模型集合,保留给告警记录
     */
    private List<SceneThingsModelItem> sceneThingsModelItems;


    private static IFunctionInvoke functionInvoke = SpringUtils.getBean(IFunctionInvoke.class);

    private static IDeviceService deviceService = SpringUtils.getBean(IDeviceService.class);

    private static IAlertService alertService = SpringUtils.getBean(IAlertService.class);

    private static NotifySendService notifySendService = SpringUtils.getBean(NotifySendService.class);

    private static RedisCache redisCache = SpringUtils.getBean(RedisCache.class);

    private static IAlertLogService alertLogService = SpringUtils.getBean(IAlertLogService.class);

    private static IDeviceUserService deviceUserService = SpringUtils.getBean(IDeviceUserService.class);

    private static ISceneService sceneService = SpringUtils.getBean(ISceneService.class);

    private static IRuleEngine ruleService = SpringUtils.getBean(IRuleEngine.class);

    private static IDeviceAlertUserService deviceAlertUserService = SpringUtils.getBean(IDeviceAlertUserService.class);

    public SceneContext(String deviceNum, Long productId, int type, List<ThingsModelSimpleItem> thingsModelSimpleItems) {
        this.deviceNum = deviceNum;
        this.productId = productId;
        this.type = type;
        this.thingsModelSimpleItems = thingsModelSimpleItems;
    }

    /**
     * 处理规则脚本
     *
     * @return
     */
    private boolean process(String json) throws InterruptedException {
        log.info("------------------[规则引擎执行...]---------------------");
        ScriptTemplate scriptTemplate = JSON.parseObject(json, ScriptTemplate.class);
        log.info("规则引擎脚本配置：{}，规则引擎上下文：{}", scriptTemplate, this);
        //触发器
        if (scriptTemplate.getPurpose() == 2) {
            // 触发器，检查静默时间
            if (!checkSilent(scriptTemplate.getSilent(), scriptTemplate.getSceneId(), this.deviceNum)) {
                // 触发条件为不满足时，返回true
                if (scriptTemplate.getCond() == 3) {
                    return true;
                }
                return false;
            }
            if (scriptTemplate.getSource() == 1) {
                // 设备触发
                return deviceTrigger(scriptTemplate);
            } else if (scriptTemplate.getSource() == 3) {
                // 产品触发
                return productTrigger(scriptTemplate);
            }
            //执行动作
        } else if (scriptTemplate.getPurpose() == 3) {
            if (scriptTemplate.getCheckdelay() != 0) {
                // 告警恢复规则 设备上线触发 不加载
                if (scriptTemplate.getSource() != 5 && type != 5) {
                    loadDelayTrigger(scriptTemplate);
                }
            } else {
                // 执行动作，延迟执行，线程休眠 delay x 1000毫秒
                Thread.sleep(scriptTemplate.getDelay() * 1000);

                if (scriptTemplate.getSource() == 4) {
                    //存在相同未处理告警只记录 不触发告警通知和动作
                    if (checkDeviceAlerting(scriptTemplate.getSceneId(), this.deviceNum)) {
                        this.alert(scriptTemplate.getSceneId(),false);
                    } else {
                        this.alert(scriptTemplate.getSceneId(),true);
                    }
                } else if (scriptTemplate.getSource() == 5) {
                    this.alertRecover(scriptTemplate);
                } else if (scriptTemplate.getSource() == 1 || scriptTemplate.getSource() == 3) {
                    // 下发指令
                    this.send(scriptTemplate);
                }
                // 更新静默时间
                this.updateSilent(scriptTemplate.getSilent(), scriptTemplate.getSceneId(), this.deviceNum);
            }
        }
        return false;
    }

    /***
     * 设备触发脚本处理
     * @param scriptTemplate 解析后的Json脚本数据
     * @return
     */
    private boolean deviceTrigger(ScriptTemplate scriptTemplate) {
        // 判断定制触发（执行一次）或设备上报
        boolean isDeviceReport = StringUtils.isEmpty(deviceNum) ? false : true;
        if (isDeviceReport) {
            // 1. 匹配设备编号
            boolean matchDeviceNum = Arrays.asList(scriptTemplate.getDeviceNums().split(",")).contains(deviceNum);
            if (scriptTemplate.getType() < 4) {
                // 2.匹配物模型标识符
                ThingsModelSimpleItem matchItem = null;
                if (thingsModelSimpleItems != null) {
                    for (ThingsModelSimpleItem item : thingsModelSimpleItems) {
                        if (item.getId().equals(scriptTemplate.getId())) {
                            matchItem = item;
                            break;
                        }
                    }
                }
                if (matchDeviceNum && matchItem != null) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(scriptTemplate.getId(), matchItem.getValue(), type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), deviceNum);
                    sceneThingsModelItems.add(sceneItem);
                    // 3.设备上报值匹配
                    boolean isMatch = matchValue(scriptTemplate.getOperator(), scriptTemplate.getValue(), matchItem.getValue());
                    if (isMatch) {
                        return true;
                    }
                }

            } else {
                //设备上线清理 离线延时匹配任务
                if (type == 5 && scriptTemplate.getCheckdelay() != 0) {
                    String key = "CHECK_" + scriptTemplate.getSceneId() + deviceNum;
                    //设备上线删除告警任务
                    ruleService.removeCheckTask(key);
                    log.info("设备上线清理 离线延时匹配任务：{}", key);
                }
                // 上线，下线
                if (matchDeviceNum && scriptTemplate.getType() == type) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(type == 5 ? "online" : "offline", type == 5 ? "1" : "0", type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), deviceNum);
                    sceneThingsModelItems.add(sceneItem);
                    // 记录结果
                    return true;
                }
            }
        } else {
            // 定时触发/执行一次
            int resultCount = 0;
            // 3.查询设备最新上报值去匹配
            for (String num : Arrays.asList(scriptTemplate.getDeviceNums().split(","))) {
                // 数组类型，key去除前缀，值从逗号分隔的字符串获取
                String id = "";
                String value = "";
                int index = 0;
                if (scriptTemplate.getId().startsWith("array_")) {
                    id = scriptTemplate.getId().substring(9);
                    index = Integer.parseInt(scriptTemplate.getId().substring(6, 8));
                } else {
                    if (scriptTemplate.getType() == 5 || scriptTemplate.getType() == 6) {
                        return true;
                    }else {
                        id = scriptTemplate.getId();
                    }
                }
                String key = RedisKeyBuilder.buildTSLVCacheKey(scriptTemplate.getProductId(), num);
                String cacheValue = redisCache.getCacheMapValue(key, id);
                if (StringUtils.isEmpty(cacheValue)) {
                    continue;
                }
                ValueItem valueItem = JSON.parseObject(cacheValue, ValueItem.class);
                if (scriptTemplate.getId().startsWith("array_")) {
                    String[] values = valueItem.getValue().split(",");
                    value = values[index];
                } else {
                    value = valueItem.getValue();
                }
                boolean isMatch = matchValue(scriptTemplate.getOperator(), scriptTemplate.getValue(), value);
                if (isMatch) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(scriptTemplate.getId(), value, type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), num);
                    sceneThingsModelItems.add(sceneItem);
                    resultCount++;
                }
            }
            // 任意设备匹配成功返回true
            return resultCount > 0 ? true : false;
        }
        return false;
    }

    /***
     * 产品触发脚本处理
     * @param scriptTemplate
     * @return
     */
    private boolean productTrigger(ScriptTemplate scriptTemplate) {
        // 判断定制触发（执行一次）或设备上报
        boolean isDeviceReport = StringUtils.isEmpty(deviceNum) ? false : true;
        if (isDeviceReport) {
            // 匹配产品编号
            boolean matchProductId = scriptTemplate.getProductId().equals(productId);
            if (scriptTemplate.getType() < 4) {
                // 匹配物模型标识符
                ThingsModelSimpleItem matchItem = null;
                if (thingsModelSimpleItems != null) {
                    for (ThingsModelSimpleItem item : thingsModelSimpleItems) {
                        if (item.getId().equals(scriptTemplate.getId())) {
                            matchItem = item;
                            break;
                        }
                    }
                }
                if (matchProductId && matchItem != null) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(scriptTemplate.getId(), matchItem.getValue(), type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), deviceNum);
                    sceneThingsModelItems.add(sceneItem);
                    // 设备上报值匹配
                    boolean isMatch = matchValue(scriptTemplate.getOperator(), scriptTemplate.getValue(), matchItem.getValue());
                    if (isMatch) {
                        return true;
                    }
                }

            } else {
                //设备上线清理 离线延时匹配任务
                if (type == 5 && scriptTemplate.getCheckdelay() != 0) {
                    String key = "CHECK_" + scriptTemplate.getSceneId() + deviceNum;
                    //设备上线删除告警任务
                    ruleService.removeCheckTask(key);
                    log.info("设备上线清理 离线延时匹配任务：{}", key);
                }
                // 上线，下线
                if (matchProductId && scriptTemplate.getType() == type) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(type == 5 ? "online" : "offline", type == 5 ? "1" : "0", type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), deviceNum);
                    sceneThingsModelItems.add(sceneItem);
                    // 记录结果
                    return true;
                }
            }
        } else {
            // 定时触发/执行一次
            int resultCount = 0;
            // 查询设备最新上报值去匹配
            String[] deviceNums = deviceService.getDeviceNumsByProductId(scriptTemplate.getProductId());
            for (String num : deviceNums) {
                // 数组类型，key去除前缀，值从逗号分隔的字符串获取
                String id = "";
                String value = "";
                int index = 0;
                if (scriptTemplate.getId().startsWith("array_")) {
                    id = scriptTemplate.getId().substring(9);
                    index = Integer.parseInt(scriptTemplate.getId().substring(6, 8));
                } else {
                    if (scriptTemplate.getType() == 5 || scriptTemplate.getType() == 6) {
                        return true;
                    }else {
                        id = scriptTemplate.getId();
                    }
                }
                String key = RedisKeyBuilder.buildTSLVCacheKey(scriptTemplate.getProductId(), num);
                String cacheValue = redisCache.getCacheMapValue(key, id);
                if (StringUtils.isEmpty(cacheValue)) {
                    continue;
                }
                ValueItem valueItem = JSON.parseObject(cacheValue, ValueItem.class);
                if (scriptTemplate.getId().startsWith("array_")) {
                    String[] values = valueItem.getValue().split(",");
                    value = values[index];
                } else {
                    value = valueItem.getValue();
                }
                boolean isMatch = matchValue(scriptTemplate.getOperator(), scriptTemplate.getValue(), value);
                if (isMatch) {
                    // 记录结果
                    if (sceneThingsModelItems == null) {
                        sceneThingsModelItems = new ArrayList<>();
                    }
                    SceneThingsModelItem sceneItem = new SceneThingsModelItem(scriptTemplate.getId(), value, type,
                            scriptTemplate.getScriptId(), scriptTemplate.getSceneId(), scriptTemplate.getProductId(), num);
                    sceneThingsModelItems.add(sceneItem);
                    resultCount++;
                }
            }
            // 任意设备匹配成功返回true
            return resultCount > 0 ? true : false;
        }
        return false;
    }

    private void loadDelayTrigger(ScriptTemplate scriptTemplate) {
        String key = "CHECK_" + scriptTemplate.getSceneId() + deviceNum;
        //默认上线执行告警恢复 在延迟匹配周期内不告警
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            //设备未上线，则执行告警任务
            if (scriptTemplate.getSource() == 4) {
                // 告警
                this.alert(scriptTemplate.getSceneId(),true);
            } else if (scriptTemplate.getSource() == 1 || scriptTemplate.getSource() == 3) {
                // 下发指令
                this.send(scriptTemplate);
            }
        }, scriptTemplate.getCheckdelay(), TimeUnit.SECONDS);
        log.info("延时执行告警任务：{}", key);
        ruleService.addCheckTask(key, executor);
    }

    private boolean checkDeviceAlerting(Long sceneId, String deviceNum) {
        AlertLog alertLog = new AlertLog();
        alertLog.setSerialNumber(deviceNum);
        alertLog.setStatus(2);
        alertLog.setCreateBy(sceneId.toString());
        Long count = alertLogService.selectAlertLogListCount(alertLog);
        // 查询设备告警对应的场景是否有未处理告警
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 执行动作，下发指令
     *
     * @param scriptTemplate
     */
    private void send(ScriptTemplate scriptTemplate) {
        String[] deviceNumbers = null;
        if (scriptTemplate.getSource() == 1) {
            // 下发给指定设备
            deviceNumbers = scriptTemplate.getDeviceNums().split(",");

        } else if (scriptTemplate.getSource() == 3) {
            // 下发给产品下所有设备
            deviceNumbers = deviceService.getDeviceNumsByProductId(scriptTemplate.getProductId());
        }
        for (String deviceNum : deviceNumbers) {
            InvokeReqDto reqDto = new InvokeReqDto();
            reqDto.setProductId(scriptTemplate.getProductId());
            reqDto.setSerialNumber(deviceNum);
            reqDto.setModelName("");
            reqDto.setType(1);
            reqDto.setIdentifier(scriptTemplate.getId());
            Map<String, Object> params = new HashMap<>();
            params.put(scriptTemplate.getId(), scriptTemplate.getValue());
            reqDto.setRemoteCommand(params);
            reqDto.setParams(new JSONObject(reqDto.getRemoteCommand()));
            functionInvoke.invokeNoReply(reqDto);
        }
    }

    private void alertRecover(ScriptTemplate scriptTemplate) {
        AlertLog alertLog = new AlertLog();
        alertLog.setCreateBy(scriptTemplate.getId());
        alertLog.setSerialNumber(deviceNum);
        alertLog.setStatus(3);
        //自动设置告警处理状态
        alertLogService.updateAlertLogStatus(alertLog);
        //如果存在延时确认任务，则删除
        String key = "CHECK_" + scriptTemplate.getId() + deviceNum;
        //设备上线删除告警任务
        ruleService.removeCheckTask(key);
    }

    /**
     * 执行动作，告警处理
     *
     * @param sceneId 场景ID
     */
    private void alert(Long sceneId, boolean isNodify) {
        Set<Long> sceneIdSet = sceneThingsModelItems.stream().map(SceneThingsModelItem::getSceneId).collect(Collectors.toSet());
        List<SceneTerminalUserVO> sceneTerminalUserVOList = sceneService.selectTerminalUserBySceneIds(sceneIdSet);
        Map<Long, SceneTerminalUserVO> sceneTerminalUserMap = sceneTerminalUserVOList.stream().collect(Collectors.toMap(SceneTerminalUserVO::getSceneId, Function.identity()));
        List<AlertLog> alertLogList = new ArrayList<>();
        for (SceneThingsModelItem sceneThingsModelItem : sceneThingsModelItems) {
            // 查询设备信息
            Device device = deviceService.selectDeviceBySerialNumber(sceneThingsModelItem.getDeviceNumber());
            Optional.ofNullable(device).orElseThrow(() -> new ServiceException("告警推送，设备不存在" + "[{" + sceneThingsModelItem.getDeviceNumber() + "}]"));
            // 判断是否是终端用户的场景
            SceneTerminalUserVO sceneTerminalUserVO = sceneTerminalUserMap.get(sceneId);
            if (1 == sceneTerminalUserVO.getTerminalUser()) {
                AlertLog alertLog = this.getTerminalUserAlertLog(sceneTerminalUserVO, device, sceneThingsModelItem);
                alertLogList.add(alertLog);
                continue;
            }

            // 获取场景相关的告警参数，告警必须要是启动状态
            List<AlertSceneSendVO> sceneSendVOList = alertService.listByAlertIds(sceneId);
            if (CollectionUtils.isEmpty(sceneSendVOList)) {
                continue;
            }

            // 获取告警关联模版id
            for (AlertSceneSendVO alertSceneSendVO : sceneSendVOList) {
                // 开启通知发送
                if (isNodify) {
                    AlertPushParams alertPushParams = buildAlertPushParams(device, sceneThingsModelItem);
                    List<AlertNotifyTemplate> alertNotifyTemplateList = alertService.listAlertNotifyTemplate(alertSceneSendVO.getAlertId());
                    alertPushParams.setAlertName(alertSceneSendVO.getAlertName());
                    for (AlertNotifyTemplate alertNotifyTemplate : alertNotifyTemplateList) {
                        alertPushParams.setNotifyTemplateId(alertNotifyTemplate.getNotifyTemplateId());
                        notifySendService.alertSend(alertPushParams);
                    }
                }
                AlertLog alertLog = getAlertLog(alertSceneSendVO, device, sceneThingsModelItem);
                alertLogList.add(alertLog);
            }
        }
        // 保存告警日志
        if (CollectionUtils.isNotEmpty(alertLogList)) {
            alertLogService.insertAlertLogBatch(alertLogList);
        }
    }

    private AlertPushParams buildAlertPushParams(Device device, SceneThingsModelItem sceneThingsModelItem) {
        // 获取告警推送参数
        AlertPushParams alertPushParams = new AlertPushParams();
        alertPushParams.setDeviceName(device.getDeviceName());
        alertPushParams.setSerialNumber(sceneThingsModelItem.getDeviceNumber());
//            List<DeviceUser> deviceUserList = deviceUserService.getDeviceUserAndShare(device.getDeviceId());
        // 多租户改版查询自己配置的告警用户
        DeviceAlertUser deviceAlertUser = new DeviceAlertUser();
        deviceAlertUser.setDeviceId(device.getDeviceId());
        List<DeviceAlertUser> deviceUserList = deviceAlertUserService.selectDeviceAlertUserList(deviceAlertUser);
        if (CollectionUtils.isNotEmpty(deviceUserList)) {
            alertPushParams.setUserPhoneSet(deviceUserList.stream().map(DeviceAlertUser::getPhoneNumber).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
            alertPushParams.setUserIdSet(deviceUserList.stream().map(DeviceAlertUser::getUserId).collect(Collectors.toSet()));
        }
        String address;
        if (StringUtils.isNotEmpty(device.getNetworkAddress())) {
            address = device.getNetworkAddress();
        } else if (StringUtils.isNotEmpty(device.getNetworkIp())) {
            address = device.getNetworkIp();
        } else if (Objects.nonNull(device.getLongitude()) && Objects.nonNull(device.getLatitude())) {
            address = device.getLongitude() + "," + device.getLatitude();
        } else {
            address = "未知地点";
        }
        alertPushParams.setAddress(address);
        alertPushParams.setAlertTime(DateUtils.parseDateToStr(DateUtils.YY_MM_DD_HH_MM_SS, new Date()));
        return alertPushParams;
    }

    private AlertLog getTerminalUserAlertLog(SceneTerminalUserVO sceneTerminalUserVO, Device device, SceneThingsModelItem sceneThingsModelItem) {
        AlertLog alertLog = new AlertLog();
        alertLog.setAlertName("设备告警");
        alertLog.setAlertLevel(1L);
        alertLog.setSerialNumber(sceneThingsModelItem.getDeviceNumber());
        alertLog.setProductId(sceneThingsModelItem.getProductId());
        alertLog.setDeviceName(device.getDeviceName());
        alertLog.setUserId(sceneTerminalUserVO.getUserId());
        alertLog.setCreateBy(sceneThingsModelItem.getSceneId().toString());
        // 统一未处理
        alertLog.setStatus(2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", sceneThingsModelItem.getId());
        jsonObject.put("value", sceneThingsModelItem.getValue());
        jsonObject.put("remark", "");
        alertLog.setDetail(jsonObject.toJSONString());
        alertLog.setCreateTime(new Date());
        alertLog.setCreateBy(sceneThingsModelItem.getSceneId().toString());
        return alertLog;
    }

    /**
     * 组装告警日志
     *
     * @param alertSceneSendVO
     * @return com.fastbee.iot.domain.AlertLog
     * @param: device
     */
    private AlertLog getAlertLog(AlertSceneSendVO alertSceneSendVO, Device device, SceneThingsModelItem sceneThingsModelItem) {
        AlertLog alertLog = new AlertLog();
        alertLog.setAlertName(alertSceneSendVO.getAlertName());
        alertLog.setAlertLevel(alertSceneSendVO.getAlertLevel());
        alertLog.setSerialNumber(sceneThingsModelItem.getDeviceNumber());
        alertLog.setProductId(sceneThingsModelItem.getProductId());
        alertLog.setDeviceName(device.getDeviceName());
        alertLog.setUserId(device.getTenantId());
        alertLog.setCreateBy(sceneThingsModelItem.getSceneId().toString());
        // 统一未处理
        alertLog.setStatus(2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", sceneThingsModelItem.getId());
        jsonObject.put("value", sceneThingsModelItem.getValue());
        jsonObject.put("remark", "");
        alertLog.setDetail(jsonObject.toJSONString());
        alertLog.setCreateTime(new Date());
        alertLog.setCreateBy(sceneThingsModelItem.getSceneId().toString());
        return alertLog;
    }

    /**
     * 检查静默周期物模型值是否匹配
     *
     * @param operator     操作符
     * @param triggerValue 触发值
     * @param value        上报的值
     * @return
     */
    private boolean matchValue(String operator, String triggerValue, String value) {
        boolean result = false;
        // 操作符比较
        switch (operator) {
            case "=":
                result = value.equals(triggerValue);
                break;
            case "!=":
                result = !value.equals(triggerValue);
                break;
            case ">":
                if (isNumeric(value) && isNumeric(triggerValue)) {
                    result = Double.parseDouble(value) > Double.parseDouble(triggerValue);
                }
                break;
            case "<":
                if (isNumeric(value) && isNumeric(triggerValue)) {
                    result = Double.parseDouble(value) < Double.parseDouble(triggerValue);
                }
                break;
            case ">=":
                if (isNumeric(value) && isNumeric(triggerValue)) {
                    result = Double.parseDouble(value) >= Double.parseDouble(triggerValue);
                }
                break;
            case "<=":
                if (isNumeric(value) && isNumeric(triggerValue)) {
                    result = Double.parseDouble(value) <= Double.parseDouble(triggerValue);
                }
                break;
            case "between":
                // 比较值用英文中划线分割 -
                String[] triggerValues = triggerValue.split("-");
                if (isNumeric(value) && isNumeric(triggerValues[0]) && isNumeric(triggerValues[1])) {
                    result = Double.parseDouble(value) >= Double.parseDouble(triggerValues[0]) && Double.parseDouble(value) <= Double.parseDouble(triggerValues[1]);
                }
                break;
            case "notBetween":
                // 比较值用英文中划线分割 -
                String[] trigValues = triggerValue.split("-");
                if (isNumeric(value) && isNumeric(trigValues[0]) && isNumeric(trigValues[1])) {
                    result = Double.parseDouble(value) <= Double.parseDouble(trigValues[0]) || Double.parseDouble(value) >= Double.parseDouble(trigValues[1]);
                }
                break;
            case "contain":
                result = value.contains(triggerValue);
                break;
            case "notContain":
                result = !value.contains(triggerValue);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 检查静默时间
     *
     * @param silent
     * @param sceneId
     * @return
     */
    private boolean checkSilent(int silent, Long sceneId, String serialNumber) {
        if (silent == 0 || sceneId == 0) {
            return true;
        }
        // silent:scene_场景编号
        String key = "silent:" + "scene_" + sceneId + ":" + serialNumber;
        Calendar calendar = Calendar.getInstance();
        // 查询静默截止时间
        Long expireTime = redisCache.getCacheObject(key);
        if (expireTime == null) {
            // 添加场景静默时间
            calendar.add(Calendar.MINUTE, silent);
            redisCache.setCacheObject(key, calendar.getTimeInMillis());
            return true;
        } else {
            Long NowTimestamp = Calendar.getInstance().getTimeInMillis();
            if (NowTimestamp > expireTime) {
                return true;
            }
            return false;
        }
    }

    /**
     * 更新静默时间
     *
     * @param sceneId
     * @param silent
     */
    private void updateSilent(int silent, Long sceneId, String serialNumber) {
        if (silent == 0 || sceneId == 0) {
            return;
        }
        // 更新场景静默时间
        String key = "silent:" + "scene_" + sceneId + ":" + serialNumber;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, silent);
        redisCache.setCacheObject(key, calendar.getTimeInMillis());
    }

    /**
     * 判断字符串是否为整数或小数
     */
    private boolean isNumeric(String str) {
        Pattern pattern = compile("[0-9]*\\.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}
