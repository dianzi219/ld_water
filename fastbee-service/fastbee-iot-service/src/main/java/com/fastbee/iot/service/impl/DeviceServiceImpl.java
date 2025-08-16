package com.fastbee.iot.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fastbee.common.constant.Constants;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.constant.ProductAuthConstant;
import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysDept;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.enums.*;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.CaculateUtils;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.http.HttpUtils;
import com.fastbee.common.utils.ip.IpUtils;
import com.fastbee.common.utils.json.JsonUtils;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.enums.DeviceType;
import com.fastbee.iot.mapper.*;
import com.fastbee.iot.model.*;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fastbee.iot.model.ThingsModelItem.EnumItem;
import com.fastbee.iot.model.ThingsModelItem.ThingsModel;
import com.fastbee.iot.model.ThingsModels.*;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;
import com.fastbee.iot.model.gateWay.SubDeviceListVO;
import com.fastbee.iot.service.*;
import com.fastbee.iot.cache.IDeviceCache;
import com.fastbee.iot.cache.ITSLValueCache;
import com.fastbee.iot.tdengine.service.ILogService;
import com.fastbee.system.mapper.SysDeptMapper;
import com.fastbee.system.service.ISysUserService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;
import static com.fastbee.common.utils.SecurityUtils.isAdmin;

