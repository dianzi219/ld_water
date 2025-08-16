package com.fastbee.iot.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.service.IThingsModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.OrderControlMapper;
import com.fastbee.iot.domain.OrderControl;
import com.fastbee.iot.service.IOrderControlService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

/**
 * 指令权限控制Service业务层处理
 *
 * @author kerwincui
 * @date 2024-07-01
 */
@Service
public class OrderControlServiceImpl implements IOrderControlService
{
    @Autowired
    private OrderControlMapper orderControlMapper;
    @Resource
    private IThingsModelService thingsModelService;

    /**
     * 查询指令权限控制
     *
     * @param id 指令权限控制主键
     * @return 指令权限控制
     */
    @Override
    public OrderControl selectOrderControlById(Long id)
    {
        return orderControlMapper.selectOrderControlById(id);
    }

    /**
     * 查询指令权限控制列表
     *
     * @param orderControl 指令权限控制
     * @return 指令权限控制
     */
    @Override
    public List<OrderControl> selectOrderControlList(OrderControl orderControl)
    {
        orderControl.setTenantId(SecurityUtils.getDeptId());
        List<OrderControl> orderControls = orderControlMapper.selectOrderControlList(orderControl);
        for (OrderControl control : orderControls) {
            String[] split = control.getSelectOrder().split(",");
            List<String> ids = Arrays.stream(split).collect(Collectors.toList());
            ThingsModel thingsModel = new ThingsModel();
            thingsModel.setModelIdList(ids);
            List<ThingsModel> thingsModels = thingsModelService.selectThingsModelList(thingsModel);
            String names = thingsModels.stream().map(ThingsModel::getModelName).collect(Collectors.joining(","));
            control.setModelNames(names);
            //判断是否在执行时间内的控制
            if (Objects.nonNull(control.getStartTime()) && Objects.nonNull(control.getEndTime())) {
                Date now = DateUtils.getNowDate();
                Date startTime = control.getStartTime();
                Date endTime = control.getEndTime();
                if (now.after(startTime) && now.before(endTime) && control.getCount() > 0) {
                    control.setStatus("1");
                }else {
                    control.setStatus("0");
                }
            }
        }
        return orderControls;
    }

    /**
     * 新增指令权限控制
     *
     * @param orderControl 指令权限控制
     * @return 结果
     */
    @Override
    public int insertOrderControl(OrderControl orderControl)
    {
        orderControl.setTenantId(SecurityUtils.getDeptId());
        orderControl.setCreateTime(DateUtils.getNowDate());
        orderControl.setStatus("1");
        return orderControlMapper.insertOrderControl(orderControl);
    }

    /**
     * 修改指令权限控制
     *
     * @param orderControl 指令权限控制
     * @return 结果
     */
    @Override
    public int updateOrderControl(OrderControl orderControl)
    {
        orderControl.setUpdateTime(DateUtils.getNowDate());
        return orderControlMapper.updateOrderControl(orderControl);
    }

    /**
     * 批量删除指令权限控制
     *
     * @param ids 需要删除的指令权限控制主键
     * @return 结果
     */
    @Override
    public int deleteOrderControlByIds(Long[] ids)
    {
        return orderControlMapper.deleteOrderControlByIds(ids);
    }

    /**
     * 删除指令权限控制信息
     *
     * @param id 指令权限控制主键
     * @return 结果
     */
    @Override
    public int deleteOrderControlById(Long id)
    {
        return orderControlMapper.deleteOrderControlById(id);
    }
    /**
     * 根据用户id获取
     * @param userId
     * @return
     */
    @Override
    public List<OrderControl> selectByUserId(Long userId,Long deviceId){
        List<OrderControl> list = orderControlMapper.selectByUserId(userId, deviceId);
        for (OrderControl control : list) {
            //判断是否在执行时间内的控制
            Date now = DateUtils.getNowDate();
            Date startTime = control.getStartTime();
            Date endTime = control.getEndTime();
            if (now.after(startTime) && now.before(endTime) && control.getCount() > 0){
                control.setStatus("1");
            }else {
                control.setStatus("0");
            }
        }
        return list;
    }
    /**
     * 判定是否有操作权限
     * @param deviceId
     * @param modelId
     * @return
     */
    @Override
    public AjaxResult judgeThingsModel(Long deviceId, Long modelId) {
        Long userId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptUserId = loginUser.getDeptUserId();
        //这里排除管理员和终端用户
        if (!userId.equals(deptUserId) && !Objects.isNull(loginUser.getDeptId())) {
            List<OrderControl> list = orderControlMapper.selectByUserId(userId, deviceId);
            for (OrderControl control : list) {
                //判断是否在执行时间内的控制
                Date now = DateUtils.getNowDate();
                Date startTime = control.getStartTime();
                Date endTime = control.getEndTime();
                if (now.after(startTime) && now.before(endTime) && control.getCount() > 0) {
                    String selectOrder = control.getSelectOrder();
                    String[] ids = selectOrder.split(",");
                    if (Arrays.asList(ids).contains(modelId + "")) {
                        return AjaxResult.success();
                    }
                }
            }
            return AjaxResult.error(MessageUtils.message("device.can.send"));
        }
        return AjaxResult.success();
    }
}
