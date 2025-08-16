package com.fastbee.iot.service.impl;

import java.util.List;

import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.model.AlertCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.model.DeviceAlertCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.AlertLogMapper;
import com.fastbee.iot.domain.AlertLog;
import com.fastbee.iot.service.IAlertLogService;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 设备告警Service业务层处理
 *
 * @author kerwincui
 * @date 2022-01-13
 */
@Service
public class AlertLogServiceImpl implements IAlertLogService {
    @Autowired
    private AlertLogMapper alertLogMapper;

    /**
     * 查询设备告警
     *
     * @param alertLogId 设备告警主键
     * @return 设备告警
     */
    @Override
    public AlertLog selectAlertLogByAlertLogId(Long alertLogId) {
        return alertLogMapper.selectAlertLogByAlertLogId(alertLogId);
    }

    /**
     * 查询设备告警列表
     *
     * @param alertLog 设备告警
     * @return 设备告警
     */
    @Override
    public List<AlertLog> selectAlertLogList(AlertLog alertLog) {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles = user.getRoles();
//        for (int i = 0; i < roles.size(); i++) {
//            // 租户和用户，只查看自己分组
//            if (roles.get(i).getRoleKey().equals("tenant") || roles.get(i).getRoleKey().equals("general")) {
//                alertLog.setUserId(user.getUserId());
//                break;
//            }
//        }
        // 查询所属机构
        // 兼容组态多租户分享
        if (null != alertLog.getDeptUserId()) {
            alertLog.setUserId(alertLog.getDeptUserId());
        } else {
            if (null != user.getDeptId()) {
                alertLog.setUserId(user.getDept().getDeptUserId());
            } else {
                alertLog.setUserId(user.getUserId());
            }
        }
        return alertLogMapper.selectAlertLogList(alertLog);
    }

    @Override
    public List<AlertLog> selectAlertLogListByCreateBy(String createBy, String remark, Integer status) {
        AlertLog alertLog = new AlertLog();
        alertLog.setCreateBy(createBy);
        alertLog.setStatus(status);
        alertLog.setRemark(remark);
        return alertLogMapper.selectAlertLogList(alertLog);
    }

    /**
     * 查询设备告警列表
     *
     * @param alertLog 设备告警
     * @return 设备告警
     */
    @Override
    public Long selectAlertLogListCount(AlertLog alertLog) {
//        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles = user.getRoles();
//        for (int i = 0; i < roles.size(); i++) {
//            // 租户和用户，只查看自己分组
//            if (roles.get(i).getRoleKey().equals("tenant") || roles.get(i).getRoleKey().equals("general")) {
//                alertLog.setUserId(user.getUserId());
//                break;
//            }
//        }
        return alertLogMapper.selectAlertLogListCount(alertLog);
    }

    @Override
    public List<DeviceAlertCount> selectDeviceAlertCount() {
        return alertLogMapper.selectDeviceAlertCount();
    }

    @Override
    public DeviceAlertCount selectDeviceAlertCountBySN(String serialNumber) {
        return alertLogMapper.selectDeviceAlertCountBySN(serialNumber);
    }

    @Override
    public List<DeviceAlertCount> selectSceneAlertCount() {
        return alertLogMapper.selectSceneAlertCount();
    }

    @Override
    public DeviceAlertCount selectSceneAlertCountBySceneId(String sceneId) {
        return alertLogMapper.selectSceneAlertCountBySceneId(sceneId);
    }

    /**
     * 新增设备告警
     *
     * @param alertLog 设备告警
     * @return 结果
     */
    @Override
    public int insertAlertLog(AlertLog alertLog) {
        alertLog.setCreateTime(DateUtils.getNowDate());
        return alertLogMapper.insertAlertLog(alertLog);
    }

    @Override
    public int insertAlertLogBatch(List<AlertLog> alertLogList) {
        return alertLogMapper.insertAlertLogBatch(alertLogList);
    }

    /**
     * 修改设备告警
     *
     * @param alertLog 设备告警
     * @return 结果
     */
    @Override
    public int updateAlertLog(AlertLog alertLog) {
        alertLog.setUpdateTime(DateUtils.getNowDate());
        if (alertLog.getRemark().length() > 0) {
            // 1=不需要处理,2=未处理,3=已处理
            alertLog.setStatus(3);
        }
        return alertLogMapper.updateAlertLog(alertLog);
    }

    @Override
    public int updateAlertLogStatus(AlertLog alertLog) {
        alertLog.setUpdateTime(DateUtils.getNowDate());
        return alertLogMapper.updateAlertLogStatus(alertLog);
    }

    /**
     * 批量删除设备告警
     *
     * @param alertLogIds 需要删除的设备告警主键
     * @return 结果
     */
    @Override
    public int deleteAlertLogByAlertLogIds(Long[] alertLogIds) {
        return alertLogMapper.deleteAlertLogByAlertLogIds(alertLogIds);
    }

    /**
     * 删除设备告警信息
     *
     * @param alertLogId 设备告警主键
     * @return 结果
     */
    @Override
    public int deleteAlertLogByAlertLogId(Long alertLogId) {
        return alertLogMapper.deleteAlertLogByAlertLogId(alertLogId);
    }

    /**
     * 通过设备编号删除设备告警信息
     *
     * @param SerialNumber 设备告警主键
     * @return 结果
     */
    @Override
    public int deleteAlertLogBySerialNumber(String SerialNumber) {
        return alertLogMapper.deleteAlertLogBySerialNumber(SerialNumber);
    }
    @Override
    public List<AlertCountVO> countAlertProcess(DataCenterParam dataCenterParam) {
        return alertLogMapper.countAlertProcess(dataCenterParam);
    }

    @Override
    public List<AlertCountVO> countAlertLevel(DataCenterParam dataCenterParam) {
        return alertLogMapper.countAlertLevel(dataCenterParam);
    }
}