/**
 * 设备Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
public class DeviceServiceImpl implements IDeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);
    @Value("${server.broker.port}")
    private Long brokerPort;

    @Autowired
    private ITSLCache itslCache;
    @Autowired
    private IToolService toolService;
    @Resource
    private IProductService productService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ILogService logService;
    @Autowired
    private IAlertLogService alertLogService;
    @Autowired
    private RedisCache redisCache;
    @Resource
    @Lazy
    private IDeviceCache deviceCache;
    @Resource
    private IOrderControlService orderControlService;
    @Resource
    private IFunctionLogService functionLogService;
    @Resource
    private IEventLogService eventLogService;
    @Resource
    private IProductAuthorizeService productAuthorizeService;
    @Resource
    private IDeviceShareService deviceShareService;
    @Resource
    private ISceneService sceneService;
    @Resource
    private ISipRelationService sipRelationService;
    @Resource
    private IThingsModelService thingsModelService;

    @Autowired
    private EventLogMapper eventLogMapper;
    @Resource
    private SceneDeviceMapper sceneDeviceMapper;
    @Resource
    private ITSLValueCache itslValueCache;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private DeviceRecordMapper deviceRecordMapper;
    @Resource
    private SubGatewayMapper subGatewayMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private DeviceUserMapper deviceUserMapper;
    @Resource
    private ProductModbusJobMapper productModbusJobMapper;
    @Resource
    private IModbusJobService modbusJobService;
    @Resource
    private ProductSubGatewayMapper productSubGatewayMapper;
    @Resource
    private ModbusParamsMapper modbusParamsMapper;
    @Value("${server.tcp.port}")
    private Long tcpPort;


    // select cache

    /**
     * 查询设备
     *
     * @param deviceId 设备主键
     * @return 设备
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #deviceId", unless = "#result == null")
    @Override
    public Device selectDeviceByDeviceId(Long deviceId) {
        Device device = deviceMapper.selectDeviceByDeviceId(deviceId);
        List<ValueItem> list = itslValueCache.getCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
        if (list != null && list.size() > 0) {
            // redis中获取设备状态（物模型值）
            device.setThingsModelValue(JSONObject.toJSONString(list));
        }
        //查询关联的监控设备
        SipRelation sipRelation = new SipRelation();
        sipRelation.setReDeviceId(deviceId);
        List<SipRelation> sipRelationList = sipRelationService.selectSipRelationList(sipRelation);
        device.setSipRelationList(sipRelationList);
        // 没图片用产品图片
        if (StringUtils.isEmpty(device.getImgUrl())) {
            device.setImgUrl(productService.selectImgUrlByProductId(device.getProductId()));
        }
        if (DeviceType.SUB_GATEWAY.getCode() == device.getDeviceType()) {
             Integer slaveId = subGatewayMapper.selectSlaveIdBySubDeviceId(device.getDeviceId());
            if (null != slaveId) {
                device.setSlaveId(slaveId);
            }
        }
        if (FastBeeConstant.PROTOCOL.ModbusRtu.equals(device.getProtocolCode())
                || FastBeeConstant.PROTOCOL.ModbusTcp.equals(device.getProtocolCode())) {
            LambdaQueryWrapper<ModbusParams> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ModbusParams::getProductId, device.getProductId());
            ModbusParams modbusParams = modbusParamsMapper.selectOne(queryWrapper);
            if (null != modbusParams) {
                if (DeviceType.DIRECT_DEVICE.getCode() == device.getDeviceType()) {
                    device.setSlaveId(modbusParams.getSlaveId());
                }
                device.setCanConfigPoll(0 == modbusParams.getPollType());
            }
        }
        return device;
    }

    /**
     * 根据设备编号查询设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #serialNumber", unless = "#result == null")
    @Override
    public Device selectDeviceBySerialNumber(String serialNumber) {
        return deviceMapper.selectDeviceBySerialNumber(serialNumber);
    }

    /**
     * 根据设备编号查询简洁设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #serialNumber", unless = "#result == null")
    @Override
    public Device selectShortDeviceBySerialNumber(String serialNumber) {
        Device device = deviceMapper.selectShortDeviceBySerialNumber(serialNumber);
        if (!Objects.isNull(device)) {
            // redis中获取设备状态（物模型值）
            List<ValueItem> list = itslValueCache.getCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
            if (list != null && list.size() > 0) {
                device.setThingsModelValue(JSONObject.toJSONString(list));
            }
        }
        return device;
    }

    /**
     * 查询设备状态以及传输协议
     *
     * @param serialNumber
     * @return
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #serialNumber", unless = "#result == null")
    @Override
    public DeviceStatusVO selectDeviceStatusAndTransportStatus(String serialNumber) {
        return deviceMapper.selectDeviceStatusAndTransportStatus(serialNumber);
    }

    /**
     * 根据设备编号查询协议编码
     *
     * @param serialNumber
     * @return
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #serialNumber", unless = "#result == null")
    @Override
    public DeviceAndProtocol selectProtocolBySerialNumber(String serialNumber) {
        return deviceMapper.selectProtocolBySerialNumber(serialNumber);
    }

    /**
     * 根据设备编号查询协议编号
     *
     * @param serialNumber 设备编号
     * @return 协议编号
     */
    @Cacheable(cacheNames = "Device", key = "#root.methodName + ':' + #serialNumber", unless = "#result == null")
    @Override
    public ProductCode getProtocolBySerialNumber(String serialNumber) {
        return deviceMapper.getProtocolBySerialNumber(serialNumber);
    }

    /**
     * 修改设备
     *
     * @param device 设备
     * @return 结果
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "Device", key = "'selectDeviceBySerialNumber:' + #device.getSerialNumber()"),
            @CacheEvict(cacheNames = "Device", key = "'selectDeviceByDeviceId:' + #device.getDeviceId()"),
            @CacheEvict(cacheNames = "Device", key = "'selectDeviceStatusAndTransportStatus:' + #device.getSerialNumber()"),
            @CacheEvict(cacheNames = "Device", key = "'selectProtocolBySerialNumber:' + #device.getSerialNumber()"),
            @CacheEvict(cacheNames = "Device", key = "'selectShortDeviceBySerialNumber:' + #device.getSerialNumber()")
    })
    @Override
    public AjaxResult updateDevice(Device device) {
        // 设备编号唯一检查
        Device oldDevice = deviceMapper.selectDeviceByDeviceId(device.getDeviceId());
        if (!oldDevice.getSerialNumber().equals(device.getSerialNumber())) {
            Device existDevice = deviceMapper.selectDeviceBySerialNumber(device.getSerialNumber());
            if (existDevice != null) {
                log.error("设备编号：" + device.getSerialNumber() + " 已经存在，新增设备失败");
                return AjaxResult.error("设备编号：" + device.getSerialNumber() + " 已经存在，修改失败", 0);
            }
        }
        device.setUpdateTime(DateUtils.getNowDate());
        // 未激活状态,可以修改产品以及物模型值
        if (device.getStatus() == 1) {
            // 缓存设备状态（物模型值）
            itslValueCache.addCacheDeviceStatus(device.getProductId(), device.getSerialNumber());

        } else {
            device.setProductId(null);
            device.setProductName(null);
        }
        deviceMapper.updateDevice(device);
        return AjaxResult.success("修改成功", 1);
    }

    /**
     * 删除设备
     *
     * @param deviceId 需要删除的设备主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "Device", allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteDeviceByDeviceId(Long deviceId) {
        //查询设备
        Device device = deviceMapper.selectDeviceByDeviceId(deviceId);
        SysUser user = getLoginUser().getUser();
        Long sceneUserId;
        if (null == user.getDeptId()) {
            sceneUserId = user.getUserId();
        } else {
            sceneUserId = user.getDept().getDeptUserId();
        }
        // 2.3版本更改 设备管理者和设备拥有者为true，普通用户如果不是设备所有者，只能删除设备用户和用户自己的设备关联分组信息
        boolean isGeneralUser;
        if (isAdmin(user.getUserId())) {
            isGeneralUser = false;
        } else {
            // 终端用户和非设备所属机构用户不允许删除设备
            if (null == user.getDeptId()) {
                isGeneralUser = true;
            } else if (!Objects.equals(device.getTenantId(), user.getDept().getDeptUserId())) {
                isGeneralUser = true;
            } else {
                isGeneralUser = false;
            }
        }
        // 设备下不能有场景联动
        List<SceneDeviceBindVO> sceneDeviceBindVOList = sceneDeviceMapper.listSceneDeviceBind(device.getSerialNumber());
        if (null != sceneDeviceBindVOList.stream().filter(s -> s.getUserId().equals(sceneUserId)).findAny().orElse(null)) {
            return AjaxResult.error("删除失败，请先删除对应设备下的场景联动");
        }
        // 查绑定用户被分享用户配置的场景，需要把场景删掉
        DeviceUser deviceUser = deviceUserMapper.selectDeviceUserByDeviceId(deviceId);
        List<Long> userIdSceneList = new ArrayList<>();
        if (null != deviceUser) {
            userIdSceneList.add(deviceUser.getUserId());
        }
        Long[] shareSceneIds = null;
        List<DeviceShare> deviceShares = deviceShareService.selectDeviceShareByDeviceId(deviceId);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(deviceShares)) {
            List<Long> userIdList = deviceShares.stream().map(DeviceShare::getUserId).collect(Collectors.toList());
            userIdSceneList.addAll(userIdList);
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userIdSceneList)) {
            shareSceneIds = sceneDeviceMapper.listSceneIdByDeviceIdAndUserId(device.getSerialNumber(), userIdSceneList);
        }
        if (isGeneralUser) {
            if (null != deviceUser && deviceUser.getUserId().equals(user.getUserId())) {
                if (null != shareSceneIds && shareSceneIds.length > 0) {
                    sceneService.deleteSceneBySceneIds(shareSceneIds);
                }
                // 删除用户的设备用户信息。
                deviceUserMapper.deleteDeviceUserByDeviceId(new UserIdDeviceIdModel(user.getUserId(), deviceId));
                deviceShareService.deleteDeviceShareByDeviceId(deviceId);
            } else {
                DeviceShare deviceShare = new DeviceShare();
                deviceShare.setDeviceId(deviceId).setUserId(user.getUserId());
                deviceShareService.deleteDeviceShareByDeviceIdAndUserId(deviceShare);
            }
            // 删除用户分组中的设备 普通用户，且不是设备所有者。
            deviceMapper.deleteDeviceGroupByDeviceId(new UserIdDeviceIdModel(user.getUserId(), deviceId));
        } else {
            if (null != shareSceneIds && shareSceneIds.length > 0) {
                sceneService.deleteSceneBySceneIds(shareSceneIds);
            }
            // 删除设备分组。  租户、管理员和设备所有者
            deviceMapper.deleteDeviceGroupByDeviceId(new UserIdDeviceIdModel(null, deviceId));
            // 删除设备用户。
            deviceUserMapper.deleteDeviceUserByDeviceId(new UserIdDeviceIdModel(null, deviceId));
            deviceShareService.deleteDeviceShareByDeviceId(deviceId);
            // 删除定时任务 TODO - emq兼容
            // deviceJobService.deleteJobByDeviceIds(new Long[]{deviceId});
            // 批量删除设备监测日志
            logService.deleteDeviceLogByDeviceNumber(device.getSerialNumber());
            // 批量删除设备事件日志  DeviceNumber
            eventLogService.deleteEventLogBySerialNumber(device.getSerialNumber());
            // 批量删除设备功能日志
            functionLogService.deleteFunctionLogByDeviceNumber(device.getSerialNumber());
            // 批量删除设备告警记录
            alertLogService.deleteAlertLogBySerialNumber(device.getSerialNumber());
            // redis中删除设备物模型（状态）
            String key = RedisKeyBuilder.buildTSLVCacheKey(device.getProductId(), device.getSerialNumber());
            redisCache.deleteObject(key);
            // 删除设备
            deviceMapper.deleteDeviceByDeviceIds(new Long[]{deviceId});
            // redis中删除规则脚本
            String cacheKey = RedisKeyBuilder.buildDeviceMsgCacheKey(device.getSerialNumber());
            redisCache.deleteObject(cacheKey);
            // 删除设备
            deviceMapper.deleteDeviceByDeviceIds(new Long[]{deviceId});
            // 删除tcp协议设备缓存
            String tcpKey1 = RedisKeyBuilder.buildModbusTcpCacheKey(device.getSerialNumber());
            String tcpKey2 = RedisKeyBuilder.buildModbusTcpRuntimeCacheKey(device.getSerialNumber());
            redisCache.deleteStrObject(tcpKey1);
            redisCache.deleteStrHash(tcpKey2);
            // 删除轮询任务
            ModbusJob modbusJob = new ModbusJob();
            modbusJob.setSubDeviceId(deviceId);
            List<ModbusJob> modbusJobList = modbusJobService.selectModbusJobList(modbusJob);
            for (ModbusJob job : modbusJobList) {
                try {
                    modbusJobService.deleteModbusJobByTaskId(job);
                } catch (SchedulerException e) {
                    log.error("删除设备时删除设备轮询任务出错，{}，{}", e, job);
                }
            }
            // 删除网关绑定子设备关系
            if (DeviceType.GATEWAY.getCode() == device.getDeviceType()) {
                subGatewayMapper.deleteGatewayByGwDeviceId(deviceId);
            }
            // 删除modbus轮询指令
            String keyModbus = RedisKeyBuilder.buildModbusRuntimeCacheKey(device.getSerialNumber());
            redisCache.deleteObject(keyModbus);
        }
        return AjaxResult.success();
    }


    /**
     * TODO-2.3 查询设备统计信息
     *
     * @return 设备
     */
    @Override
    public DeviceStatistic selectDeviceStatistic() {
        Long deptId = SecurityUtils.getDeptId();
        // 获取设备、产品和告警数量
        DeviceStatistic statistic = deviceMapper.selectDeviceProductAlertCount(deptId);
        if (statistic == null) {
            statistic = new DeviceStatistic();
            return statistic;
        }
        Device device = new Device();
        device.setTenantId(SecurityUtils.getUserId());
        // 获取属性、功能和事件
        DeviceStatistic thingsCount = logService.selectCategoryLogCount(device);
        if (thingsCount == null) {
            return statistic;
        }
        // 组合属性、功能、事件和监测数据
        statistic.setPropertyCount(thingsCount.getPropertyCount());
        statistic.setFunctionCount(thingsCount.getFunctionCount());
        statistic.setEventCount(thingsCount.getEventCount());
        statistic.setMonitorCount(thingsCount.getMonitorCount());
        return statistic;
    }

    /**
     * 根据设备编号查询设备认证信息
     *
     * @param model 设备编号和产品ID
     * @return 设备
     */
    @Override
    public ProductAuthenticateModel selectProductAuthenticate(AuthenticateInputModel model) {
        return deviceMapper.selectProductAuthenticate(model);
    }


    /**
     * 更新设备的物模型
     *
     * @param input    设备ID和物模型值
     * @param type     1=属性 2=功能 3=事件
     * @param isShadow 是否影子值
     * @return 设备
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ThingsModelSimpleItem> reportDeviceThingsModelValue(ThingsModelValuesInput input, int type, boolean isShadow) {
        String key = RedisKeyBuilder.buildTSLVCacheKey(input.getProductId(), input.getDeviceNumber());
        Map<String, String> maps = new HashMap<String, String>();
        List<ThingsModelSimpleItem> list = new ArrayList<>();

        //属性存储集合
        List<DeviceLog> deviceLogList = new ArrayList<>();
        //指令存储集合
        List<FunctionLog> functionLogList = new ArrayList<>();
        //事件存储集合
        List<EventLog> eventLogList = new ArrayList<>();
        for (ThingsModelSimpleItem item : input.getThingsModelValueRemarkItem()) {
            String identity = item.getId();
            String serialNumber = input.getDeviceNumber();
            if (identity.startsWith("array_")) {
                identity = identity.substring(9);
            }

            ThingsModelValueItem thingModels = thingsModelService.getSingleThingModels(input.getProductId(), identity);
            if (Objects.isNull(thingModels)) {
                log.warn("=>查询物模型为空[{}],标识符:[{}]", input.getProductId(), identity);
            }else{
                // 获取设备实时值
                ValueItem cacheIdentifier = itslValueCache.getCacheIdentifier(input.getProductId(), input.getDeviceNumber(), thingModels.getId());
                if (Objects.isNull(cacheIdentifier)) {
                    log.warn("=>查询物模型值为空[{}],标识符:[{}]", input.getProductId(), identity);
                }
                String id = item.getId();
                String value = item.getValue();
                item.setName(thingModels.getName());

                /* ★★★★★★★★★★★★★★★★★★★★★★  数据计算 -开始 ★★★★★★★★★★★★★★★★★★★★★★*/
                //有计算公式的，经过计算公式
                if (thingModels.getFormula() != null && !"".equals(thingModels.getFormula())) {
                    Map<String, String> params = new HashMap<>();
                    params.put("%s", value);
                    value = String.valueOf(CaculateUtils.execute(thingModels.getFormula(), params));
                    item.setValue(value);
                }
                /* ★★★★★★★★★★★★★★★★★★★★★★  数据计算 -结束  ★★★★★★★★★★★★★★★★★★★★★★*/

                /* ★★★★★★★★★★★★★★★★★★★★★★  处理数据 - 开始 ★★★★★★★★★★★★★★★★★★★★★★*/
                ValueItem valueItem = new ValueItem();
                valueItem.setId(identity);
                if (id.startsWith("array_")) {
                    // 查询是否有缓存，如果没有先进行缓存
                    if (!redisCache.containsKey(key)) {
                        Device device = this.selectDeviceBySerialNumber(input.getDeviceNumber());
                        this.selectDeviceByDeviceId(device.getDeviceId());
                    }

                    int index = Integer.parseInt(id.substring(6, 8));
                    if (isShadow) {
                        String[] shadows = cacheIdentifier.getShadow().split(",");
                        shadows[index] = value;
                        valueItem.setShadow(String.join(",", shadows));
                        // 影子模式也缓存value值，不然设备上线后处理会报null
                        if (null != cacheIdentifier.getValue()) {
                            valueItem.setValue(String.join(",", cacheIdentifier.getValue()));
                        }
                    } else {
                        // 设置值，获取数组值，然后替换其中元素
                        valueItem.setTs(DateUtils.getNowDate());
                        String[] values = cacheIdentifier.getValue().split(",");
                        values[index] = value;
                        valueItem.setValue(String.join(",", values));

                        String[] shadows = cacheIdentifier.getShadow().split(",");
                        shadows[index] = value;
                        valueItem.setShadow(String.join(",", shadows));
                    }
                    redisCache.setCacheMapValue(key, identity, JSONObject.toJSONString(valueItem));
                    //maps.put(identity, JSONObject.toJSONString(valueItem));
                } else {
                    if (isShadow) {
                        valueItem.setShadow(value);
                        // 影子模式也缓存value值，不然设备上线后处理会报null
                        if (null != cacheIdentifier.getValue()) {
                            valueItem.setValue(cacheIdentifier.getValue());
                        }
                    } else {
                        valueItem.setValue(value);
                        valueItem.setShadow(value);
                        valueItem.setTs(DateUtils.getNowDate());
                    }
                    maps.put(identity, JSONObject.toJSONString(valueItem));
                }
                /* ★★★★★★★★★★★★★★★★★★★★★★  处理数据 - 结束 ★★★★★★★★★★★★★★★★★★★★★★*/

                /*★★★★★★★★★★★★★★★★★★★★★★  存储数据 - 开始 ★★★★★★★★★★★★★★★★★★★★★★*/
                ThingsModelType modelType = ThingsModelType.getType(thingModels.getType());
                Device device = this.selectDeviceBySerialNumber(input.getDeviceNumber());
                switch (modelType) {
                    case PROP:
                        if (1 == thingModels.getIsHistory()) {
                            DeviceLog deviceLog = new DeviceLog();
                            deviceLog.setSerialNumber(serialNumber);
                            deviceLog.setLogType(type);
                            // 1=影子模式，2=在线模式，3=其他
                            deviceLog.setMode(isShadow ? 1 : 2);
                            // 设备日志值
                            deviceLog.setLogValue(value);
                            deviceLog.setRemark(item.getRemark());
                            deviceLog.setIdentity(id);
                            deviceLog.setCreateTime(DateUtils.getNowDate());
                            deviceLog.setUserId(device.getTenantId());
                            deviceLog.setUserName(device.getTenantName());
                            deviceLog.setTenantId(device.getTenantId());
                            deviceLog.setTenantName(device.getTenantName());
                            deviceLog.setModelName(thingModels.getName());
                            deviceLog.setIsMonitor(thingModels.getIsMonitor());
                            deviceLogList.add(deviceLog);
                        }
                        break;
                    case SERVICE:
                        if (1 == thingModels.getIsHistory()) {
                            FunctionLog function = new FunctionLog();
                            function.setCreateTime(DateUtils.getNowDate());
                            function.setFunValue(value);
                            function.setSerialNumber(input.getDeviceNumber());
                            function.setIdentify(id);
                            function.setShowValue(value);
                            // 属性获取
                            function.setFunType(2);
                            function.setUserId(device.getTenantId());
                            function.setModelName(thingModels.getName());
                            functionLogList.add(function);
                        }
                        break;
                    case EVENT:
                        EventLog event = new EventLog();
                        event.setDeviceId(device.getDeviceId());
                        event.setDeviceName(device.getDeviceName());
                        event.setLogValue(value);
                        event.setSerialNumber(serialNumber);
                        event.setIdentity(id);
                        event.setLogType(3);
                        event.setIsMonitor(0);
                        event.setUserId(device.getTenantId());
                        event.setUserName(device.getTenantName());
                        event.setCreateTime(DateUtils.getNowDate());
                        // 1=影子模式，2=在线模式，3=其他
                        event.setMode(2);
                        eventLogList.add(event);
                        break;
                }
                list.add(item);
            }

        }
        // 缓存最新一条数据到redis
        redisCache.hashPutAll(key, maps);
        //存储历史数据
        if (!CollectionUtils.isEmpty(deviceLogList) && !isShadow) {
            for (DeviceLog deviceLog : deviceLogList) {
                logService.saveDeviceLog(deviceLog);
            }
        }
        //指令存储,影子模式不存储
        if (!CollectionUtils.isEmpty(functionLogList) && !isShadow) {
            functionLogService.insertBatch(functionLogList);
        }
        if (!CollectionUtils.isEmpty(eventLogList)) {
            //事件存储
            eventLogService.saveBatch(eventLogList);
        }
        /* ★★★★★★★★★★★★★★★★★★★★★★  存储数据 - 结束 ★★★★★★★★★★★★★★★★★★★★★★*/
        return list;
    }

    /**
     * 查询设备列表
     *
     * @param device 设备
     * @return 设备
     */
    @Override
    public List<Device> selectDeviceList(Device device) {
//        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles = user.getRoles();
//        for (int i = 0; i < roles.size(); i++) {
//            if (roles.get(i).getRoleKey().equals("tenant")) {
//                // 租户查看产品下所有设备
//                device.setTenantId(user.getUserId());
//            } else if (roles.get(i).getRoleKey().equals("general")) {
//                // 用户查看自己设备
//                device.setUserId(user.getUserId());
//            }
//        }
        return deviceMapper.selectDeviceList(device);
    }

    /**
     * 查询未分配授权码设备列表
     *
     * @param device 设备
     * @return 设备
     */
    @Override
    public List<Device> selectUnAuthDeviceList(Device device) {
//        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles = user.getRoles();
//        for (int i = 0; i < roles.size(); i++) {
//            if (roles.get(i).getRoleKey().equals("tenant")) {
//                // 租户查看产品下所有设备
//                device.setTenantId(user.getUserId());
//            } else if (roles.get(i).getRoleKey().equals("general")) {
//                // 用户查看自己设备
//                device.setUserId(user.getUserId());
//            }
//        }
        return deviceMapper.selectUnAuthDeviceList(device);
    }

    /**
     * TODO-2.3 查询分组可添加设备分页列表（分组用户与设备用户匹配）
     *
     * @param device 设备
     * @return 设备
     */
    @Override
    public List<Device> selectDeviceListByGroup(Device device) {
//        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles = user.getRoles();
//        for (int i = 0; i < roles.size(); i++) {
//            if (roles.get(i).getRoleKey().equals("tenant")) {
//                // 租户查看产品下所有设备
//                device.setTenantId(user.getUserId());
//            } else if (roles.get(i).getRoleKey().equals("general")) {
//                // 用户查看自己设备
//                device.setUserId(user.getUserId());
//            }
//        }
        return deviceMapper.selectDeviceListByGroup(device);
    }

    /**
     * 查询所有设备简短列表
     *
     * @return 设备
     */
    @Override
    public List<DeviceAllShortOutput> selectAllDeviceShortList(Device device) {
        return deviceMapper.selectAllDeviceShortList(device);
    }

    @Override
    public List<String> selectSerialNumberByProductId(Long productId) {
        return deviceMapper.selectSerialNumberByProductId(productId);
    }

    @Override
    public int selectDeviceCountByProductId(Long productId) {
        return deviceMapper.selectDeviceCountByProductId(productId);
    }

    /**
     * 查询设备分页简短列表
     *
     * @param device 设备
     * @return 设备
     */
    @Override
    public List<DeviceShortOutput> selectDeviceShortList(Device device) {
        List<DeviceShortOutput> list = deviceMapper.selectDeviceShortList(device);
        List<DeviceAlertCount> alist = alertLogService.selectDeviceAlertCount();
        for (DeviceAlertCount item : alist) {
            list.stream()
                    .filter(it -> Objects.equals(it.getSerialNumber(), item.getSerialNumber()))
                    .forEach(it -> it.setAlertCount(item));
        }
        return list;
    }

    /**
     * 查询设备
     *
     * @param deviceId 设备主键
     * @return 设备
     */
    // TODO 22--slaveId
    @Override
    public DeviceShortOutput selectDeviceRunningStatusByDeviceId(Long deviceId) {
        DeviceShortOutput device = deviceMapper.selectDeviceRunningStatusByDeviceId(deviceId);
        JSONObject thingsModelObject = JSONObject.parseObject(itslCache.getCacheThingsModelByProductId(device.getProductId()));
        JSONArray properties = thingsModelObject.getJSONArray("properties");
        JSONArray functions = thingsModelObject.getJSONArray("functions");
        List<ValueItem> thingsModelValueItems = itslValueCache.getCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
        // 物模型转换赋值
        List<ThingsModelValueItem> thingsList = new ArrayList<>();
        //判断一下properties 和 functions是否为空, 否则报空指针
        if (!CollectionUtils.isEmpty(properties)) {
            thingsList.addAll(convertJsonToThingsList(properties, thingsModelValueItems, 1));
        }
        if (!CollectionUtils.isEmpty(functions)) {
            thingsList.addAll(convertJsonToThingsList(functions, thingsModelValueItems, 2));
        }
        device.setThingsModels(thingsList);
        return device;

    }

    /**
     * 物模型基本类型转换赋值
     *
     * @param jsonArray
     * @param thingsModelValues
     * @param type
     * @return
     */
    @Async
    public List<ThingsModelValueItem> convertJsonToThingsList(JSONArray jsonArray, List<ValueItem> thingsModelValues, Integer type) {
        List<ThingsModelValueItem> thingsModelList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ThingsModelValueItem thingsModel = new ThingsModelValueItem();
            JSONObject thingsJson = jsonArray.getJSONObject(i);
            JSONObject datatypeJson = thingsJson.getJSONObject("datatype");
            thingsModel.setId(thingsJson.getString("id"));
            thingsModel.setName(thingsJson.getString("name"));
            thingsModel.setIsMonitor(thingsJson.getInteger("isMonitor") == null ? 0 : thingsJson.getInteger("isMonitor"));
            thingsModel.setIsReadonly(thingsJson.getInteger("isReadonly") == null ? 0 : thingsJson.getInteger("isReadonly"));
            thingsModel.setIsChart(thingsJson.getInteger("isChart") == null ? 0 : thingsJson.getInteger("isChart"));
            thingsModel.setIsSharePerm(thingsJson.getInteger("isSharePerm") == null ? 0 : thingsJson.getInteger("isSharePerm"));
            thingsModel.setIsHistory(thingsJson.getInteger("isHistory") == null ? 0 : thingsJson.getInteger("isHistory"));
            thingsModel.setIsApp(thingsJson.getInteger("isApp") == null ? 0 : thingsJson.getInteger("isApp"));
            thingsModel.setOrder(thingsJson.getInteger("order") == null ? 0 : thingsJson.getInteger("order"));
            thingsModel.setType(type);
            // 获取value
            for (ValueItem valueItem : thingsModelValues) {
                if (valueItem.getId().equals(thingsModel.getId())) {
                    thingsModel.setValue(valueItem.getValue());
                    thingsModel.setShadow(valueItem.getShadow());
                    thingsModel.setTs(valueItem.getTs());
                    break;
                }
            }
            // json转DataType(DataType赋值)
            Datatype dataType = convertJsonToDataType(datatypeJson, thingsModelValues, type, thingsModel.getId() + "_");
            thingsModel.setDatatype(dataType);
            if (JsonUtils.isJson(thingsModel.getValue())) {
                JSONObject jsonObject = JSONObject.parseObject(thingsModel.getValue());
                for (EnumItem enumItem : dataType.getEnumList()) {
                    ThingsModelValueItem model = new ThingsModelValueItem();
                    BeanUtils.copyProperties(thingsModel, model);
                    String val = jsonObject.getString(enumItem.getValue());
                    model.setValue(val);
                    model.setName(enumItem.getValue());
                    thingsModelList.add(model);
                }
            } else {
                // 物模型项添加到集合
                thingsModelList.add(thingsModel);
            }
        }
        return thingsModelList;
    }

    /**
     * 物模型DataType转换
     *
     * @param datatypeJson
     * @param thingsModelValues
     * @param type
     * @param parentIdentifier  上级标识符
     * @return
     */
    private Datatype convertJsonToDataType(JSONObject datatypeJson, List<ValueItem> thingsModelValues, Integer type, String parentIdentifier) {
        Datatype dataType = new Datatype();
        //有些物模型数据定义为空的情况兼容
        if (datatypeJson == null) {
            return dataType;
        }
        dataType.setType(datatypeJson.getString("type"));
        if (dataType.getType().equals("decimal")) {
            dataType.setMax(datatypeJson.getBigDecimal("max"));
            dataType.setMin(datatypeJson.getBigDecimal("min"));
            dataType.setStep(datatypeJson.getBigDecimal("step"));
            dataType.setUnit(datatypeJson.getString("unit"));
        } else if (dataType.getType().equals("integer")) {
            dataType.setMax(datatypeJson.getBigDecimal("max"));
            dataType.setMin(datatypeJson.getBigDecimal("min"));
            dataType.setStep(datatypeJson.getBigDecimal("step"));
            dataType.setUnit(datatypeJson.getString("unit"));
        } else if (dataType.getType().equals("bool")) {
            dataType.setFalseText(datatypeJson.getString("falseText"));
            dataType.setTrueText(datatypeJson.getString("trueText"));
        } else if (dataType.getType().equals("string")) {
            dataType.setMaxLength(datatypeJson.getInteger("maxLength"));
        } else if (dataType.getType().equals("enum")) {
            List<EnumItem> enumItemList = JSON.parseArray(datatypeJson.getString("enumList"), EnumItem.class);
            dataType.setEnumList(enumItemList);
            dataType.setShowWay(datatypeJson.getString("showWay"));
        } else if (dataType.getType().equals("object")) {
            JSONArray jsonArray = JSON.parseArray(datatypeJson.getString("params"));
            // 物模型值过滤（parentId_开头）
            thingsModelValues = thingsModelValues.stream().filter(x -> x.getId().startsWith(parentIdentifier)).collect(Collectors.toList());
            List<ThingsModelValueItem> thingsList = convertJsonToThingsList(jsonArray, thingsModelValues, type);
            // 排序
            thingsList = thingsList.stream().sorted(Comparator.comparing(ThingsModelValueItem::getOrder).reversed()).collect(Collectors.toList());
            dataType.setParams(thingsList);
        } else if (dataType.getType().equals("array")) {
            dataType.setArrayType(datatypeJson.getString("arrayType"));
            dataType.setArrayCount(datatypeJson.getInteger("arrayCount"));
            if ("object".equals(dataType.getArrayType())) {
                // 对象数组
                JSONArray jsonArray = datatypeJson.getJSONArray("params");
                // 物模型值过滤（parentId_开头）
                thingsModelValues = thingsModelValues.stream().filter(x -> x.getId().startsWith(parentIdentifier)).collect(Collectors.toList());
                List<ThingsModelValueItem> thingsList = convertJsonToThingsList(jsonArray, thingsModelValues, type);
                // 排序
                thingsList = thingsList.stream().sorted(Comparator.comparing(ThingsModelValueItem::getOrder).reversed()).collect(Collectors.toList());
                // 数组类型物模型里面对象赋值
                List<ThingsModel>[] arrayParams = new List[dataType.getArrayCount()];
                for (int i = 0; i < dataType.getArrayCount(); i++) {
                    List<ThingsModel> thingsModels = new ArrayList<>();
                    for (int j = 0; j < thingsList.size(); j++) {
                        ThingsModel thingsModel = new ThingsModel();
                        BeanUtils.copyProperties(thingsList.get(j), thingsModel);
                        String shadow = thingsList.get(j).getShadow();
                        if (StringUtils.isNotEmpty(shadow) && !shadow.equals("")) {
                            String[] shadows = shadow.split(",");
                            if (i + 1 > shadows.length) {
                                // 解决产品取消发布，增加数组长度导致设备影子和值赋值失败
                                thingsModel.setShadow(" ");
                            } else {
                                thingsModel.setShadow(shadows[i]);
                            }
                        }
                        String value = thingsList.get(j).getValue();
                        if (StringUtils.isNotEmpty(value) && !value.equals("")) {
                            String[] values = value.split(",");
                            if (i + 1 > values.length) {
                                thingsModel.setValue(" ");
                            } else {
                                thingsModel.setValue(values[i]);
                            }
                        }
                        thingsModels.add(thingsModel);
                    }
                    arrayParams[i] = thingsModels;
                }
                dataType.setArrayParams(arrayParams);
            }
        }
        return dataType;
    }

    /**
     * 新增设备
     *
     * @param device 设备
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Device insertDevice(Device device) {
        // 设备编号唯一检查
        Device existDevice = deviceMapper.selectDeviceBySerialNumber(device.getSerialNumber());
        if (!Objects.isNull(existDevice)) {
            throw new ServiceException("设备编号：" + device.getSerialNumber() + " 已经存在，新增失败");
        }
        SysUser sysUser = getLoginUser().getUser();
        //添加设备
        device.setCreateTime(DateUtils.getNowDate());
        device.setTenantId(sysUser.getDept().getDeptUserId());
        device.setTenantName(sysUser.getDept().getDeptUserName());
        device.setRssi(0);
        // 设置图片
        Product product = productService.selectProductByProductId(device.getProductId());
        device.setImgUrl(product.getImgUrl());
        // 随机经纬度和地址
        SysUser user = getLoginUser().getUser();
        device.setNetworkIp(user.getLoginIp());
        setLocation(user.getLoginIp(), device);
        deviceMapper.insertDevice(device);
        // 处理modbus产品
        if (FastBeeConstant.PROTOCOL.ModbusRtu.equals(product.getProtocolCode()) || FastBeeConstant.PROTOCOL.ModbusTcp.equals(product.getProtocolCode())) {
            this.handleModbusConfig(device, product);
        }
        // redis缓存设备默认状态（物模型值）
        itslValueCache.addCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
        return device;
    }

    private void handleModbusConfig(Device device, Product product) {
        if (DeviceType.DIRECT_DEVICE.getCode() == device.getDeviceType()) {
            LambdaQueryWrapper<ProductModbusJob> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductModbusJob::getProductId, product.getProductId());
            List<ProductModbusJob> productModbusJobList = productModbusJobMapper.selectList(queryWrapper);
            for (ProductModbusJob productModbusJob : productModbusJobList) {
                ModbusJob modbusJob = new ModbusJob();
                modbusJob.setJobName(productModbusJob.getJobName());
                modbusJob.setSubDeviceId(device.getDeviceId());
                modbusJob.setSubSerialNumber(device.getSerialNumber());
                modbusJob.setCommand(productModbusJob.getCommand());
                modbusJob.setStatus("0");
                modbusJob.setRemark(productModbusJob.getRemark());
                modbusJobService.insertModbusJob(modbusJob);
                redisCache.getCacheModbusTcpId(device.getSerialNumber());
            }
        } else if (DeviceType.GATEWAY.getCode() ==  device.getDeviceType()) {
            LambdaQueryWrapper<ProductSubGateway> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductSubGateway::getGwProductId, product.getProductId());
            // 处理子设备添加和子设备轮询
            ProductSubGateway productSubGateway = new ProductSubGateway();
            productSubGateway.setGwProductId(product.getProductId());
            List<ProductSubGatewayVO> productSubGatewayList = productSubGatewayMapper.selectListVO(productSubGateway);
            if (CollectionUtils.isEmpty(productSubGatewayList)) {
                return;
            }
            productSubGatewayList.sort(Comparator.comparing(ProductSubGatewayVO::getSlaveId));
            List<Long> subProductIdList = productSubGatewayList.stream().map(ProductSubGatewayVO::getSubProductId).collect(Collectors.toList());
            LambdaQueryWrapper<ProductModbusJob> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(ProductModbusJob::getProductId, subProductIdList);
            List<ProductModbusJob> productModbusJobList = productModbusJobMapper.selectList(queryWrapper1);
            Map<Long, List<ProductModbusJob>> productModbusJobMap = productModbusJobList.stream().collect(Collectors.groupingBy(ProductModbusJob::getProductId));
            // 新增子设备、绑定子设备、子设备生成轮询
            for (ProductSubGatewayVO productSubGatewayVO : productSubGatewayList) {
                Device addDevice = new Device();
                addDevice.setDeviceName(productSubGatewayVO.getSubProductName() + "_" + productSubGatewayVO.getSlaveId());
                addDevice.setProductId(productSubGatewayVO.getSubProductId());
                addDevice.setProductName(productSubGatewayVO.getSubProductName());
                String serialNumber = this.generationDeviceNum(1);
                addDevice.setSerialNumber(serialNumber);
                addDevice.setTenantId(device.getTenantId());
                addDevice.setTenantName(device.getTenantName());
                addDevice.setCreateBy(device.getCreateBy());
                addDevice.setFirmwareVersion(new BigDecimal(1));
                addDevice.setRssi(0);
                addDevice.setIsShadow(0);
                addDevice.setLocationWay(productSubGatewayVO.getSubLocationWay());
                addDevice.setImgUrl(product.getImgUrl());
                addDevice.setCreateTime(new Date());
                deviceMapper.insertDevice(addDevice);
                SubGateway subGateway = new SubGateway();
                subGateway.setGwDeviceId(device.getDeviceId());
                subGateway.setSubDeviceId(addDevice.getDeviceId());
                subGateway.setSlaveId(productSubGatewayVO.getSlaveId());
                subGateway.setCreateBy(addDevice.getCreateBy());
                subGateway.setCreateTime(new Date());
                subGatewayMapper.insertGateway(subGateway);
                List<ProductModbusJob> productModbusList = productModbusJobMap.get(productSubGatewayVO.getSubProductId());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(productModbusList)) {
                    for (ProductModbusJob productModbusJob : productModbusList) {
                        ModbusJob modbusJob = new ModbusJob();
                        modbusJob.setJobName(productModbusJob.getJobName());
                        modbusJob.setSubDeviceId(addDevice.getDeviceId());
                        modbusJob.setSubSerialNumber(addDevice.getSerialNumber());
                        modbusJob.setCommand(productModbusJob.getCommand());
                        modbusJob.setStatus(productModbusJob.getStatus());
                        modbusJob.setRemark(productModbusJob.getRemark());
                        modbusJobService.insertModbusJob(modbusJob);
                        redisCache.getCacheModbusTcpId(addDevice.getSerialNumber());
                    }
                }
            }

        }
    }

    /**
     * 终端-设备关联用户
     *
     * @param params
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deviceRelateUser(DeviceRelateUserInput params) {
        Long userId = params.getUserId(); //终端用户id
        // 查询用户信息
        SysUser sysUser = userService.selectUserById(userId);
        for (DeviceNumberAndProductId item : params.getDeviceNumberAndProductIds()) {
            Device existDevice = deviceMapper.selectDeviceBySerialNumber(item.getDeviceNumber());
            if (!Objects.isNull(existDevice)) {
                // 先删除设备的所有用户
                deviceUserMapper.deleteDeviceUserByDeviceId(new UserIdDeviceIdModel(null, existDevice.getDeviceId()));
                // 添加新的设备用户
                DeviceUser deviceUser = new DeviceUser();
                deviceUser.setUserId(sysUser.getUserId());
                deviceUser.setPhonenumber(sysUser.getPhonenumber());
                deviceUser.setDeviceId(existDevice.getDeviceId());
                deviceUser.setCreateTime(DateUtils.getNowDate());
                deviceUserMapper.insertDeviceUser(deviceUser);
            } else {
                // 自动添加设备
                int result = insertDeviceAuto(
                        item.getDeviceNumber(),
                        userId,
                        item.getProductId());
                if (result == 0) {
                    return AjaxResult.error("设备不存在，自动添加设备时失败，请检查产品编号是否正确");
                }
            }
        }
        return AjaxResult.success("添加设备成功");
    }

    /**
     * 设备认证后自动添加设备
     *
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDeviceAuto(String serialNumber, Long userId, Long productId) {
        // 设备编号唯一检查
        int count = deviceMapper.selectDeviceCountBySerialNumber(serialNumber);
        if (count != 0) {
            log.error("设备编号：" + serialNumber + "已经存在了，新增设备失败");
            return 0;
        }
        Product product = productService.selectProductByProductId(productId);
        if (product == null) {
            log.error("自动添加设备时，根据产品ID查找不到对应产品");
            return 0;
        }
        Device device = new Device();
        device.setSerialNumber(serialNumber);
        SysUser user = userService.getDeptUserByUserId(userId);
        device.setTenantId(user.getUserId());
        device.setTenantName(user.getUserName());
        device.setFirmwareVersion(BigDecimal.valueOf(1.0));
        // 设备状态（1-未激活，2-禁用，3-在线，4-离线）
        device.setStatus(3);
        device.setActiveTime(DateUtils.getNowDate());
        device.setIsShadow(0);
        device.setRssi(0);
        // 1-自动定位，2-设备定位，3-自定义位置
        device.setLocationWay(product.getLocationWay());
        device.setCreateTime(DateUtils.getNowDate());
        // 随机位置
        device.setLongitude(BigDecimal.valueOf(116.23 - (Math.random() * 15)));
        device.setLatitude(BigDecimal.valueOf(39.54 - (Math.random() * 15)));
        device.setNetworkAddress("中国");
        device.setNetworkIp("127.0.0.1");

        int random = (int) (Math.random() * (90)) + 10;
        device.setDeviceName(product.getProductName() + random);
        device.setImgUrl(product.getImgUrl());
        device.setProductId(product.getProductId());
        device.setProductName(product.getProductName());
        int result = deviceMapper.insertDevice(device);

        // 缓存设备状态
        itslValueCache.addCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
        return result;
    }

    /**
     * 获取用户操作设备的影子值
     *
     * @param device
     * @return
     */
    @Override
    public ThingsModelShadow getDeviceShadowThingsModel(Device device) {
        // 物模型值
        List<ValueItem> thingsModelValueItems = itslValueCache.getCacheDeviceStatus(device.getProductId(), device.getSerialNumber());
        ThingsModelShadow shadow = new ThingsModelShadow();
        // 查询出设置的影子值
        List<ThingsModelValueItem> shadowList = new ArrayList<>();
        for (ValueItem item : thingsModelValueItems) {
            if (!item.getShadow().equals(item.getValue()) && !item.getShadow().isEmpty()) {
                //处理id
                String id = item.getId();
                if (item.getShadow().contains(",")) {
                    String[] shadows = item.getShadow().split(",");
                    String[] values = item.getValue().split(",");
                    for (int i = 0; i < shadows.length; i++) {
                        String value = values[i];
                        String shaVal = shadows[i];
                        if (!shaVal.equals(value)) {
                            id = "array_0" + i + "_" + id;
                            ThingsModelSimpleItem shaowItem = new ThingsModelSimpleItem(id, shaVal, "");
                            shadow.getProperties().add(shaowItem);
                        }
                    }
                } else {
                    ThingsModelSimpleItem shaowItem = new ThingsModelSimpleItem(id, item.getShadow(), "");
                    shadow.getProperties().add(shaowItem);
                }
                String cacheKey = RedisKeyBuilder.buildTSLVCacheKey(device.getProductId(), device.getSerialNumber());
                item.setShadow("");
                redisCache.setCacheMapValue(cacheKey, item.getId(), JSON.toJSONString(item));
            }
        }
        return shadow;
    }


    /**
     * 生成设备唯一编号
     *
     * @return 结果
     */
    @Override
    public String generationDeviceNum(Integer type) {
        // 设备编号：D + userId + 15位随机字母和数字
        SysUser user = getLoginUser().getUser();
        String number;
        //Hex随机字符串
        if (type == 3) {
            number = toolService.generateRandomHex(12);
        } else {
            number = "D" + user.getUserId().toString() + toolService.getStringRandom(10);
        }
        int count = deviceMapper.getDeviceNumCount(number);
        if (count == 0) {
            return number;
        } else {
            generationDeviceNum(type);
        }
        return "";
    }

    /**
     * @param device 设备状态和定位更新
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceStatusAndLocation(Device device, String ipAddress) {
        // 设置自动定位和状态
        if (ipAddress != null && !Objects.equals(ipAddress, "")) {
            if (device.getActiveTime() == null) {
                device.setActiveTime(DateUtils.getNowDate());
            }
            // 定位方式(1=ip自动定位，2=设备定位，3=自定义)
            if (device.getLocationWay() == 1) {
                device.setNetworkIp(ipAddress);
                //setLocation(ipAddress, device);
            }
        }
        int result = deviceMapper.updateDeviceStatus(device);
        // 添加到设备日志
        EventLog event = new EventLog();
        event.setDeviceId(device.getDeviceId());
        event.setDeviceName(device.getDeviceName());
        event.setSerialNumber(device.getSerialNumber());
        event.setIsMonitor(0);
        event.setUserId(device.getTenantId());
        event.setUserName(device.getTenantName());
        event.setCreateTime(DateUtils.getNowDate());
        // 日志模式 1=影子模式，2=在线模式，3=其他
        event.setMode(3);
        if (device.getStatus() == 3) {
            event.setLogValue("1");
            event.setRemark("设备上线");
            event.setIdentity("online");
            event.setLogType(5);
        } else if (device.getStatus() == 4) {
            event.setLogValue("0");
            event.setRemark("设备离线");
            event.setIdentity("offline");
            event.setLogType(6);
        }
        eventLogMapper.insert(event);
        return result;
    }

    /**
     * @param device 设备状态
     * @return 结果
     */
    @Override
    public int updateDeviceStatus(Device device) {
        return deviceMapper.updateDeviceStatus(device);
    }

    /**
     * 更新固件版本
     *
     * @param device
     * @return
     */
    @Override
    public int updateDeviceFirmwareVersion(Device device) {
        return deviceMapper.updateDeviceFirmwareVersion(device);
    }

    /**
     * 上报设备信息
     *
     * @param device       上报的设备信息
     * @param deviceEntity 查询的设备实体
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reportDevice(Device device, Device deviceEntity) {
        // 未采用设备定位则清空定位，定位方式(1=ip自动定位，2=设备定位，3=自定义)
        if (deviceEntity.getLocationWay() != 2) {
            device.setLatitude(null);
            device.setLongitude(null);
        }
        int result = 0;
        // 设备端默认可设置UserID=1，需要排除
        if (deviceEntity != null && device.getUserId() != null && device.getUserId() != 1) {
            // 通过配网或者扫码关联设备后，设备的用户信息需要变更
            if (deviceEntity.getTenantId().longValue() != device.getUserId().longValue()) {
                // 先删除设备的所有用户
                deviceUserMapper.deleteDeviceUserByDeviceId(new UserIdDeviceIdModel(null, deviceEntity.getDeviceId()));
                // 添加新的设备用户
                SysUser sysUser = userService.selectUserById(device.getTenantId());
                if (sysUser != null) {
                    DeviceUser deviceUser = new DeviceUser();
                    deviceUser.setUserId(sysUser.getUserId());
                    deviceUser.setPhonenumber(sysUser.getPhonenumber());
                    deviceUser.setDeviceId(deviceEntity.getDeviceId());
                    deviceUser.setCreateTime(DateUtils.getNowDate());
                    deviceUserMapper.insertDeviceUser(deviceUser);
                }
                // 更新设备用户信息 多租户版本不需要
//                device.setTenantId(device.getTenantId());
//                device.setTenantName(sysUser.getUserName());
            }
            device.setUpdateTime(DateUtils.getNowDate());
            if (deviceEntity.getActiveTime() == null || deviceEntity.getActiveTime().equals("")) {
                device.setActiveTime(DateUtils.getNowDate());
            }

        }
        // 不更新物模型
        device.setThingsModelValue(null);
        result = deviceMapper.updateDeviceBySerialNumber(device);
        return result;
    }

    /**
     * 查询产品下所有设备，返回设备编号
     *
     * @param productId 产品id
     * @return
     */
    @Override
    public List<Device> selectDevicesByProductId(Long productId, Integer hasSub) {
        return deviceMapper.selectDevicesByProductId(productId, hasSub);
    }


    /**
     * 批量更新设备状态
     *
     * @param serialNumbers 设备ids
     * @param status        状态
     */
    @Deprecated
    @Override
    public void batchChangeStatus(List<String> serialNumbers, DeviceStatus status) {
        if (CollectionUtils.isEmpty(serialNumbers)) {
            return;
        }
        //设备离线
        if (DeviceStatus.OFFLINE.equals(status)) {
            deviceMapper.batchChangeOffline(serialNumbers);
        } else if (DeviceStatus.ONLINE.equals(status)) {
            deviceMapper.batchChangeOnline(serialNumbers);
        }
        deviceCache.updateBatchDeviceStatusCache(serialNumbers, status);
    }

    /**
     * 根据设备编号查询设备信息 -不带缓存物模型值
     *
     * @param serialNumber
     * @return
     */
    @Override
    public Device selectDeviceNoModel(String serialNumber) {
        return deviceMapper.selectDeviceBySerialNumber(serialNumber);
    }

    /**
     * 获取设备MQTT连接参数
     *
     * @param deviceId 设备id
     * @return
     */
    @Override
    public DeviceMqttConnectVO getMqttConnectData(Long deviceId) {
        DeviceMqttConnectVO connectVO = new DeviceMqttConnectVO();
        DeviceMqttVO deviceMqttVO = deviceMapper.selectMqttConnectData(deviceId);
        if (deviceMqttVO == null) {
            throw new ServiceException("获取设备MQTT连接参数失败");
        }
        if (FastBeeConstant.TRANSPORT.TCP.equals(deviceMqttVO.getTransport())) {
            connectVO.setPort(tcpPort);
            connectVO.setEnrollPackage("7e80" + deviceMqttVO.getSerialNumber() + "7e");
            return connectVO;
        }
        // 不管认证方式，目前就只返回简单认证方式
        String password;
        if (ProductAuthConstant.AUTHORIZE.equals(deviceMqttVO.getIsAuthorize())) {
            // 查询产品授权码
            List<ProductAuthorize> productAuthorizeList = productAuthorizeService.listByProductId(deviceMqttVO.getProductId());
            if (CollectionUtils.isEmpty(productAuthorizeList)) {
                throw new ServiceException("产品已启用授权，获取设备授权码失败，请先配置授权码");
            }
            List<ProductAuthorize> collect = productAuthorizeList.stream().filter(p -> p.getProductId().equals(deviceMqttVO.getDeviceId())).collect(Collectors.toList());
            ProductAuthorize productAuthorize = CollectionUtils.isEmpty(collect) ? productAuthorizeList.get(0) : collect.get(0);
            password = deviceMqttVO.getMqttPassword() + "&" + productAuthorize.getAuthorizeCode();
        } else {
            password = deviceMqttVO.getMqttPassword();
        }
        String clientId = ProductAuthConstant.CLIENT_ID_AUTH_TYPE_SIMPLE + "&" + deviceMqttVO.getSerialNumber() + "&" + deviceMqttVO.getProductId() + "&" + deviceMqttVO.getTenantId();
        // 组装返回结果
        connectVO.setClientId(clientId).setUsername(deviceMqttVO.getMqttAccount()).setPasswd(password).setPort(brokerPort);
        connectVO.setSubscribeTopic("/" + deviceMqttVO.getProductId() + "/" + deviceMqttVO.getSerialNumber() + "/function/get");
        connectVO.setReportTopic("/" + deviceMqttVO.getProductId() + "/" + deviceMqttVO.getSerialNumber() + "/property/post");
        return connectVO;
    }

    /**
     * 重置设备状态
     */
    @Override
    public void reSetDeviceStatus() {
        deviceMapper.reSetDeviceStatus();
    }

    /**
     * 根据IP获取地址
     *
     * @param ip
     * @return
     */
    private void setLocation(String ip, Device device) {
        String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
        String address = "未知地址";
        // 内网不查询
        if (IpUtils.internalIp(ip)) {
            device.setNetworkAddress("内网IP");
        }
        try {
            String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
            if (!StringUtils.isEmpty(rspStr)) {
                JSONObject obj = JSONObject.parseObject(rspStr);
                device.setNetworkAddress(obj.getString("addr"));
                System.out.println(device.getSerialNumber() + "- 设置地址：" + obj.getString("addr"));
                // 查询经纬度
                setLatitudeAndLongitude(obj.getString("city"), device);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 设置经纬度
     *
     * @param city
     */
    private void setLatitudeAndLongitude(String city, Device device) {
        String BAIDU_URL = "https://api.map.baidu.com/geocoder";
        String baiduResponse = HttpUtils.sendGet(BAIDU_URL, "address=" + city + "&output=json", Constants.GBK);
        if (!StringUtils.isEmpty(baiduResponse)) {
            JSONObject baiduObject = JSONObject.parseObject(baiduResponse);
            JSONObject location = baiduObject.getJSONObject("result").getJSONObject("location");
            device.setLongitude(location.getBigDecimal("lng"));
            device.setLatitude(location.getBigDecimal("lat"));
            System.out.println(device.getSerialNumber() + "- 设置经度：" + location.getBigDecimal("lng") + "，设置纬度：" + location.getBigDecimal("lat"));
        }
    }

    /**
     * 根据产品ID获取产品下所有编号
     *
     * @param productId
     * @return
     */
    @Override
    public String[] getDeviceNumsByProductId(Long productId) {
        return deviceMapper.getDeviceNumsByProductId(productId);
    }


    /**
     * 重置设备状态
     *
     * @return 结果
     */
    @Override
    public int resetDeviceStatus(String deviceNum) {
        return deviceMapper.resetDeviceStatus(deviceNum);
    }

    @Override
    public String importDevice(List<DeviceImportVO> deviceImportVOList, Long productId) {
        LoginUser loginUser = getLoginUser();
        SysUser deptUser = userService.getDeptUserByUserId(loginUser.getUserId());
        Product product = productService.selectProductByProductId(productId);
        if (null == product) {
            return "导入失败，产品信息为空";
        }
        List<String> serialNumberList = deviceImportVOList.stream().map(DeviceImportVO::getSerialNumber).collect(Collectors.toList());
        List<String> oldSerialNumberList = deviceMapper.checkExistBySerialNumbers(serialNumberList);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(oldSerialNumberList)) {
            return "以下设备编号（" + JSON.toJSONString(oldSerialNumberList) + ")已存在，请修改后重试";
        }
        List<Device> deviceList = new ArrayList<>();
        for (DeviceImportVO deviceImportVO : deviceImportVOList) {
            Device device = new Device();
            device.setDeviceName(deviceImportVO.getDeviceName());
            if (StringUtils.isEmpty(deviceImportVO.getSerialNumber())) {
                device.setSerialNumber(this.generationDeviceNum(1));
            } else {
                device.setSerialNumber(deviceImportVO.getSerialNumber());
            }
            device.setProductId(product.getProductId());
            device.setProductName(product.getProductName());
            device.setLocationWay(product.getLocationWay());
            device.setTenantId(deptUser.getUserId());
            device.setTenantName(deptUser.getUserName());
            device.setCreateBy(loginUser.getUserId().toString());
            device.setFirmwareVersion(new BigDecimal(1));
            device.setRssi(0);
            device.setIsShadow(0);
            device.setImgUrl(product.getImgUrl());
            deviceList.add(device);
        }
        int result = deviceMapper.insertBatchDevice(deviceList);
        // 新增导入记录
        DeviceRecord deviceRecord = new DeviceRecord();
        deviceRecord.setOperateDeptId(loginUser.getDeptId()).setProductId(productId).setType(DeviceRecordTypeEnum.IMPORT.getType())
                .setTotal(deviceList.size()).setTenantId(loginUser.getUser().getDept().getDeptUserId()).setTenantName(loginUser.getUser().getDept().getDeptUserName());
        if (result > 0) {
            deviceRecord.setStatus(StatusEnum.SUCCESS.getStatus()).setSuccessQuantity(deviceList.size()).setFailQuantity(0);
        } else {
            deviceRecord.setStatus(StatusEnum.FAIL.getStatus()).setSuccessQuantity(0).setFailQuantity(deviceList.size());
        }
        deviceRecord.setCreateBy(loginUser.getUsername());
        deviceRecordMapper.insertDeviceRecord(deviceRecord);
        return result > 0 ? "" : "导入失败";
    }

    @Override
    public String importAssignmentDevice(List<DeviceAssignmentVO> deviceAssignmentVOS, Long productId, Long deptId) {
        LoginUser loginUser = getLoginUser();
        Product product = productService.selectProductByProductId(productId);
        if (null == product) {
            return "导入失败，产品信息为空";
        }
        List<String> serialNumberList = deviceAssignmentVOS.stream().map(DeviceAssignmentVO::getSerialNumber).collect(Collectors.toList());
        List<String> oldSerialNumberList = deviceMapper.checkExistBySerialNumbers(serialNumberList);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(oldSerialNumberList)) {
            return "以下设备编号（" + JSON.toJSONString(oldSerialNumberList) + ")已存在，请修改后重试";
        }
        SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
        if (null == sysDept) {
            return "机构不存在，请重新选择机构！";
        }
        SysUser sysUser = userService.selectUserById(sysDept.getDeptUserId());
        if (null == sysUser) {
            return "机构管理员信息不存在，请重新选择机构！";
        }
        List<Device> deviceList = new ArrayList<>();
        for (DeviceAssignmentVO deviceAssignmentVO : deviceAssignmentVOS) {
            Device device = new Device();
            device.setDeviceName(deviceAssignmentVO.getDeviceName());
            if (StringUtils.isEmpty(deviceAssignmentVO.getSerialNumber())) {
                device.setSerialNumber(this.generationDeviceNum(1));
            } else {
                device.setSerialNumber(deviceAssignmentVO.getSerialNumber());
            }
            device.setProductId(product.getProductId());
            device.setProductName(product.getProductName());
            device.setLocationWay(product.getLocationWay());
            device.setCreateBy(loginUser.getUserId().toString());
            device.setTenantId(sysUser.getUserId());
            device.setTenantName(sysUser.getUserName());
            device.setFirmwareVersion(new BigDecimal(1));
            device.setRssi(0);
            device.setIsShadow(0);
            device.setImgUrl(product.getImgUrl());
            deviceList.add(device);
        }
        int result = deviceMapper.insertBatchDevice(deviceList);
        // 新增导入记录
        DeviceRecord deviceRecord = new DeviceRecord();
        deviceRecord.setOperateDeptId(loginUser.getDeptId()).setTargetDeptId(deptId).setTargetDeptId(deptId).setProductId(productId).setDistributeType(DeviceDistributeTypeEnum.IMPORT.getType())
                .setType(DeviceRecordTypeEnum.ASSIGNMENT.getType()).setTotal(deviceList.size()).setTenantId(loginUser.getUser().getDept().getDeptUserId()).setTenantName(loginUser.getUser().getDept().getDeptUserName());
        List<DeviceRecord> deviceRecordList = new ArrayList<>();
        if (result > 0) {
            deviceRecord.setStatus(StatusEnum.SUCCESS.getStatus());
            deviceRecord.setSuccessQuantity(deviceList.size());
            deviceRecord.setFailQuantity(0);
        } else {
            deviceRecord.setStatus(StatusEnum.FAIL.getStatus());
            deviceRecord.setSuccessQuantity(0);
            deviceRecord.setFailQuantity(deviceList.size());
        }
        deviceRecord.setCreateBy(loginUser.getUsername());
        int insertRecordStatus = deviceRecordMapper.insertDeviceRecord(deviceRecord);
        for (Device device : deviceList) {
            DeviceRecord deviceRecord1 = new DeviceRecord();
            deviceRecord1.setOperateDeptId(loginUser.getDeptId()).setTargetDeptId(deptId).setProductId(productId).setDeviceId(device.getDeviceId())
                    .setType(DeviceRecordTypeEnum.ASSIGNMENT_DETAIL.getType()).setParentId(deviceRecord.getId()).setSerialNumber(device.getSerialNumber())
                    .setDistributeType(DeviceDistributeTypeEnum.IMPORT.getType()).setTenantId(loginUser.getUser().getDept().getDeptUserId()).setTenantName(loginUser.getUser().getDept().getDeptUserName());
            deviceRecord1.setStatus(insertRecordStatus > 0 ? StatusEnum.SUCCESS.getStatus() : StatusEnum.FAIL.getStatus());
            deviceRecord1.setCreateBy(loginUser.getUsername());
            deviceRecordList.add(deviceRecord1);
        }
        deviceRecordMapper.insertBatch(deviceRecordList);
        return result > 0 ? "" : "导入失败";
    }

    /**
     * 获取所有已经激活并不是禁用的设备
     *
     * @return
     */
    @Override
    public List<DeviceStatusVO> selectDeviceActive() {
        return deviceMapper.selectDeviceActive();
    }

    @Override
    public AjaxResult assignment(Long deptId, String deviceIds) {
        SysUser user = getLoginUser().getUser();
        SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
        if (null == sysDept || null == sysDept.getDeptUserId()) {
            return AjaxResult.error("机构不存在或未绑定管理员，请调整后重试！");
        }
        Long deptUserId = sysDept.getDeptUserId();
        SysUser sysUser = userService.selectUserById(deptUserId);
        if (null == sysUser) {
            return AjaxResult.error("机构管理员不存在");
        }
        List<Long> deviceIdList = Arrays.stream(deviceIds.split(",")).map(d -> Long.parseLong(d.trim())).distinct().collect(Collectors.toList());
        int result = deviceMapper.updateTenantIdByDeptIds(deptUserId, sysUser.getUserName(), deviceIdList);
        DeviceRecord deviceRecord = new DeviceRecord();
        deviceRecord.setOperateDeptId(user.getDeptId()).setTargetDeptId(deptId).setTargetDeptId(deptId).setDistributeType(DeviceDistributeTypeEnum.SELECT.getType())
                .setType(DeviceRecordTypeEnum.ASSIGNMENT.getType()).setTotal(deviceIdList.size()).setTenantId(user.getDept().getDeptUserId()).setTenantName(user.getDept().getDeptUserName());
        if (result > 0) {
            deviceRecord.setStatus(StatusEnum.SUCCESS.getStatus());
            deviceRecord.setSuccessQuantity(deviceIdList.size());
            deviceRecord.setFailQuantity(0);
        } else {
            deviceRecord.setStatus(StatusEnum.FAIL.getStatus());
            deviceRecord.setSuccessQuantity(0);
            deviceRecord.setFailQuantity(deviceIdList.size());
        }
        deviceRecord.setCreateBy(user.getUserName());
        int insertRecordStatus = deviceRecordMapper.insertDeviceRecord(deviceRecord);
        List<DeviceRecord> deviceRecordList = new ArrayList<>();
        List<Device> deviceList = deviceMapper.selectDeviceListByDeviceIds(deviceIdList);
        for (Device device : deviceList) {
            DeviceRecord deviceRecord1 = new DeviceRecord();
            deviceRecord1.setOperateDeptId(user.getDeptId()).setTargetDeptId(deptId).setDeviceId(device.getDeviceId())
                    .setProductId(device.getProductId()).setSerialNumber(device.getSerialNumber())
                    .setType(DeviceRecordTypeEnum.ASSIGNMENT_DETAIL.getType()).setParentId(deviceRecord.getId())
                    .setDistributeType(DeviceDistributeTypeEnum.SELECT.getType()).setTenantId(user.getDept().getDeptUserId()).setTenantName(user.getDept().getDeptUserName());
            deviceRecord1.setStatus(insertRecordStatus > 0 ? StatusEnum.SUCCESS.getStatus() : StatusEnum.FAIL.getStatus());
            deviceRecord1.setCreateBy(user.getUserName());
            deviceRecordList.add(deviceRecord1);
        }
        deviceRecordMapper.insertBatch(deviceRecordList);
        return result > 0 ? AjaxResult.success("分配设备成功") : AjaxResult.error("分配设备失败");
    }

    @Override
    public AjaxResult recovery(String deviceIds, Long recoveryDeptId) {
        LoginUser loginUser = getLoginUser();
        Long deptId = loginUser.getDeptId();
        SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
        if (null == sysDept || null == sysDept.getDeptUserId()) {
            return AjaxResult.error("机构不存在或未绑定管理员，请调整后重试！");
        }
        Long deptUserId = sysDept.getDeptUserId();
        SysUser sysUser = userService.selectUserById(deptUserId);
        if (null == sysUser) {
            return AjaxResult.error("机构管理员不存在");
        }
        List<Long> deviceIdList = Arrays.stream(deviceIds.split(",")).map(d -> Long.parseLong(d.trim())).distinct().collect(Collectors.toList());
        int result = deviceMapper.updateTenantIdByDeptIds(deptUserId, sysUser.getUserName(), deviceIdList);
        List<DeviceRecord> deviceRecordList = new ArrayList<>();
        List<Device> deviceList = deviceMapper.selectDeviceListByDeviceIds(deviceIdList);
        for (Device device : deviceList) {
            DeviceRecord deviceRecord1 = new DeviceRecord();
            deviceRecord1.setOperateDeptId(deptId).setTargetDeptId(recoveryDeptId).setDeviceId(device.getDeviceId())
                    .setProductId(device.getProductId()).setSerialNumber(device.getSerialNumber())
                    .setType(DeviceRecordTypeEnum.RECOVERY.getType()).setTenantId(loginUser.getUser().getDept().getDeptUserId()).setTenantName(loginUser.getUser().getDept().getDeptUserName());
            deviceRecord1.setStatus(result > 0 ? StatusEnum.SUCCESS.getStatus() : StatusEnum.FAIL.getStatus());
            deviceRecord1.setCreateBy(loginUser.getUsername());
            deviceRecordList.add(deviceRecord1);
        }
        deviceRecordMapper.insertBatch(deviceRecordList);
        return result > 0 ? AjaxResult.success("回收设备成功") : AjaxResult.error("回收设备失败");
    }

    @Override
    public List<DeviceShortOutput> listTerminalUser(Device device) {
        return deviceMapper.listTerminalUser(device);
    }

    @Override
    public List<Device> listTerminalUserByGroup(Device device) {
        return deviceMapper.listTerminalUserByGroup(device);
    }

    /**
     * 根据监控设备channelId获取设备
     *
     * @param channelId
     * @return
     */
    @Override
    public Device selectDeviceByChannelId(String channelId) {
        return deviceMapper.selectDeviceByChannelId(channelId);
    }


    @Override
    public List<ThingsModelDTO> listThingsModel(Long deviceId) {
        DeviceShortOutput device = deviceMapper.selectDeviceRunningStatusByDeviceId(deviceId);
        if (null == device) {
            return new ArrayList<>();
        }
        //管理员可以下发，其他看指令控制
        int canSend = getLoginUser().getUserId().equals(getLoginUser().getDeptUserId()) ? 1 : 0;
        List<ThingsModelDTO> list = thingsModelService.getCacheAndHandleArrayByProductId(device.getProductId(), device.getSerialNumber());
        for (ThingsModelDTO thingsModelDTO : list) {
            thingsModelDTO.setProductId(device.getProductId());
            thingsModelDTO.setSerialNumber(device.getSerialNumber());
            thingsModelDTO.setIsShadow(1 == device.getIsShadow());
            thingsModelDTO.setCanSend(canSend);
        }
        SubGateway subGateway = new SubGateway();
        subGateway.setGwDeviceId(deviceId);
        List<SubDeviceListVO> subDeviceListVOList = subGatewayMapper.selectGatewayList(subGateway);
        for (SubDeviceListVO subDeviceListVO : subDeviceListVOList) {
            List<ThingsModelDTO> subList = thingsModelService.getCacheAndHandleArrayByProductId(subDeviceListVO.getSubProductId(), subDeviceListVO.getSubDeviceNo());
            for (ThingsModelDTO thingsModelDTO : subList) {
                thingsModelDTO.setProductId(subDeviceListVO.getSubProductId());
                thingsModelDTO.setSerialNumber(subDeviceListVO.getSubDeviceNo());
                thingsModelDTO.setIsShadow(1 == device.getIsShadow());
                thingsModelDTO.setCanSend(canSend);
                thingsModelDTO.setParentSerialNumber(device.getSerialNumber());
            }
            list.addAll(subList);
        }
        //指令控制
        jurisdictionThingsModel(list, deviceId);
        return list;
    }

    private void jurisdictionThingsModel(List<ThingsModelDTO> list, Long deviceId) {
        Long userId = SecurityUtils.getUserId();
        //查询是否有指令控制
        List<OrderControl> orderControls = orderControlService.selectByUserId(userId, deviceId);
        if (CollectionUtils.isEmpty(orderControls)) return;
        List<Long> modelIdList = new ArrayList<>();
        for (OrderControl control : orderControls) {
            String selectOrder = control.getSelectOrder();
            String[] split = selectOrder.split(",");
            //判断是否生效
            boolean just = just(control);
            if (just) {
                List<Long> ids = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                modelIdList.addAll(ids);
            }
        }
        if (!CollectionUtils.isEmpty(modelIdList)) {
            for (ThingsModelDTO dto : list) {
                if (modelIdList.contains(dto.getModelId())) {
                    dto.setCanSend(1);
                } else {
                    dto.setCanSend(0);
                }
            }
        }

    }

    private boolean just(OrderControl control) {
        Date startTime = control.getStartTime();
        Date endTime = control.getEndTime();
        Integer count = control.getCount();
        Date now = DateUtils.getNowDate();
        return now.after(startTime) && now.before(endTime) && count > 0;
    }

    @Override
    public void updateByOrder(Long userId, Long deviceId) {
        //处理指令权限问题 ,非管理员角色
        List<OrderControl> controlList = orderControlService.selectByUserId(userId, deviceId);
        controlList = controlList.stream().filter(control -> Objects.equals(control.getStatus(), "1")).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(controlList)) {
            //更新次数
            OrderControl control = controlList.get(0);
            control.setCount(control.getCount() - 1);
            orderControlService.updateOrderControl(control);
        }
    }

    /**
     * 根据分组id集合查询设备分组
     *
     * @param groupIds 设备分组主键
     * @return 设备分组
     */
    @Override
    public List<DeviceGroup> listDeviceGroupByGroupIds(List<Long> groupIds) {
        return deviceMapper.listDeviceGroupByGroupIds(groupIds);
    }



    /**
     * 查询产品下所有设备，返回设备编号
     *
     * @param productId 产品id
     * @return
     */
    @Override
    public List<String> selectSerialNumbersByProductId(Long productId) {
        return deviceMapper.selectSerialNumbersByProductId(productId);
    }

}
