package com.fastbee.controller.modbus;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.iot.model.modbus.ProductModbusJobVO;
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
import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.service.IProductModbusJobService;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 产品轮训任务列Controller
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
@RestController
@RequestMapping("/productModbus/job")
@Api(tags = "产品轮训任务列")
public class ProductModbusJobController extends BaseController {
    @Resource
    private IProductModbusJobService productModbusJobService;

    /**
     * 查询产品轮训任务列列表
     */
    @PreAuthorize("@ss.hasPermi('productModbus:job:list')")
    @GetMapping("/list")
    @ApiOperation("查询产品轮训任务列列表")
    public TableDataInfo list(ProductModbusJob productModbusJob) {
        startPage();
        List<ProductModbusJobVO> list = productModbusJobService.selectProductModbusJobList(productModbusJob);
        return getDataTable(list);
    }

    /**
     * 导出产品轮训任务列列表
     */
    @ApiOperation("导出产品轮训任务列列表")
    @PreAuthorize("@ss.hasPermi('productModbus:job:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductModbusJob productModbusJob) {
        List<ProductModbusJobVO> list = productModbusJobService.selectProductModbusJobList(productModbusJob);
        ExcelUtil<ProductModbusJobVO> util = new ExcelUtil<>(ProductModbusJobVO. class);
        util.exportExcel(response, list, "产品轮训任务列数据");
    }

    /**
     * 获取产品轮训任务列详细信息
     */
    @PreAuthorize("@ss.hasPermi('productModbus:job:query')")
    @GetMapping(value = "/{taskId}")
    @ApiOperation("获取产品轮训任务列详细信息")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId) {
        return success(productModbusJobService.queryByIdWithCache(taskId));
    }

    /**
     * 新增产品轮训任务列
     */
    @PreAuthorize("@ss.hasPermi('productModbus:job:add')")
    @PostMapping
    @ApiOperation("新增产品轮训任务列")
    public AjaxResult add(@RequestBody ProductModbusJob productModbusJob) {
        return toAjax(productModbusJobService.insertWithCache(productModbusJob));
    }

    /**
     * 修改产品轮训任务列
     */
    @PreAuthorize("@ss.hasPermi('productModbus:job:edit')")
    @PutMapping
    @ApiOperation("修改产品轮训任务列")
    public AjaxResult edit(@RequestBody ProductModbusJob productModbusJob) {
        return toAjax(productModbusJobService.updateWithCache(productModbusJob));
    }

    /**
     * 删除产品轮训任务列
     */
    @PreAuthorize("@ss.hasPermi('productModbus:job:remove')")
    @DeleteMapping("/{taskIds}")
    @ApiOperation("删除产品轮训任务列")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(productModbusJobService.deleteWithCacheByIds(taskIds, true));
    }

    /**
     * 获取产品从机id
     */
    @GetMapping(value = "/getSlaveId")
    @ApiOperation("获取产品从机id")
    public AjaxResult getSlaveId(Long productId, Long deviceId) {
        return success(productModbusJobService.getSlaveId(productId, deviceId));
    }
}
