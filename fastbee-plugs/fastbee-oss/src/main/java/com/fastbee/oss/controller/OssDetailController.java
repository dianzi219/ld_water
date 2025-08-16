package com.fastbee.oss.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.oss.domain.OssDetail;
import com.fastbee.oss.service.IOssDetailService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件记录Controller
 *
 * @author zhuangpeng.li
 * @date 2024-04-22
 */
@RestController
@RequestMapping("/oss/detail")
public class OssDetailController extends BaseController {
    @Autowired
    private IOssDetailService ossDetailService;

    /**
     * 查询文件记录列表
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:list')")
    @GetMapping("/list")
    public TableDataInfo list(OssDetail ossDetail) {
        startPage();
        List<OssDetail> list = ossDetailService.selectOssDetailList(ossDetail);
        return getDataTable(list);
    }

    /**
     * 导出文件记录列表
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:export')")
    @Log(title = "文件记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OssDetail ossDetail) {
        List<OssDetail> list = ossDetailService.selectOssDetailList(ossDetail);
        ExcelUtil<OssDetail> util = new ExcelUtil<OssDetail>(OssDetail.class);
        util.exportExcel(response, list, "文件记录数据");
    }

    /**
     * 获取文件记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return success(ossDetailService.selectOssDetailById(id));
    }

    /**
     * 新增文件记录
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:add')")
    @Log(title = "文件记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OssDetail ossDetail) {
        return toAjax(ossDetailService.insertOssDetail(ossDetail));
    }

    /**
     * 修改文件记录
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:edit')")
    @Log(title = "文件记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OssDetail ossDetail) {
        return toAjax(ossDetailService.updateOssDetail(ossDetail));
    }

    /**
     * 删除文件记录
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:remove')")
    @Log(title = "文件记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(ossDetailService.deleteOssDetailByIds(ids));
    }


    /**
     * 上传OSS对象存储
     *
     * @param file 文件
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:upload')")
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult upload(@RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return error("上传文件不能为空");
        }
        return toAjax(ossDetailService.upload(file));
    }

    /**
     * 下载OSS对象
     *
     * @param ossId OSS对象ID
     */
    @PreAuthorize("@ss.hasPermi('oss:detail:download')")
    @GetMapping("/download/{ossId}")
    public void download(@PathVariable Integer ossId, HttpServletResponse response) throws IOException {
        ossDetailService.download(ossId, response);
    }

}
