package com.fastbee.controller.sceneModel;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.constant.HttpStatus;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataExtendInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.SceneModelDevice;
import com.fastbee.iot.domain.SceneModelTag;
import com.fastbee.iot.mapper.SceneModelDataMapper;
import com.fastbee.iot.service.ISceneModelDeviceService;
import com.fastbee.iot.service.ISceneModelTagService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 场景录入型变量Controller
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@RestController
@RequestMapping("/scene/modelTag")
@Api(tags = "场景录入运算变量管理")
public class SceneModelTagController extends BaseController
{
    @Resource
    private ISceneModelTagService sceneModelTagService;
    @Resource
    private ISceneModelDeviceService sceneModelDeviceService;
    @Resource
    private SceneModelDataMapper sceneModelDataMapper;

    /**
     * 查询场景录入型变量列表
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:list')")
    @GetMapping("/list")
    @ApiOperation("查询场景录入运算变量列表")
    public TableDataExtendInfo list(SceneModelTag sceneModelTag)
    {
        SceneModelDevice sceneModelDevice = sceneModelDeviceService.selectOneBySceneModelIdAndVariableType(sceneModelTag.getSceneModelId(), sceneModelTag.getVariableType());
        startPage();
        List<SceneModelTag> list = sceneModelTagService.selectSceneModelTagList(sceneModelTag);
        TableDataExtendInfo rspData = new TableDataExtendInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        rspData.setAllEnable(null != sceneModelDevice ? sceneModelDevice.getAllEnable() : 0);
        return rspData;
    }

    /**
     * 导出场景录入型变量列表
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:export')")
    @Log(title = "场景录入型变量", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出场景录入运算变量列表")
    public void export(HttpServletResponse response, SceneModelTag sceneModelTag)
    {
        List<SceneModelTag> list = sceneModelTagService.selectSceneModelTagList(sceneModelTag);
        ExcelUtil<SceneModelTag> util = new ExcelUtil<SceneModelTag>(SceneModelTag.class);
        util.exportExcel(response, list, "场景录入型变量数据");
    }

    /**
     * 获取场景录入型变量详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取场景录入运算变量详情")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(sceneModelTagService.selectSceneModelTagById(id));
    }

    /**
     * 新增场景录入型变量
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:add')")
    @Log(title = "场景录入型变量", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增场景录入运算变量")
    public AjaxResult add(@RequestBody SceneModelTag sceneModelTag)
    {
        return toAjax(sceneModelTagService.insertSceneModelTag(sceneModelTag));
    }

    /**
     * 修改场景录入型变量
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:edit')")
    @Log(title = "场景录入型变量", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("编辑场景录入运算变量")
    public AjaxResult edit(@RequestBody SceneModelTag sceneModelTag)
    {
        return toAjax(sceneModelTagService.updateSceneModelTag(sceneModelTag));
    }

    /**
     * 删除场景录入型变量
     */
    @PreAuthorize("@ss.hasPermi('scene:modelTag:remove')")
    @Log(title = "场景录入型变量", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    @ApiOperation("删除场景录入运算变量")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        // 校验场景是否被应用到计算公式
        int count = sceneModelDataMapper.checkIsApplyAliasFormule(ids[0], null);
        if (count > 0) {
            throw new ServiceException("当前变量被引用到场景运算型变量的计算公式中，无法删除，请先删除引用关系后再执行删除操作！");
        }
        return toAjax(sceneModelTagService.deleteSceneModelTagByIds(ids));
    }
}
