package com.fastbee.controller.ruleEngine;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.iot.domain.Script;
import com.fastbee.iot.service.IScriptService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 规则引擎脚本Controller
 *
 * @author lizhuangpeng
 * @date 2023-07-01
 */
@RestController
@RequestMapping("/iot/script")
public class ScriptController extends BaseController
{
    @Autowired
    private IScriptService scriptService;

    /**
     * 查询规则引擎脚本列表
     */
    @PreAuthorize("@ss.hasPermi('iot:script:list')")
    @GetMapping("/list")
    public TableDataInfo list(Script ruleScript)
    {
        SysUser sysUser = getLoginUser().getUser();
//        List<SysRole> roles = sysUser.getRoles();
//        for (SysRole role : roles) {
//            if (role.getRoleKey().equals("general") || role.getRoleKey().equals("tenant")) {
//                // 用户和租户只能查看自己的规则脚本
//                ruleScript.setUserId(sysUser.getUserId());
//                break;
//            }
//        }
        // 只能查看所属机构
        if (null != sysUser.getDeptId()) {
            ruleScript.setUserId(sysUser.getDept().getDeptUserId());
        } else {
            ruleScript.setUserId(sysUser.getUserId());
        }
        startPage();
        List<Script> list = scriptService.selectRuleScriptList(ruleScript);
        return getDataTable(list);
    }

    /**
     * 导出规则引擎脚本列表
     */
    @PreAuthorize("@ss.hasPermi('iot:script:export')")
    @Log(title = "规则引擎脚本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Script ruleScript)
    {
        SysUser sysUser = getLoginUser().getUser();
//        List<SysRole> roles = sysUser.getRoles();
//        for (SysRole role : roles) {
//            if (role.getRoleKey().equals("general") || role.getRoleKey().equals("tenant")) {
//                // 用户和租户只能查看自己的规则脚本
//                ruleScript.setUserId(sysUser.getUserId());
//                break;
//            }
//        }
        // 只能查看所属机构
        if (null != sysUser.getDeptId()) {
            ruleScript.setUserId(sysUser.getDept().getDeptUserId());
        } else {
            ruleScript.setUserId(sysUser.getUserId());
        }
        List<Script> list = scriptService.selectRuleScriptList(ruleScript);
        ExcelUtil<Script> util = new ExcelUtil<Script>(Script.class);
        util.exportExcel(response, list, "规则引擎脚本数据");
    }

    /**
     * 获取规则引擎脚本详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:script:query')")
    @GetMapping(value = "/{scriptId}")
    public AjaxResult getInfo(@PathVariable("scriptId") String scriptId)
    {
        return success(scriptService.selectRuleScriptById(scriptId));
    }

    /**
     * 新增规则引擎脚本
     */
    @PreAuthorize("@ss.hasPermi('iot:script:add')")
    @Log(title = "规则引擎脚本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Script ruleScript)
    {
        return toAjax(scriptService.insertRuleScript(ruleScript));
    }

    /**
     * 修改规则引擎脚本
     */
    @PreAuthorize("@ss.hasPermi('iot:script:edit')")
    @Log(title = "规则引擎脚本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Script ruleScript)
    {
        return toAjax(scriptService.updateRuleScript(ruleScript));
    }

    /**
     * 获取规则引擎脚本日志
     */
    @PreAuthorize("@ss.hasPermi('iot:script:query')")
    @GetMapping(value = "/log/{scriptId}")
    public AjaxResult getScriptLog(@PathVariable("scriptId") String scriptId)
    {
        return success(scriptService.selectRuleScriptLog(scriptId));
    }

    @PreAuthorize("@ss.hasPermi('iot:script:query')")
    @GetMapping(value = "/log/open/{scriptId}")
    public AjaxResult openPublishLog(@PathVariable("scriptId") String scriptId)
    {
        scriptService.openScriptLog(scriptId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('iot:script:query')")
    @GetMapping(value = "/log/close/{scriptId}")
    public AjaxResult closePublishLog(@PathVariable("scriptId") String scriptId)
    {
        scriptService.closeScriptLog(scriptId);
        return success();
    }

    /**
     * 删除规则引擎脚本
     */
    @PreAuthorize("@ss.hasPermi('iot:script:remove')")
    @Log(title = "规则引擎脚本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scriptIds}")
    public AjaxResult remove(@PathVariable String[] scriptIds)
    {
        return toAjax(scriptService.deleteRuleScriptByIds(scriptIds));
    }

    /**
     * 删除规则引擎脚本
     */
    @PostMapping("/validate")
    public AjaxResult validateScript(@RequestBody Script ruleScript)
    {
        return scriptService.validateScript(ruleScript);
    }
}
