package com.fastbee.iot.service.impl;

import com.fastbee.common.core.mq.SubDeviceBo;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.iot.domain.ModbusParams;
import com.fastbee.iot.domain.SubGateway;
import com.fastbee.iot.mapper.SubGatewayMapper;
import com.fastbee.iot.model.gateWay.GateSubDeviceVO;
import com.fastbee.iot.model.gateWay.SubDeviceAddVO;
import com.fastbee.iot.model.gateWay.SubDeviceListVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IModbusParamsService;
import com.fastbee.iot.service.ISubGatewayService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 网关与子设备关联Service业务层处理
 *
 * @author gsb
 * @date 2024-05-27
 */
@Service
public class SubGatewayServiceImpl implements ISubGatewayService
{
    @Resource
    private SubGatewayMapper gatewayMapper;
    @Resource
    private IModbusParamsService modbusParamsService;

    /**
     * 查询网关与子设备关联
     *
     * @param id 网关与子设备关联主键
     * @return 网关与子设备关联
     */
    @Override
    public SubGateway selectGatewayById(Long id)
    {
        return gatewayMapper.selectGatewayById(id);
    }

    /**
     * 查询网关与子设备关联列表
     *
     * @param gateway 网关与子设备关联
     * @return 网关与子设备关联
     */
    @Override
    public List<SubDeviceListVO> selectGatewayList(SubGateway gateway)
    {
        return gatewayMapper.selectGatewayList(gateway);
    }

    /**
     * 新增网关与子设备关联
     *
     * @param gateway 网关与子设备关联
     * @return 结果
     */
    @Override
    public int insertGateway(SubGateway gateway)
    {
        gateway.setCreateTime(DateUtils.getNowDate());
        return gatewayMapper.insertGateway(gateway);
    }

    /**
     * 修改网关与子设备关联
     *
     * @param gateway 网关与子设备关联
     * @return 结果
     */
    @Override
    public int updateGateway(SubGateway gateway)
    {
        gateway.setUpdateTime(DateUtils.getNowDate());
        return gatewayMapper.updateGateway(gateway);
    }

    /**
     * 批量删除网关与子设备关联
     *
     * @param ids 需要删除的网关与子设备关联主键
     * @return 结果
     */
    @Override
    public int deleteGatewayByIds(Long[] ids)
    {
        return gatewayMapper.deleteGatewayByIds(ids);
    }

    /**
     * 删除网关与子设备关联信息
     *
     * @param id 网关与子设备关联主键
     * @return 结果
     */
    @Override
    public int deleteGatewayById(Long id)
    {
        return gatewayMapper.deleteGatewayById(id);
    }

    /**
     * 获取可选的网关子设备列表
     * @return
     */
    @Override
    public List<GateSubDeviceVO> getIsSelectGateSubDevice(GateSubDeviceVO subDeviceVO){
        return gatewayMapper.getIsSelectGateSubDevice(subDeviceVO);
    }

    /**
     * 批量添加子设备
     * @param subDeviceAddVO
     * @return
     */
    @Override
    public int insertSubDeviceBatch(SubDeviceAddVO subDeviceAddVO){
        Long gwDeviceId = subDeviceAddVO.getGwDeviceId();
        Long[] subDeviceIds = subDeviceAddVO.getSubDeviceIds();
        List<SubGateway> list = new ArrayList<>();
        if (subDeviceIds.length >0){
            for (Long subDeviceId : subDeviceIds) {
                ModbusParams modbusParams = modbusParamsService.getModbusParamsByDeviceId(subDeviceId);
                SubGateway subGateway = new SubGateway();
                subGateway.setSubDeviceId(subDeviceId);
                subGateway.setGwDeviceId(gwDeviceId);
                if (!Objects.isNull(modbusParams)) {
                    subGateway.setSlaveId(modbusParams.getSlaveId());
                }
                subGateway.setCreateBy(SecurityUtils.getUsername());
                list.add(subGateway);
            }
        }
        return gatewayMapper.insertSubDeviceBatch(list);
    }

    /**
     * 批量更新子设备
     * @param list
     * @return
     */
    @Override
    public void updateSubDeviceBatch(List<SubGateway> list){
        assert !CollectionUtils.isEmpty(list) : "集合为空";
        for (SubGateway gateway : list) {
            this.updateGateway(gateway);
        }
    }

    /**
     * 根据网关设备编号查询子设备列表
     * @param gwSerialNumber
     * @return
     */
    @Override
    public List<SubDeviceBo> getSubDeviceListByGw(String gwSerialNumber){
        return gatewayMapper.getSubDeviceListByGw(gwSerialNumber);
    }
}
