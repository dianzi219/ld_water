package com.fastbee.oauth.controller;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.oauth.domain.OauthClientDetails;
import com.fastbee.oauth.service.IOauthClientDetailsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 云云对接Controller
 *
 * @author kerwincui
 * @date 2022-02-07
 */
@Api(tags = "云云对接")
@RestController
@RequestMapping("/iot/clientDetails")
public class OauthClientDetailsController extends BaseController
{
    @Autowired
    private IOauthClientDetailsService oauthClientDetailsService;

    /**
     * 查询云云对接列表
     */
    @ApiOperation("查询云云对接列表")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:list')")
    @GetMapping("/list")
    public TableDataInfo list(OauthClientDetails oauthClientDetails)
    {
        startPage();
        List<OauthClientDetails> list = oauthClientDetailsService.selectOauthClientDetailsList(oauthClientDetails);
        return getDataTable(list);
    }

    /**
     * 导出云云对接列表
     */
    @ApiOperation("导出云云对接列表")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:export')")
    @Log(title = "云云对接", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OauthClientDetails oauthClientDetails)
    {
        List<OauthClientDetails> list = oauthClientDetailsService.selectOauthClientDetailsList(oauthClientDetails);
        ExcelUtil<OauthClientDetails> util = new ExcelUtil<OauthClientDetails>(OauthClientDetails.class);
        util.exportExcel(response, list, "云云对接数据");
    }

    /**
     * 获取云云对接详细信息
     */
    @ApiOperation("获取云云对接详细信息")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(oauthClientDetailsService.selectOauthClientDetailsById(id));
    }

    /**
     * 新增云云对接
     */
    @ApiOperation("新增云云对接")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:add')")
    @Log(title = "云云对接", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OauthClientDetails oauthClientDetails)
    {
        return oauthClientDetailsService.insertOauthClientDetails(oauthClientDetails);
    }

    /**
     * 修改云云对接
     */
    @ApiOperation("修改云云对接")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:edit')")
    @Log(title = "云云对接", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OauthClientDetails oauthClientDetails)
    {
        return oauthClientDetailsService.updateOauthClientDetails(oauthClientDetails);
    }

    /**
     * 修改云云对接
     */
    @ApiOperation("删除云云对接")
    @PreAuthorize("@ss.hasPermi('iot:clientDetails:remove')")
    @Log(title = "云云对接", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(oauthClientDetailsService.deleteOauthClientDetailsByIds(ids));
    }
}
