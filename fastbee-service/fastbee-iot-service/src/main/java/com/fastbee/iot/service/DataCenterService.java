package com.fastbee.iot.service;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.iot.model.AlertCountVO;
import com.fastbee.iot.model.DeviceHistoryParam;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.model.scenemodel.SceneHistoryParam;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据中心服务类
 * @author fastb
 * @date 2024-06-13 15:29
 * @version 1.0
 */
public interface DataCenterService {

    /**
     * 查询设备物模型的历史数据
     * @param deviceHistoryParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    List<JSONObject> deviceHistory(DeviceHistoryParam deviceHistoryParam);

    /**
     * 查询场景变量的历史数据
     * @param sceneHistoryParam 传参
     * @return java.util.List<com.alibaba.fastjson2.JSONObject>
     */
    List<JSONObject> sceneHistory(SceneHistoryParam sceneHistoryParam);

    /**
     * 统计告警处理信息
     * @param dataCenterParam 传参
     * @return com.fastbee.iot.model.AlertCountVO
     */
    List<AlertCountVO> countAlertProcess(DataCenterParam dataCenterParam);

    /**
     * 统计告警级别信息
     * @param dataCenterParam 传参
     * @return com.fastbee.iot.model.AlertCountVO
     */
    List<AlertCountVO> countAlertLevel(DataCenterParam dataCenterParam);

    /**
     * 统计设备物模型指令下发数量
     * @param dataCenterParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    List<ThingsModelLogCountVO> countThingsModelInvoke(DataCenterParam dataCenterParam);
}
