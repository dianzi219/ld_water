package com.fastbee.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.framework.web.service.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.system.domain.SysClient;
import com.fastbee.system.service.ISysClientService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 系统授权Controller
 *
 * @author zhuangpeng.li
 * @date 2024-07-26
 */
@RestController
@RequestMapping("/system/sysclient")
@Api(tags = "系统授权")
public class SysClientController extends BaseController {
    @Autowired
    private ISysClientService sysClientService;
    @Autowired
    private TokenService tokenService;

    /**
     * 查询系统授权列表
     */
    @PreAuthorize("@ss.hasPermi('system:sysclient:list')")
    @GetMapping("/list")
    @ApiOperation("查询系统授权列表")
    public TableDataInfo list(SysClient sysClient) {
        startPage();
        List<SysClient> list = sysClientService.selectSysClientList(sysClient);
        return getDataTable(list);
    }

    /**
     * 导出系统授权列表
     */
    @ApiOperation("导出系统授权列表")
    @PreAuthorize("@ss.hasPermi('system:sysclient:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysClient sysClient) {
        List<SysClient> list = sysClientService.selectSysClientList(sysClient);
        ExcelUtil<SysClient> util = new ExcelUtil<SysClient>(SysClient.class);
        util.exportExcel(response, list, "系统授权数据");
    }

    /**
     * 获取系统授权详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:sysclient:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取系统授权详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(sysClientService.selectSysClientById(id));
    }

    /**
     * 新增系统授权
     */
    @PreAuthorize("@ss.hasPermi('system:sysclient:add')")
    @PostMapping
    @ApiOperation("新增系统授权")
    public AjaxResult add(@RequestBody SysClient sysClient) {
        SysUser user = getLoginUser().getUser();
        return toAjax(tokenService.addToken(user, sysClient));
    }

    /**
     * 修改系统授权
     */
    @PreAuthorize("@ss.hasPermi('system:sysclient:edit')")
    @PutMapping
    @ApiOperation("修改系统授权")
    public AjaxResult edit(@RequestBody SysClient sysClient) {
        SysUser user = getLoginUser().getUser();
        return toAjax(tokenService.updateToken(user, sysClient));
    }

    /**
     * 删除系统授权
     */
    @PreAuthorize("@ss.hasPermi('system:sysclient:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除系统授权")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(sysClientService.deleteSysClientByIds(ids));
    }
}
