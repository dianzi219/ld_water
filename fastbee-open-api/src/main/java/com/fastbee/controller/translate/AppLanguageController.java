package com.fastbee.controller.translate;


import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.system.domain.AppLanguage;
import com.fastbee.system.service.IAppLanguageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * app语言Controller
 */
@RestController
@RequestMapping("/app/language")
@Api(tags = "APP语言")
public class AppLanguageController extends BaseController
{
    @Autowired
    private IAppLanguageService appLanguageService;

    /**
     * 查询app语言列表
     */
    //@PreAuthorize("@ss.hasPermi('app:language:list')")
    @GetMapping("/list")
    @ApiOperation("查询app语言列表")
    public TableDataInfo list(AppLanguage appLanguage)
    {
        startPage();
        List<AppLanguage> list = appLanguageService.selectAppLanguageList(appLanguage);
        return getDataTable(list);
    }

    /**
     * 导出app语言列表
     */
    @PreAuthorize("@ss.hasPermi('app:language:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, AppLanguage appLanguage)
    {
        List<AppLanguage> list = appLanguageService.selectAppLanguageList(appLanguage);
        ExcelUtil<AppLanguage> util = new ExcelUtil<AppLanguage>(AppLanguage.class);
        util.exportExcel(response, list, "app语言数据");
    }

    /**
     * 获取app语言详细信息
     */
    //@PreAuthorize("@ss.hasPermi('app:language:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取app语言详细信息")
    public CommonResult getInfo(@PathVariable("id") Long id)
    {
        return CommonResult.success(appLanguageService.selectAppLanguageById(id));
    }

    /**
     * 新增app语言
     */
    @PreAuthorize("@ss.hasPermi('app:language:add')")
    @PostMapping
    public CommonResult add(@RequestBody AppLanguage appLanguage)
    {
        return CommonResult.success(appLanguageService.insertAppLanguage(appLanguage));
    }

    /**
     * 修改app语言
     */
    @PreAuthorize("@ss.hasPermi('app:language:edit')")
    @PutMapping
    public CommonResult edit(@RequestBody AppLanguage appLanguage)
    {
        return CommonResult.success(appLanguageService.updateAppLanguage(appLanguage));
    }

    /**
     * 删除app语言
     */
    @PreAuthorize("@ss.hasPermi('app:language:remove')")
	@DeleteMapping("/{ids}")
    public CommonResult remove(@PathVariable Long[] ids)
    {
        return CommonResult.success(appLanguageService.deleteAppLanguageByIds(ids));
    }
}
