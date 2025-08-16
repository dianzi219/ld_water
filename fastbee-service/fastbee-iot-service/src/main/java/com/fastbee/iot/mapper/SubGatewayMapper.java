package com.fastbee.iot.mapper;

import com.fastbee.common.core.mq.SubDeviceBo;
import com.fastbee.iot.domain.SubGateway;
import com.fastbee.iot.model.gateWay.GateSubDeviceVO;
import com.fastbee.iot.model.gateWay.SubDeviceAddVO;
import com.fastbee.iot.model.gateWay.SubDeviceListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网关与子设备关联Mapper接口
 *
 * @author gsb
 * @date 2024-05-27
 */
public interface SubGatewayMapper
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
     * 删除网关与子设备关联
     *
     * @param id 网关与子设备关联主键
     * @return 结果
     */
    public int deleteGatewayById(Long id);

    /**
     * 批量删除网关与子设备关联
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGatewayByIds(Long[] ids);

    /**
     * 获取可选的网关子设备列表
     * @return
     */
    List<GateSubDeviceVO> getIsSelectGateSubDevice(GateSubDeviceVO subDeviceVO);

    /**
     * 批量添加子设备
     * @param deviceList
     * @return
     */
    int insertSubDeviceBatch(@Param("deviceList") List<SubGateway> deviceList);


    /**
     * 批量更新子设备
     * @param list
     * @return
     */
    int updateSubDeviceBatch(List<SubGateway> list);

    /**
     * 根据网关设备编号查询子设备列表
     * @param gwSerialNumber
     * @return
     */
    List<SubDeviceBo> getSubDeviceListByGw(String gwSerialNumber);

    /**
     * @description: 查询子设备地址
     * @param: subDeviceId 子设备id
     * @return: java.lang.Integer
     */
    Integer selectSlaveIdBySubDeviceId(Long subDeviceId);

    /**
     * 删除网关与子设备绑定关系
     * @param gwDeviceId
     */
    void deleteGatewayByGwDeviceId(Long gwDeviceId);
}
