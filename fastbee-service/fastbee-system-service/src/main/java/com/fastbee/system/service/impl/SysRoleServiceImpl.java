package com.fastbee.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import com.fastbee.common.core.domain.entity.SysDept;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.system.mapper.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fastbee.common.annotation.DataScope;
import com.fastbee.common.constant.UserConstants;
import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.system.domain.SysRoleDept;
import com.fastbee.system.domain.SysRoleMenu;
import com.fastbee.system.domain.SysUserRole;
import com.fastbee.system.service.ISysRoleService;

import javax.annotation.Resource;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;
import static com.fastbee.common.utils.SecurityUtils.isAdmin;

/**
 * 角色 业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    @Resource
    private SysDeptMapper sysDeptMapper;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        LoginUser loginUser = getLoginUser();
        Long userId = loginUser.getUserId();
        Long userDeptId = loginUser.getDeptId();
        Long deptId;
        if (null != role.getDeptId()) {
            deptId = role.getDeptId();
        } else {
            deptId = loginUser.getDeptId();
        }
        List<SysDept> sysDeptList;
        SysDept sysDept = new SysDept();
        sysDept.setDeptId(deptId);
        if (null != role.getShowChild() && role.getShowChild()) {
            sysDeptList = sysDeptMapper.listDeptAndChild(sysDept);
        } else {
            sysDeptList = sysDeptMapper.selectDeptList(sysDept);
        }
//        List<SysDept> sysDeptList = new ArrayList<>();
//        if (null != role.getDeptIds()) {
//            sysDeptList = sysDeptMapper.selectDeptByIds(Arrays.asList(role.getDeptIds()));
//        } else {
//            LoginUser loginUser = getLoginUser();
//            Long deptId = loginUser.getDeptId();
//            if (null != deptId) {
//                SysDept sysDept = new SysDept();
//                sysDept.setDeptId(deptId);
//                sysDeptList = sysDeptMapper.selectDeptList(sysDept);
//            }
//        }
        if (CollectionUtils.isNotEmpty(sysDeptList)) {
            Map<Long, String> deptMap = sysDeptList.stream().collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));
            List<Long> deptIdList = sysDeptList.stream().map(SysDept::getDeptId).collect(Collectors.toList());
            List<SysRoleDept> sysRoleDeptList = roleDeptMapper.selectRoleDeptByDeptIds(deptIdList);
            Map<Long, Long> roleDeptMap = sysRoleDeptList.stream().collect(Collectors.toMap(SysRoleDept::getRoleId, SysRoleDept::getDeptId));
            if (CollectionUtils.isEmpty(sysRoleDeptList)) {
                return new ArrayList<>();
            }
            List<Long> roleIdList = sysRoleDeptList.stream().map(SysRoleDept::getRoleId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(roleIdList)) {
                return new ArrayList<>();
            }
            List<SysRole> sysRoleList = roleMapper.selectRoleByIds(roleIdList, role.getRoleName(), role.getStatus());
            for (SysRole sysRole : sysRoleList) {
                Long deptId1 = roleDeptMap.get(sysRole.getRoleId());
                String deptName = deptMap.get(deptId1);
                sysRole.setDeptId(deptId1);
                sysRole.setDeptName(deptName);
//                if (userDeptId.equals(deptId1) && "manager".equals(sysRole.getRoleKey())) {
//                    sysRole.setCanEditRole(false);
//                } else {
//                    sysRole.setCanEditRole(true);
//                }
                sysRole.setCanEditRole(!(userDeptId.equals(deptId1) && "manager".equals(sysRole.getRoleKey()) && !isAdmin(userId)));
                sysRole.setManager("manager".equals(sysRole.getRoleKey()));
            }
            return sysRoleList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Cacheable(cacheNames = "role", key = "#root.methodName + ':' + #userId", unless = "#result == null or #result.size() == 0")
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Cacheable(cacheNames = "role", key = "#root.methodName + ':' + #userId", unless = "#result == null or #result.size() == 0")
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Cacheable(cacheNames = "role", key = "#root.methodName + ':' + #roleId", unless = "#result == null")
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        SysRole sysRole = roleMapper.selectRoleById(roleId);
        if (ObjectUtil.isNotNull(sysRole)) {
            SysRoleDept sysRoleDept = roleDeptMapper.selectByRoleId(sysRole.getRoleId());
            if (ObjectUtil.isNotNull(sysRoleDept)) {
                sysRole.setDeptId(sysRoleDept.getDeptId());
            }
        }
        return sysRole;
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleId 角色id
     */
    @Override
    public void checkRoleDataScope(Long roleId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysRole role = new SysRole();
            role.setRoleId(roleId);
            List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
            if (StringUtils.isEmpty(roles))
            {
                throw new ServiceException("没有权限访问角色数据！");
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Cacheable(cacheNames = "role", key = "#root.methodName + ':' + #roleId", unless = "#result == null")
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role)
    {
        // 新增角色信息
        roleMapper.insertRole(role);
        // 绑定部门角色
        List<SysRoleDept> sysRoleDeptList = new ArrayList<>();
        SysRoleDept sysRoleDept = new SysRoleDept();
        sysRoleDept.setDeptId(role.getDeptId());
        sysRoleDept.setRoleId(role.getRoleId());
        sysRoleDeptList.add(sysRoleDept);
        if (CollectionUtils.isNotEmpty(sysRoleDeptList)) {
            roleDeptMapper.batchRoleDept(sysRoleDeptList);
        }
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "role", key = "'selectRoleById:' + #role.roleId", condition = "#result != null"),
    })
    @Override
    @Transactional
    public int updateRole(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "role", key = "'selectRoleById:' + #role.roleId", condition = "#result != null"),
    })
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @CacheEvict(cacheNames = "role", allEntries = true)
    @Override
    @Transactional
    public int deleteRoleById(Long roleId)
    {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @CacheEvict(cacheNames = "role", allEntries = true)
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            //if (countUserRoleByRoleId(roleId) > 0)
            //{
            //    throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            //}
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @CacheEvict(cacheNames = "role", allEntries = true)
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @CacheEvict(cacheNames = "role", allEntries = true)
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
