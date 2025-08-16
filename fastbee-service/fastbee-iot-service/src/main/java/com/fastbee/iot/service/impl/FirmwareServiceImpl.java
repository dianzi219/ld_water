package com.fastbee.iot.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.FirmwareTask;
import com.fastbee.iot.domain.FirmwareTaskDetail;
import com.fastbee.iot.mapper.FirmwareTaskDetailMapper;
import com.fastbee.iot.mapper.FirmwareTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.fastbee.iot.mapper.FirmwareMapper;
import com.fastbee.iot.domain.Firmware;
import com.fastbee.iot.service.IFirmwareService;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 产品固件Service业务层处理
 *
 * @author kerwincui
 * @date 2024-08-18
 */
@Service
public class FirmwareServiceImpl extends ServiceImpl<FirmwareMapper,Firmware> implements IFirmwareService {

    @Resource
    private FirmwareMapper firmwareMapper;
    @Resource
    private FirmwareTaskMapper firmwareTaskMapper;
    @Resource
    private FirmwareTaskDetailMapper firmwareTaskDetailMapper;


    /**
     * 查询产品固件列表
     *
     * @param firmware 产品固件
     * @return 产品固件
     */
    @Override
    public List<Firmware> selectFirmwareList(Firmware firmware) {
        LambdaQueryWrapper<Firmware> lqw = buildQueryWrapper(firmware);
        lqw.orderByDesc(Firmware::getCreateTime);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<Firmware> buildQueryWrapper(Firmware query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<Firmware> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getFirmwareName()), Firmware::getFirmwareName, query.getFirmwareName());
        lqw.eq(query.getFirmwareType() != null, Firmware::getFirmwareType, query.getFirmwareType());
        lqw.eq(query.getProductId() != null, Firmware::getProductId, query.getProductId());
        lqw.like(StringUtils.isNotBlank(query.getProductName()), Firmware::getProductName, query.getProductName());
        lqw.eq(query.getTenantId() != null, Firmware::getTenantId, query.getTenantId());
        lqw.like(StringUtils.isNotBlank(query.getTenantName()), Firmware::getTenantName, query.getTenantName());
        lqw.eq(query.getIsSys() != null, Firmware::getIsSys, query.getIsSys());
        lqw.eq(query.getIsLatest() != null, Firmware::getIsLatest, query.getIsLatest());
        lqw.eq(query.getVersion() != null, Firmware::getVersion, query.getVersion());
        lqw.eq(StringUtils.isNotBlank(query.getFilePath()), Firmware::getFilePath, query.getFilePath());
        lqw.eq(query.getByteSize() != null, Firmware::getByteSize, query.getByteSize());

        if (!Objects.isNull(params.get("beginTime")) &&
                !Objects.isNull(params.get("endTime"))) {
            lqw.between(Firmware::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }


    /**
     * 查询待升级固件版本列表
     * @param firmware
     * @return
     */
    @Override
    public List<Firmware> selectUpGradeVersionList(Firmware firmware){
        return firmwareMapper.selectUpGradeVersionList(firmware);
    }

    /**
     * 新增产品固件
     *
     * @param firmware 产品固件
     * @return 结果
     */
    @Override
    public int insertFirmware(Firmware firmware){
        SysUser user = getLoginUser().getUser();
        firmware.setTenantId(user.getUserId());
        firmware.setTenantName(user.getUserName());
        firmware.setCreateTime(DateUtils.getNowDate());
        return firmwareMapper.insert(firmware);
    }


    /**
     * 查询设备最新固件
     * @param deviceId 产品固件主键
     * @param firmwareType 固件类型
     * @return 产品固件
     */
    @Override
    public Firmware selectLatestFirmware(Long deviceId, Long firmwareType){
        return firmwareMapper.selectLatestFirmware(deviceId, firmwareType);
    }

    @Override
    public int deleteBatchByIds(List<Long> firmwareIdList) {
        int i = firmwareMapper.deleteBatchIds(firmwareIdList);
        if (i > 0) {
            LambdaQueryWrapper<FirmwareTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(FirmwareTask::getFirmwareId, firmwareIdList);
            List<FirmwareTask> firmwareTaskList = firmwareTaskMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(firmwareTaskList)) {
                return i;
            }
            List<Long> taskIdList = firmwareTaskList.stream().map(FirmwareTask::getId).collect(Collectors.toList());
            firmwareTaskMapper.deleteBatchIds(taskIdList);
            LambdaQueryWrapper<FirmwareTaskDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(FirmwareTaskDetail::getTaskId, taskIdList);
            firmwareTaskDetailMapper.delete(queryWrapper1);
        }
        return i;
    }

}
