package com.fastbee.iot.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fastbee.common.core.domain.entity.SysDept;
import com.fastbee.common.enums.DeviceDistributeTypeEnum;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.iot.convert.DeviceRecordConvert;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.Product;
import com.fastbee.iot.mapper.DeviceMapper;
import com.fastbee.iot.mapper.ProductMapper;
import com.fastbee.iot.model.DeviceRecordVO;
import com.fastbee.system.mapper.SysDeptMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.DeviceRecordMapper;
import com.fastbee.iot.domain.DeviceRecord;
import com.fastbee.iot.service.IDeviceRecordService;

import javax.annotation.Resource;

/**
 * 设备记录Service业务层处理
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
@Service
public class DeviceRecordServiceImpl implements IDeviceRecordService
{
    @Resource
    private DeviceRecordMapper deviceRecordMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private DeviceMapper deviceMapper;

    /**
     * 查询设备记录
     *
     * @param id 设备记录主键
     * @return 设备记录
     */
    @Override
    public DeviceRecord selectDeviceRecordById(Long id)
    {
        return deviceRecordMapper.selectDeviceRecordById(id);
    }

    /**
     * 查询设备记录列表
     *
     * @param deviceRecord 设备记录
     * @return 设备记录
     */
    @Override
    public Page<DeviceRecordVO> selectDeviceRecordList(DeviceRecord deviceRecord)
    {
        LambdaQueryWrapperX<DeviceRecord> queryWrapperX = this.getListQueryWrapper(deviceRecord);
        Page<DeviceRecord> deviceRecordPage = deviceRecordMapper.selectPage(new Page<>(deviceRecord.getPageNum(), deviceRecord.getPageSize()), queryWrapperX);
//        List<DeviceRecord> deviceRecordList = deviceRecordMapper.selectDeviceRecordList(deviceRecord);
        List<DeviceRecord> records = deviceRecordPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new Page<>();
        }
        Page<DeviceRecordVO> deviceRecordVOPage = DeviceRecordConvert.INSTANCE.convertDeviceRecordVOPage(deviceRecordPage);
        List<DeviceRecordVO> deviceRecordVOList = deviceRecordVOPage.getRecords();
        // 查租户、产品、设备信息
        List<Long> deptIdList = new ArrayList<>();
        deptIdList.addAll(deviceRecordVOList.stream().map(DeviceRecordVO::getOperateDeptId).distinct().collect(Collectors.toList()));
        deptIdList.addAll(deviceRecordVOList.stream().map(DeviceRecordVO::getTargetDeptId).distinct().collect(Collectors.toList()));
        Map<Long, SysDept> deptMap = new HashMap<>(2);
        if (CollectionUtils.isNotEmpty(deptIdList)) {
            List<SysDept> sysDeptList = sysDeptMapper.selectDeptByIds(deptIdList);
            deptMap = sysDeptList.stream().collect(Collectors.toMap(SysDept::getDeptId, Function.identity(), (o, n) -> n));
        }
        Map<Long, Product> productMap = new HashMap<>(2);
        List<Long> productIdList = deviceRecordVOList.stream().map(DeviceRecordVO::getProductId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(productIdList)) {
            List<Product> productList = productMapper.selectProductListByProductIds(productIdList);
            productMap = productList.stream().collect(Collectors.toMap(Product::getProductId, Function.identity(), (o, n) -> n));
        }
        Map<Long, Device> deviceMap = new HashMap<>(2);
        List<Long> deviceIdList = deviceRecordVOList.stream().map(DeviceRecordVO::getDeviceId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deviceIdList)) {
            List<Device> deviceList = deviceMapper.selectDeviceListByDeviceIds(deviceIdList);
            deviceMap = deviceList.stream().collect(Collectors.toMap(Device::getDeviceId, Function.identity(), (o, n) -> n));
        }
        for (DeviceRecordVO deviceRecordVO : deviceRecordVOList) {
            SysDept operateDept = deptMap.get(deviceRecordVO.getOperateDeptId());
            if (null != operateDept) {
                deviceRecordVO.setOperateDeptName(operateDept.getDeptName());
            }
            SysDept targetDept = deptMap.get(deviceRecordVO.getTargetDeptId());
            if (null != targetDept) {
                deviceRecordVO.setTargetDeptName(targetDept.getDeptName());
            }
            Product product = productMap.get(deviceRecordVO.getProductId());
            if (null != product) {
                deviceRecordVO.setProductName(product.getProductName());
            }
            Device device = deviceMap.get(deviceRecordVO.getDeviceId());
            if (null != device) {
                deviceRecordVO.setDeviceName(device.getDeviceName());
            }
            if (null != deviceRecordVO.getDistributeType()) {
                deviceRecordVO.setDistributeTypeDesc(DeviceDistributeTypeEnum.getDesc(deviceRecordVO.getDistributeType()));
            }
        }
        deviceRecordVOPage.setRecords(deviceRecordVOList);
        return deviceRecordVOPage;
    }

    private LambdaQueryWrapperX<DeviceRecord> getListQueryWrapper(DeviceRecord deviceRecord) {
        LambdaQueryWrapperX<DeviceRecord> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eqIfPresent(DeviceRecord::getType, deviceRecord.getType());
        queryWrapperX.eqIfPresent(DeviceRecord::getProductId, deviceRecord.getProductId());
        queryWrapperX.eqIfPresent(DeviceRecord::getStatus, deviceRecord.getStatus());
        queryWrapperX.betweenIfPresent(DeviceRecord::getCreateTime, deviceRecord.getParams().get("beginTime"), deviceRecord.getParams().get("endTime"));
        queryWrapperX.eqIfPresent(DeviceRecord::getOperateDeptId, deviceRecord.getOperateDeptId());
        queryWrapperX.eqIfPresent(DeviceRecord::getSerialNumber, deviceRecord.getSerialNumber());
        return queryWrapperX;
    }

    /**
     * 新增设备记录
     *
     * @param deviceRecord 设备记录
     * @return 结果
     */
    @Override
    public int insertDeviceRecord(DeviceRecord deviceRecord)
    {
        deviceRecord.setCreateTime(DateUtils.getNowDate());
        return deviceRecordMapper.insertDeviceRecord(deviceRecord);
    }

    /**
     * 修改设备记录
     *
     * @param deviceRecord 设备记录
     * @return 结果
     */
    @Override
    public int updateDeviceRecord(DeviceRecord deviceRecord)
    {
        deviceRecord.setUpdateTime(DateUtils.getNowDate());
        return deviceRecordMapper.updateDeviceRecord(deviceRecord);
    }

    /**
     * 批量删除设备记录
     *
     * @param ids 需要删除的设备记录主键
     * @return 结果
     */
    @Override
    public int deleteDeviceRecordByIds(Long[] ids)
    {
        return deviceRecordMapper.deleteDeviceRecordByIds(ids);
    }

    /**
     * 删除设备记录信息
     *
     * @param id 设备记录主键
     * @return 结果
     */
    @Override
    public int deleteDeviceRecordById(Long id)
    {
        return deviceRecordMapper.deleteDeviceRecordById(id);
    }
}
