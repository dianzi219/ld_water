package com.fastbee.iot.service;

import com.fastbee.common.core.mq.SubDeviceBo;
import com.fastbee.iot.domain.SubGateway;
import com.fastbee.iot.model.gateWay.GateSubDeviceVO;
import com.fastbee.iot.model.gateWay.SubDeviceAddVO;
import com.fastbee.iot.model.gateWay.SubDeviceListVO;

import java.util.List;

/**
 * 网关与子设备关联Service接口
 *
 * @author gsb
 * @date 2024-05-27
 */
public interface ISubGatewayService
{
    /**
     * 查询网关与子设备关联
     *
     * @param id 网关与子设备关联主键
     * @return 网关与子设备关联
     */
    public SubGateway selectGatewayById(Long id);

    /**
     * 查询网关与子设备关联列表
     *
     * @param gateway 网关与子设备关联
     * @return 网关与子设备关联集合
     */
    public List<SubDeviceListVO> selectGatewayList(SubGateway gateway);

    /**
     * 新增网关与子设备关联
     *
     * @param gateway 网关与子设备关联
     * @return 结果
     */
    public int insertGateway(SubGateway gateway);

    /**
     * 修改网关与子设备关联
     *
     * @param gateway 网关与子设备关联
     * @return 结果
     */
    public int updateGateway(SubGateway gateway);

    /**
     * 批量删除网关与子设备关联
     *
     * @param ids 需要删除的网关与子设备关联主键集合
     * @return 结果
     */
    public int deleteGatewayByIds(Long[] ids);

    /**
     * 删除网关与子设备关联信息
     *
     * @param id 网关与子设备关联主键
     * @return 结果
     */
    public int deleteGatewayById(Long id);

    /**
     * 获取可选的网关子设备列表
     * @return
     */
    List<GateSubDeviceVO> getIsSelectGateSubDevice(GateSubDeviceVO subDeviceVO);

    /**
     * 批量添加子设备
     * @param subDeviceAddVO
     * @return
     */
    int insertSubDeviceBatch(SubDeviceAddVO subDeviceAddVO);

    /**
     * 批量更新子设备
     * @param list
     * @return
     */
    void updateSubDeviceBatch(List<SubGateway> list);

    /**
     * 根据网关设备编号查询子设备列表
     * @param gwSerialNumber
     * @return
     */
    List<SubDeviceBo> getSubDeviceListByGw(String gwSerialNumber);
}
