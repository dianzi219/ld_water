package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.OrderControl;
import org.apache.ibatis.annotations.Param;

/**
 * 指令权限控制Mapper接口
 *
 * @author kerwincui
 * @date 2024-07-01
 */
public interface OrderControlMapper
{
    /**
     * 查询指令权限控制
     *
     * @param id 指令权限控制主键
     * @return 指令权限控制
     */
    public OrderControl selectOrderControlById(Long id);

    /**
     * 查询指令权限控制列表
     *
     * @param orderControl 指令权限控制
     * @return 指令权限控制集合
     */
    public List<OrderControl> selectOrderControlList(OrderControl orderControl);

    /**
     * 新增指令权限控制
     *
     * @param orderControl 指令权限控制
     * @return 结果
     */
    public int insertOrderControl(OrderControl orderControl);

    /**
     * 修改指令权限控制
     *
     * @param orderControl 指令权限控制
     * @return 结果
     */
    public int updateOrderControl(OrderControl orderControl);

    /**
     * 删除指令权限控制
     *
     * @param id 指令权限控制主键
     * @return 结果
     */
    public int deleteOrderControlById(Long id);

    /**
     * 批量删除指令权限控制
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderControlByIds(Long[] ids);

    /**
     * 根据用户id获取
     * @param userId
     * @return
     */
    List<OrderControl> selectByUserId(@Param("userId") Long userId,@Param("deviceId") Long deviceId);
}
