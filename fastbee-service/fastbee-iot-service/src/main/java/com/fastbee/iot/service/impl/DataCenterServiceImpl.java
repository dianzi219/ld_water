package com.fastbee.iot.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.SceneModelTagCache;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.model.AlertCountVO;
import com.fastbee.iot.model.DeviceHistoryParam;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.model.scenemodel.SceneHistoryParam;
import com.fastbee.iot.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fastb
 * @version 1.0
 * @description: 数据中心服务类
 * @date 2024-06-13 15:29
 */
@Service
public class DataCenterServiceImpl implements DataCenterService {

    @Resource
    private IDeviceLogService deviceLogService;
    @Resource
    private IFunctionLogService functionLogService;
    @Resource
    private IEventLogService eventLogService;
    @Resource
    private IDeviceService deviceService;
    @Resource
    private ISceneModelDataService sceneModelDataService;
    @Resource
    private IThingsModelService thingsModelService;
    @Resource
    private SceneModelTagCache sceneModelTagCache;
    @Resource
    private IAlertLogService alertLogService;

    @Override
    public List<JSONObject> deviceHistory(DeviceHistoryParam deviceHistoryParam) {
        List<JSONObject> resultList = new ArrayList<>();
        List<DeviceHistoryParam.IdentifierVO> identifierVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(deviceHistoryParam.getIdentifierList())) {
            List<ThingsModelDTO> list = deviceService.listThingsModel(deviceHistoryParam.getDeviceId());
            for (ThingsModelDTO thingsModelDTO : list) {
                DeviceHistoryParam.IdentifierVO identifierVO = new DeviceHistoryParam.IdentifierVO();
                identifierVO.setIdentifier(thingsModelDTO.getIdentifier());
                identifierVO.setType(thingsModelDTO.getType());
                identifierVOList.add(identifierVO);
            }
        } else {
            identifierVOList = deviceHistoryParam.getIdentifierList();
        }
        if (CollectionUtils.isEmpty(identifierVOList)) {
            return resultList;
        }
        List<String> identifierList = identifierVOList.stream().map(DeviceHistoryParam.IdentifierVO::getIdentifier).collect(Collectors.toList());
        List<HistoryModel> historyModelList = new ArrayList<>();
        List<String> propertyIdentifierList = identifierVOList.stream().filter(t -> 1 == t.getType()).map(DeviceHistoryParam.IdentifierVO::getIdentifier).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(propertyIdentifierList)) {
            DeviceLog deviceLog = new DeviceLog();
            deviceLog.setIdentityList(propertyIdentifierList);
            deviceLog.setSerialNumber(deviceHistoryParam.getSerialNumber());
            deviceLog.setBeginTime(deviceHistoryParam.getBeginTime());
            deviceLog.setEndTime(deviceHistoryParam.getEndTime());
            List<HistoryModel> historyModelList1 = deviceLogService.listHistory(deviceLog);
            historyModelList.addAll(historyModelList1);
        }
        List<String> functionIdentifierList = identifierVOList.stream().filter(t -> 2 == t.getType()).map(DeviceHistoryParam.IdentifierVO::getIdentifier).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(functionIdentifierList)) {
            FunctionLog functionLog = new FunctionLog();
            functionLog.setIdentifyList(functionIdentifierList);
            functionLog.setSerialNumber(deviceHistoryParam.getSerialNumber());
            functionLog.setBeginTime(DateUtils.dateTime(DateUtils.YY_MM_DD_HH_MM_SS, deviceHistoryParam.getBeginTime()));
            functionLog.setEndTime(DateUtils.dateTime(DateUtils.YY_MM_DD_HH_MM_SS, deviceHistoryParam.getEndTime()));
            historyModelList.addAll(functionLogService.listHistory(functionLog));
        }
        List<String> eventIdentifierList = identifierVOList.stream().filter(t -> 3 == t.getType()).map(DeviceHistoryParam.IdentifierVO::getIdentifier).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(eventIdentifierList)) {
            EventLog eventLog = new EventLog();
            eventLog.setIdentityList(eventIdentifierList);
            eventLog.setSerialNumber(deviceHistoryParam.getSerialNumber());
            Map<String, Object> params = new HashMap<>(2);
            params.put("beginTime", deviceHistoryParam.getBeginTime());
            params.put("endTime", deviceHistoryParam.getEndTime());
            eventLog.setParams(params);
            historyModelList.addAll(eventLogService.listHistory(eventLog));
        }
        historyModelList.sort(Comparator.comparing(HistoryModel::getTime));
        return this.handleData(identifierList, historyModelList);
    }

    private List<JSONObject> handleData(List<String> identifierList, List<HistoryModel> historyModelList) {
        List<JSONObject> resultList = new ArrayList<>();
        LinkedHashMap<Date, List<HistoryModel>> map = historyModelList.stream()
                .collect(Collectors.groupingBy(HistoryModel::getTime, LinkedHashMap::new, Collectors.toList()));
//        Map<String, HistoryModel> oldHistoryModelMap = new HashMap<>(2);
        for (Map.Entry<Date, List<HistoryModel>> entry : map.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            List<HistoryModel> value = entry.getValue();
            Map<String, HistoryModel> historyModelMap = value.stream().collect(Collectors.toMap(HistoryModel::getIdentity, Function.identity(), (o, n) -> n));
            List<JSONObject> jsonObjectList = new ArrayList<>();
            for (String identifier : identifierList) {
                JSONObject jsonObject1 = new JSONObject();
                HistoryModel historyModel = historyModelMap.get(identifier);
                if (null != historyModel) {
//                    oldHistoryModelMap.put(identifier, historyModel);
                    jsonObject1.put(historyModel.getIdentity(), historyModel.getValue());
                } else {
//                    HistoryModel oldHistoryModel = oldHistoryModelMap.get(identifier);
//                    if (null != oldHistoryModel) {
//                        jsonObject1.put(identifier, oldHistoryModel.getValue());
//                    } else {
//                        jsonObject1.put(identifier, null);
//                    }
                    jsonObject1.put(identifier, null);
                }
                jsonObjectList.add(jsonObject1);
            }
            jsonObject.put(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, entry.getKey()), jsonObjectList);
            resultList.add(jsonObject);
        }
        return resultList;
    }

    @Override
    public List<JSONObject> sceneHistory(SceneHistoryParam sceneHistoryParam) {
        if (SceneModelVariableTypeEnum.INPUT_VARIABLE.getType().equals(sceneHistoryParam.getVariableType())) {
            return new ArrayList<>();
        }
        List<String> sceneModelDataIdList = StringUtils.str2List(sceneHistoryParam.getIds(), ",", true, true);
        List<SceneModelData> sceneModelDataList = sceneModelDataService.selectSceneModelDataListByIds(sceneModelDataIdList.stream().map(Long::parseLong).collect(Collectors.toList()));
        List<Long> datasourceIdList = sceneModelDataList.stream().map(SceneModelData::getDatasourceId).distinct().collect(Collectors.toList());
        if (SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneHistoryParam.getVariableType())) {
            List<ThingsModel> thingsModelList = thingsModelService.selectThingsModelListByModelIds(datasourceIdList);
            Map<Long, Integer> thingsModelMap = thingsModelList.stream().collect(Collectors.toMap(ThingsModel::getModelId, ThingsModel::getType));
            DeviceHistoryParam deviceHistoryParam = new DeviceHistoryParam();
            List<DeviceHistoryParam.IdentifierVO> identifierVOList = new ArrayList<>();
            for (SceneModelData sceneModelData : sceneModelDataList) {
                Integer type = thingsModelMap.get(sceneModelData.getDatasourceId());
                if (null != type) {
                    DeviceHistoryParam.IdentifierVO identifierVO = new DeviceHistoryParam.IdentifierVO();
                    identifierVO.setIdentifier(sceneModelData.getIdentifier());
                    identifierVO.setType(type);
                    identifierVOList.add(identifierVO);
                }
            }
            deviceHistoryParam.setSerialNumber(sceneHistoryParam.getSerialNumber());
            deviceHistoryParam.setIdentifierList(identifierVOList);
            deviceHistoryParam.setBeginTime(sceneHistoryParam.getBeginTime());
            deviceHistoryParam.setEndTime(sceneHistoryParam.getEndTime());
            return this.deviceHistory(deviceHistoryParam);
        }
        List<String> identifierList = datasourceIdList.stream().map(x -> x + "").collect(Collectors.toList());
        DeviceLog deviceLog = new DeviceLog();
        deviceLog.setIdentityList(identifierList);
        deviceLog.setLogType(7);
        deviceLog.setBeginTime(sceneHistoryParam.getBeginTime());
        deviceLog.setEndTime(sceneHistoryParam.getEndTime());
        List<HistoryModel> historyModelList = deviceLogService.listHistory(deviceLog);
        return this.handleData(identifierList, historyModelList);
    }

    @Override
    public List<AlertCountVO> countAlertProcess(DataCenterParam dataCenterParam) {
        return alertLogService.countAlertProcess(dataCenterParam);
    }

    @Override
    public List<AlertCountVO> countAlertLevel(DataCenterParam dataCenterParam) {
        return alertLogService.countAlertLevel(dataCenterParam);
    }

    @Override
    public List<ThingsModelLogCountVO> countThingsModelInvoke(DataCenterParam dataCenterParam) {
        List<ThingsModelLogCountVO> resultList = new ArrayList<>();
        resultList.addAll(deviceLogService.countThingsModelInvoke(dataCenterParam));
        resultList.addAll(functionLogService.countThingsModelInvoke(dataCenterParam));
        return resultList;
    }
}
