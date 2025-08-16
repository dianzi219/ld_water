package com.fastbee.web.controller.system;

import cn.hutool.core.util.ObjectUtil;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.constant.UserConstants;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.*;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.mapper.DeviceMapper;
import com.fastbee.iot.model.RegisterUserInput;
import com.fastbee.iot.model.RegisterUserOutput;
import com.fastbee.iot.service.IToolService;
import com.fastbee.system.domain.SysRoleDept;
import com.fastbee.system.domain.SysRoleMenu;
import com.fastbee.system.domain.SysUserRole;
import com.fastbee.system.domain.vo.SysDeptTypeVO;
import com.fastbee.system.mapper.SysRoleDeptMapper;
import com.fastbee.system.mapper.SysRoleMenuMapper;
import com.fastbee.system.mapper.SysUserRoleMapper;
import com.fastbee.common.utils.MessageUtils;

import com.fastbee.system.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import static com.fastbee.common.constant.Constants.LANGUAGE;

/**
 * 机构信息
 *
 * @author ruoyi
 */
@Api(tags = "机构管理")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    @Autowired
    private ISysDeptService deptService;

    @Resource
    private IToolService toolService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private SysRoleDeptMapper sysRoleDeptMapper;

    @Resource
    private ISysDictDataService sysDictDataService;

    @Resource
    private ISysRoleService sysRoleService;

    @Resource
    private ISysMenuService sysMenuService;

    @Resource
    private DeviceMapper deviceMapper;

    /**
     * 获取机构列表
     */
    @ApiOperation("获取机构列表")
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return success(depts);
    }

    /**
     * 查询机构列表（排除节点）
     */
    @ApiOperation("查询机构列表（排除节点）")
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return success(depts);
    }

    /**
     * 根据机构编号获取详细信息
     */
    @ApiOperation("根据机构编号获取详细信息")
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @Transactional(rollbackFor = Exception.class)
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        SysDept sysDept = deptService.selectDeptById(deptId);
        if (null != sysDept && null != sysDept.getDeptUserId()) {
            SysUser sysUser = sysUserService.selectUserById(sysDept.getDeptUserId());
            sysDept.setUserName(sysUser.getUserName());
            sysDept.setPhone(sysUser.getPhonenumber());
        }
        return success(sysDept);
    }

    /**
     * 新增机构
     */
    @ApiOperation("新增机构")
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "机构管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(HttpServletRequest request, @Validated @RequestBody SysDept dept)
    {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept)))
        {
            return error(StringUtils.format(MessageUtils.message("dept.add.failed.name.exists"), dept.getDeptName()));
        }
        dept.setCreateBy(getUsername());
        // 校验系统账号信息
        if (StringUtils.isNotEmpty(dept.getUserName())) {
            SysUser sysUser = sysUserService.selectUserByUserName(dept.getUserName());
            if (ObjectUtil.isNotNull(sysUser)) {
                throw new ServiceException(MessageUtils.message("user.username.exists"));
            }
            if (!dept.getPassword().equals(dept.getConfirmPassword())) {
                throw new ServiceException(MessageUtils.message("user.password.differ"));
            }
        }
        int result = deptService.insertDept(dept);
        // 新增机构关联系统账号
        if (result > 0) {
            // 添加管理员角色，给所有权限
            // 查询所有权限
//            List<SysMenu> sysMenuList = sysMenuService.selectMenuList(new SysMenu(), 1L, request.getHeader(LANGUAGE));
            SysDept sysDept = deptService.selectDeptById(dept.getParentId());
            List<SysMenu> sysMenuList = sysMenuService.selectMenuList(new SysMenu(), sysDept.getDeptUserId());
            Long[] menuIdList = sysMenuList.stream().map(SysMenu::getMenuId).toArray(Long[]::new);
            SysRole sysRole = new SysRole();
            sysRole.setRoleName("管理员");
            sysRole.setRoleKey("manager");
            sysRole.setRoleSort(1);
            sysRole.setStatus("0");
            sysRole.setDeptId(dept.getDeptId());
            sysRole.setMenuIds(menuIdList);
            sysRoleService.insertRole(sysRole);

            // 注册机构管理员用户
            RegisterUserInput registerUserInput = new RegisterUserInput();
            registerUserInput.setUsername(dept.getUserName());
            registerUserInput.setPassword(dept.getPassword());
            registerUserInput.setPhonenumber(dept.getPhone());
            registerUserInput.setDeptId(dept.getDeptId());
            registerUserInput.setRoleIds(new Long[]{sysRole.getRoleId()});
            RegisterUserOutput registerUserOutput = toolService.registerNoCaptcha(registerUserInput);
            if (StringUtils.isNotEmpty(registerUserOutput.getMsg())) {
                deptService.deleteDeptById(dept.getDeptId());
                sysRoleService.deleteRoleById(sysRole.getRoleId());
                return AjaxResult.error(registerUserOutput.getMsg());
            }
            // 更新机构管理员角色绑定信息
            deptService.updateDeptUserId(dept.getDeptId(), registerUserOutput.getSysUserId());
        }
        return toAjax(result);
    }

    /**
     * 修改机构
     */
    @ApiOperation("修改机构")
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "机构管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept)))
        {
            return error(StringUtils.format(MessageUtils.message("dept.update.failed.name.exists"), dept.getDeptName()));
        }
        else if (dept.getParentId().equals(deptId))
        {
            return error(StringUtils.format(MessageUtils.message("dept.update.failed.parent.not.valid"), dept.getDeptName()));
        }
        else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return error(MessageUtils.message("dept.update.failed.child.not.valid"));
        }
        dept.setUpdateBy(getUsername());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除机构
     */
    @ApiOperation("根据机构编号删除机构")
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "机构管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            return warn(MessageUtils.message("dept.delete.failed.child.exists"));
        }
