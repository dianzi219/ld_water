package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.iot.domain.OrderControl;

/**
 * 指令权限控制Service接口
 *
 * @author kerwincui
 * @date 2024-07-01
 */
public interface IOrderControlService
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
     * 批量删除指令权限控制
     *
     * @param ids 需要删除的指令权限控制主键集合
     * @return 结果
     */
    public int deleteOrderControlByIds(Long[] ids);

    /**
     * 删除指令权限控制信息
     *
     * @param id 指令权限控制主键
     * @return 结果
     */
    public int deleteOrderControlById(Long id);

    /**
     * 根据用户id获取
     * @param userId
     * @return
     */
     List<OrderControl> selectByUserId(Long userId,Long deviceId);

    /**
     * 判定是否有操作权限
     * @param deviceId
     * @param modelId
     * @return
     */
    AjaxResult judgeThingsModel(Long deviceId, Long modelId);
}
