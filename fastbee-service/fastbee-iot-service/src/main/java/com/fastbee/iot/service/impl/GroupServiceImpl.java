package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Group;
import com.fastbee.iot.mapper.GroupMapper;
import com.fastbee.iot.model.DeviceGroupInput;
import com.fastbee.iot.model.IdOutput;
import com.fastbee.iot.service.IGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fastbee.common.utils.SecurityUtils.getDeptId;
import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 设备分组Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
public class GroupServiceImpl implements IGroupService
{
    @Autowired
    private GroupMapper groupMapper;

    /**
     * 查询设备分组
     *
     * @param groupId 设备分组主键
     * @return 设备分组
     */
    @Override
    public Group selectGroupByGroupId(Long groupId)
    {
        return groupMapper.selectGroupByGroupId(groupId);
    }

    /**
     * 通过分组ID查询关联的设备ID数组
     * @param groupId
     * @return
     */
    @Override
    public Long[]  selectDeviceIdsByGroupId(Long groupId){
        List<IdOutput> list=groupMapper.selectDeviceIdsByGroupId(groupId);
        Long[] ids=new Long[list.size()];
        for(int i=0;i<list.size();i++){
            ids[i]=list.get(i).getId();
        }
        return ids;
    }

    /**
     * 查询设备分组列表
     *
     * @param group 设备分组
     * @return 设备分组
     */
    @Override
    public List<Group> selectGroupList(Group group)
    {
        Long deptId = getDeptId();
        if (Objects.isNull(deptId)){
            group.setUserId(getLoginUser().getUserId()); //终端用户根据用户id查询
        }else {
            group.setUserId(getLoginUser().getUser().getDept().getDeptUserId()); //租户根据机构绑定管理员查询
        }
        return groupMapper.selectGroupList(group);
    }

    /**
     * 新增设备分组
     *
     * @param group 设备分组
     * @return 结果
     */
    @Override
    public int insertGroup(Group group)
    {
        //分组，如果是租户按照机构管理员来存储，终端用户按照用户id来
        Long deptId = getDeptId();
        if (Objects.isNull(deptId)){
            SysUser user = getLoginUser().getUser();
            group.setUserId(user.getUserId());
            group.setUserName(user.getUserName());
        }else {
            Long deptUserId = getLoginUser().getUser().getDept().getDeptUserId();
            String userName = getLoginUser().getUser().getDept().getDeptUserName();
            group.setUserId(deptUserId);
            group.setUserName(userName);
        }
        group.setCreateTime(DateUtils.getNowDate());
        return groupMapper.insertGroup(group);
    }

    /**
     * 修改设备分组
     *
     * @param group 设备分组
     * @return 结果
     */
    @Override
    public int updateGroup(Group group)
    {
        group.setUpdateTime(DateUtils.getNowDate());
        return groupMapper.updateGroup(group);
    }

    /**
     * 分组下批量添加设备分组
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateDeviceGroups(DeviceGroupInput input){
        //删除分组下的所有关联设备
        groupMapper.deleteDeviceGroupByGroupIds(new Long[]{input.getGroupId()});
        // 分组下添加关联设备
        if(input.getDeviceIds().length>0){
            groupMapper.insertDeviceGroups(input);
        }
        return 1;
    }

    /**
     * 批量删除分组和设备分组
     *
     * @param groupIds 需要删除的设备分组主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGroupByGroupIds(Long[] groupIds)
    {
        // 删除设备分组
        groupMapper.deleteDeviceGroupByGroupIds(groupIds);
        // 删除分组
        return groupMapper.deleteGroupByGroupIds(groupIds);
    }

    /**
     * 删除分组信息
     *
     * @param groupId 设备分组主键
     * @return 结果
     */
    @Override
    public int deleteGroupByGroupId(Long groupId)
    {

        return groupMapper.deleteGroupByGroupId(groupId);
    }

    /**
     * 根据用户Id查询分组列表
     * @param userId 用户id
     * @return
     */
    @Override
    public List<Group> getGroupListByUserId(Long userId) {
        return groupMapper.selectGroupListByUserId(userId);
    }

    @Override
    public List<Group> listAllByIdList(Set<Long> groupIds) {
        List<Group> groupList = null ;
        if (CollectionUtils.isNotEmpty(groupIds)){
            groupList = groupMapper.listAllByIdList(groupIds);
        }
        return groupList;
    }

}
