package com.fastbee.system.mapper;

import java.util.List;
import com.fastbee.system.domain.SysRoleDept;
import org.apache.ibatis.annotations.Param;

/**
 * 角色与部门关联表 数据层
 * 
 * @author ruoyi
 */
public interface SysRoleDeptMapper
{
    /**
     * 通过角色ID删除角色和部门关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleDeptByRoleId(Long roleId);

    /**
     * 批量删除角色部门关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleDept(Long[] ids);

    /**
     * 查询部门使用数量
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int selectCountRoleDeptByDeptId(Long deptId);

    /**
     * 批量新增角色部门信息
     * 
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    public int batchRoleDept(List<SysRoleDept> roleDeptList);

    /**
     * 查询部门角色
     * @param deptIdList 部门id
     * @return java.util.List<com.fastbee.system.domain.SysRoleDept>
     */
    List<SysRoleDept> selectRoleDeptByDeptIds(@Param("deptIdList") List<Long> deptIdList);

    /**
     * 根据角色id查询关联部门
     * @param roleId 角色id
     * @return com.fastbee.system.domain.SysRoleDept
     */
    SysRoleDept selectByRoleId(Long roleId);

    /**
     * 查询部门角色
     * @param deptId 部门id
     * @return java.util.List<com.fastbee.common.core.domain.entity.SysRole>
     */
    List<Long> selectByDeptId(Long deptId);
}
