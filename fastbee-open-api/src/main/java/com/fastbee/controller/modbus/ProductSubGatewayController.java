package com.fastbee.controller.modbus;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.iot.model.gateWay.ProductSubGatewayAddVO;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.fastbee.iot.domain.ProductSubGateway;
import com.fastbee.iot.service.IProductSubGatewayService;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 网关与子产品关联Controller
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
@RestController
@RequestMapping("/productModbus/gateway")
@Api(tags = "网关与子产品关联")
public class ProductSubGatewayController extends BaseController {

    @Resource
    private IProductSubGatewayService productSubGatewayService;

    /**
     * 查询网关与子产品关联列表
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:list')")
    @GetMapping("/list")
    @ApiOperation("查询网关与子产品关联列表")
    public TableDataInfo list(ProductSubGateway productSubGateway) {
        startPage();
        List<ProductSubGatewayVO> list = productSubGatewayService.selectProductSubGatewayList(productSubGateway);
        return getDataTable(list);
    }

    /**
     * 导出网关与子产品关联列表
     */
    @ApiOperation("导出网关与子产品关联列表")
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductSubGateway productSubGateway) {
        List<ProductSubGatewayVO> list = productSubGatewayService.selectProductSubGatewayList(productSubGateway);
        ExcelUtil<ProductSubGatewayVO> util = new ExcelUtil<ProductSubGatewayVO>(ProductSubGatewayVO. class);
        util.exportExcel(response, list, "网关与子产品关联数据");
    }

    /**
     * 获取网关与子产品关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取网关与子产品关联详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(productSubGatewayService.queryByIdWithCache(id));
    }

    /**
     * 新增网关与子产品关联
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:add')")
    @PostMapping
    @ApiOperation("新增网关与子产品关联")
    public AjaxResult add(@RequestBody ProductSubGateway productSubGateway) {
        return toAjax(productSubGatewayService.insertWithCache(productSubGateway));
    }

    /**
     * 新增网关与子产品关联
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:add')")
    @PostMapping("/addBatch")
    @ApiOperation("新增网关与子产品关联")
    public AjaxResult addBatch(@RequestBody ProductSubGatewayAddVO productSubGatewayAddVO) {
        return toAjax(productSubGatewayService.addBatch(productSubGatewayAddVO));
    }

    /**
     * 修改网关与子产品关联
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:edit')")
    @PutMapping
    @ApiOperation("修改网关与子产品关联")
    public AjaxResult edit(@RequestBody ProductSubGateway productSubGateway) {
        return toAjax(productSubGatewayService.updateWithCache(productSubGateway));
    }

    /**
     * 修改网关与子产品关联
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:edit')")
    @PostMapping("/editBatch")
    @ApiOperation("修改网关与子产品关联")
    public AjaxResult editBatch(@RequestBody List<ProductSubGateway> list) {
        return toAjax(productSubGatewayService.editBatch(list));
    }

    /**
     * 删除网关与子产品关联
     */
    @PreAuthorize("@ss.hasPermi('productModbus:gateway:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除网关与子产品关联")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productSubGatewayService.deleteWithCacheByIds(ids, true));
    }
}
