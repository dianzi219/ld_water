package com.fastbee.iot.data.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fastbee.common.enums.OTAUpgrade;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.IOtaTaskCache;
import com.fastbee.iot.model.FirmwareTaskDetailInput;
import com.fastbee.iot.model.FirmwareTaskDetailOutput;
import com.fastbee.iot.model.FirmwareTaskDeviceStatistic;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.fastbee.iot.mapper.FirmwareTaskDetailMapper;
import com.fastbee.iot.domain.FirmwareTaskDetail;
import com.fastbee.iot.data.service.IFirmwareTaskDetailService;

/**
 * 固件升级任务详细对象Service业务层处理
 *
 * @author kerwincui
 * @date 2024-08-18
 */
@Service
public class FirmwareTaskDetailServiceImpl extends ServiceImpl<FirmwareTaskDetailMapper,FirmwareTaskDetail> implements IFirmwareTaskDetailService {

    @Resource
    private FirmwareTaskDetailMapper firmwareTaskDetailMapper;

    @Resource
    private IOtaTaskCache otaTaskCache;

    /**
     * 查询固件升级任务详细对象列表
     *
     * @param firmwareTaskDetail 固件升级任务详细对象
     * @return 固件升级任务详细对象
     */
    @Override
    public List<FirmwareTaskDetail> selectFirmwareTaskDetailList(FirmwareTaskDetail firmwareTaskDetail) {
        LambdaQueryWrapper<FirmwareTaskDetail> lqw = buildQueryWrapper(firmwareTaskDetail);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<FirmwareTaskDetail> buildQueryWrapper(FirmwareTaskDetail query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<FirmwareTaskDetail> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getTaskId() != null, FirmwareTaskDetail::getTaskId, query.getTaskId());
        lqw.eq(StringUtils.isNotBlank(query.getSerialNumber()), FirmwareTaskDetail::getSerialNumber, query.getSerialNumber());
        lqw.eq(query.getUpgradeStatus() != null, FirmwareTaskDetail::getUpgradeStatus, query.getUpgradeStatus());
        lqw.eq(StringUtils.isNotBlank(query.getDetailMsg()), FirmwareTaskDetail::getDetailMsg, query.getDetailMsg());
        lqw.eq(StringUtils.isNotBlank(query.getMessageId()), FirmwareTaskDetail::getMessageId, query.getMessageId());

        if (!Objects.isNull(params.get("beginTime")) &&
                !Objects.isNull(params.get("endTime"))) {
            lqw.between(FirmwareTaskDetail::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }

    /**
     * 查询固件升级任务详细
     * @param taskId
     * @param serialNumber
     * @return
     */
    public FirmwareTaskDetail selectFirmwareTaskDetailByTaskIdAndSerialNumber(Long taskId, String serialNumber){
        LambdaQueryWrapper<FirmwareTaskDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FirmwareTaskDetail::getId,taskId);
        wrapper.eq(FirmwareTaskDetail::getSerialNumber,serialNumber);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 更新升级状态
     * @param taskId
     * @param serialNumber
     * @param otaUpgrade
     */
    @Override
    public void updateStatus(Long taskId, String serialNumber, OTAUpgrade otaUpgrade) {
        FirmwareTaskDetail taskDetail = new FirmwareTaskDetail();
        taskDetail.setUpgradeStatus(otaUpgrade.getStatus());
        taskDetail.setDetailMsg(otaUpgrade.getDes());
        taskDetail.setUpdateTime(DateUtils.getNowDate());
        QueryWrapper<FirmwareTaskDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.eq("serial_number", serialNumber);
        baseMapper.update(taskDetail, wrapper);
    }

    /**
     * 根据固件id查询下属设备列表
     * @param firmwareTaskDetailInput
     * @return
     */
    @Override
    public List<FirmwareTaskDetailOutput> selectFirmwareTaskDetailListByFirmwareId(FirmwareTaskDetailInput firmwareTaskDetailInput) {
        List<FirmwareTaskDetailOutput> list = firmwareTaskDetailMapper.selectFirmwareTaskDetailListByFirmwareId(firmwareTaskDetailInput);
        // 传入升级进度
        for (FirmwareTaskDetailOutput output : list) {
            String statusCache = otaTaskCache.getOtaCacheValue(output.getSerialNumber(), "status");
            if (output.getUpgradeStatus() == 1 && StringUtils.isNotEmpty(statusCache)) {
                output.setProgress(Integer.parseInt(statusCache));
            }
            else if (output.getUpgradeStatus() == 2 || output.getUpgradeStatus() == 3) {
                output.setProgress(100);
            } else {
                output.setProgress(0);
            }
        }
        return list;
    }

    /**
     * 固件升级设备统计
     * @param firmwareTaskDetailInput
     * @return
     */
    @Override
    public List<FirmwareTaskDeviceStatistic> deviceStatistic(FirmwareTaskDetailInput firmwareTaskDetailInput) {
        return firmwareTaskDetailMapper.deviceStatistic(firmwareTaskDetailInput);
    }

}
