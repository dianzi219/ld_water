package com.fastbee.controller.ruleEngine;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.enums.BusinessType;
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
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.Bridge;
import com.fastbee.iot.service.IBridgeService;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 数据桥接Controller
 *
 * @author kerwincui
 * @date 2024-08-20
 */
@RestController
@RequestMapping("/iot/bridge")
@Api(tags = "数据桥接")
public class BridgeController extends BaseController {
    @Autowired
    private IBridgeService bridgeService;

    /**
     * 查询数据桥接列表
     */
    @PreAuthorize("@ss.hasPermi('iot:bridge:list')")
    @GetMapping("/list")
    @ApiOperation("查询数据桥接列表")
    public TableDataInfo list(Bridge bridge) {
        startPage();
        List<Bridge> list = bridgeService.selectBridgeList(bridge);
        return getDataTable(list);
    }

    /**
     * 导出数据桥接列表
     */
    @ApiOperation("导出数据桥接列表")
    @PreAuthorize("@ss.hasPermi('iot:bridge:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Bridge bridge) {
        List<Bridge> list = bridgeService.selectBridgeList(bridge);
        ExcelUtil<Bridge> util = new ExcelUtil<Bridge>(Bridge. class);
        util.exportExcel(response, list, "数据桥接数据");
    }

    /**
     * 获取数据桥接详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:bridge:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取数据桥接详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(bridgeService.queryByIdWithCache(id));
    }

    /**
     * 新增数据桥接
     */
    @PreAuthorize("@ss.hasPermi('iot:bridge:add')")
    @PostMapping
    @ApiOperation("新增数据桥接")
    public AjaxResult add(@RequestBody Bridge bridge) {
        return toAjax(bridgeService.insertWithCache(bridge));
    }

    /**
     * 修改数据桥接
     */
    @PreAuthorize("@ss.hasPermi('iot:bridge:edit')")
    @PutMapping
    @ApiOperation("修改数据桥接")
    public AjaxResult edit(@RequestBody Bridge bridge) {
        return toAjax(bridgeService.updateWithCache(bridge));
    }

    /**
     * 删除数据桥接
     */
    @PreAuthorize("@ss.hasPermi('iot:bridge:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除数据桥接")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bridgeService.deleteWithCacheByIds(ids, true));
    }

    @PreAuthorize("@ss.hasPermi('iot:bridge:edit')")
    @PostMapping("/connect")
    @ApiOperation("连接数据桥接")
    public AjaxResult connect(@RequestBody Bridge bridge)
    {
        return toAjax(bridgeService.connect(bridge));

    }
}