//        if (deptService.checkDeptExistUser(deptId))
//        {
//            return warn(MessageUtils.message("dept.delete.failed.user.exists"));
//        }
        deptService.checkDeptDataScope(deptId);
        // 删除机构绑定角色和用户
        List<Long> roleIdList = sysRoleDeptMapper.selectByDeptId(deptId);
        if (!org.springframework.util.CollectionUtils.isEmpty(roleIdList)) {
            sysRoleService.deleteRoleByIds(roleIdList.toArray(new Long[roleIdList.size()]));
            sysUserService.deleteUserByDeptID(deptId);
        }
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 获取机构类型
     * @param deptType 父级类型
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @GetMapping("/getDeptType")
    public AjaxResult getDeptType(HttpServletRequest request, Integer deptType, Boolean showOwner) {
        SysDictData sysDictData = new SysDictData();
        sysDictData.setDictType("department_type");
        List<SysDictData> sysDictDataList = sysDictDataService.selectDictDataList(sysDictData);
        if (CollectionUtils.isEmpty(sysDictDataList)) {
            return success();
        }
        List<SysDeptTypeVO> result = new ArrayList<>();
        for (SysDictData dictData : sysDictDataList) {
            SysDeptTypeVO sysDeptTypeVO = new SysDeptTypeVO();
            sysDeptTypeVO.setDeptType(Integer.valueOf(dictData.getDictValue()));
            sysDeptTypeVO.setDeptTypeName(dictData.getDictLabel());
            sysDeptTypeVO.setAncestors(dictData.getRemark());
            result.add(sysDeptTypeVO);
        }
        if (null == deptType) {
            return success(result);
        }
        SysDeptTypeVO sysDeptTypeVO = result.stream().filter(d -> deptType.equals(d.getDeptType())).findFirst().orElse(null);
        if (ObjectUtil.isNull(sysDeptTypeVO)) {
            return success(new ArrayList<>());
        }
        String ancestors = sysDeptTypeVO.getAncestors();
        result = result.stream().filter(d -> ancestors.contains(d.getDeptType().toString())).collect(Collectors.toList());
        if (showOwner) {
            List<SysDeptTypeVO> newResult = new ArrayList<>();
            newResult.add(sysDeptTypeVO);
            newResult.addAll(result);
            return success(newResult);
        }
        return success(result);
    }

    /**
     * 获取机构角色
     * @param deptId 机构id
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @GetMapping("/getRole")
    public AjaxResult getRole(Long deptId) {
        AjaxResult success = AjaxResult.success();
        List<SysRole> sysRoleList = deptService.getRole(deptId);
        success.put("roles", sysRoleList);
        success.put("roleIds", sysRoleList.stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        return success;
    }

}
