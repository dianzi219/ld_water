package com.fastbee.controller.datacenter;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.Anonymous;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.DeviceLog;
import com.fastbee.iot.model.AlertCountVO;
import com.fastbee.iot.model.DeviceHistoryParam;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.model.scenemodel.SceneHistoryParam;
import com.fastbee.iot.service.DataCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * @author fastb
 * @version 1.0
 * @description: 数据中心控制器
 * @date 2024-06-13 14:09
 */
@Api(tags = "数据中心管理")
@RestController
@RequestMapping("/data/center")
public class DataCenterController {

    @Resource
    private DataCenterService dataCenterService;

    /**
     * 查询设备物模型的历史数据
     * @param deviceHistoryParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @ApiOperation("查询设备的历史数据")
    @PreAuthorize("@ss.hasPermi('dataCenter:history:list')")
    @PostMapping("/deviceHistory")
    public AjaxResult deviceHistory(@RequestBody DeviceHistoryParam deviceHistoryParam)
    {
        if (StringUtils.isEmpty(deviceHistoryParam.getSerialNumber())) {
            return AjaxResult.error("请选择设备");
        }
        if (StringUtils.isNotEmpty(deviceHistoryParam.getSerialNumber())) {
            deviceHistoryParam.setSerialNumber(deviceHistoryParam.getSerialNumber().toUpperCase());
        }
        List<JSONObject> jsonObject = dataCenterService.deviceHistory(deviceHistoryParam);
        return AjaxResult.success(jsonObject);
    }

    @ApiOperation("查询场景变量历史数据")
    @PreAuthorize("@ss.hasPermi('dataCenter:history:list')")
    @GetMapping("/sceneHistory")
    public AjaxResult sceneHistory(SceneHistoryParam sceneHistoryParam)
    {
        if (StringUtils.isNotEmpty(sceneHistoryParam.getSerialNumber())) {
            sceneHistoryParam.setSerialNumber(sceneHistoryParam.getSerialNumber().toUpperCase());
        }
        List<JSONObject> jsonObject = dataCenterService.sceneHistory(sceneHistoryParam);
        return AjaxResult.success(jsonObject);
    }

    /**
     * 统计告警处理信息
     * @param dataCenterParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @ApiOperation("统计告警处理信息")
    @PreAuthorize("@ss.hasPermi('dataCenter:analysis:list')")
    @GetMapping("/countAlertProcess")
    public AjaxResult countAlertProcess(DataCenterParam dataCenterParam)
    {
        Long deptUserId = getLoginUser().getUser().getDept().getDeptUserId();
        dataCenterParam.setTenantId(deptUserId);
        List<AlertCountVO> alertCountVO = dataCenterService.countAlertProcess(dataCenterParam);
        return AjaxResult.success(alertCountVO);
    }

    /**
     * 统计告警级别信息
     * @param dataCenterParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @ApiOperation("统计告警级别信息")
    @PreAuthorize("@ss.hasPermi('dataCenter:analysis:list')")
    @GetMapping("/countAlertLevel")
    public AjaxResult countAlertLevel(DataCenterParam dataCenterParam)
    {
        Long deptUserId = getLoginUser().getUser().getDept().getDeptUserId();
        dataCenterParam.setTenantId(deptUserId);
        List<AlertCountVO> alertCountVO = dataCenterService.countAlertLevel(dataCenterParam);
        return AjaxResult.success(alertCountVO);
    }

    /**
     * 统计设备物模型指令下发数量
     * @param dataCenterParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @ApiOperation("统计设备物模型指令下发数量")
    @PreAuthorize("@ss.hasPermi('dataCenter:analysis:list')")
    @GetMapping("/countThingsModelInvoke")
    public AjaxResult countThingsModelInvoke(DataCenterParam dataCenterParam)
    {
        if (StringUtils.isEmpty(dataCenterParam.getSerialNumber())) {
            return AjaxResult.error("请传入设备编号");
        }
        if (StringUtils.isNotEmpty(dataCenterParam.getSerialNumber())) {
            dataCenterParam.setSerialNumber(dataCenterParam.getSerialNumber().toUpperCase());
        }
        List<ThingsModelLogCountVO> list = dataCenterService.countThingsModelInvoke(dataCenterParam);
        return AjaxResult.success(list);
    }

}
