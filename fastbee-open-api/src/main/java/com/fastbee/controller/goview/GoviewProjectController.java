package com.fastbee.controller.goview;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.config.RuoYiConfig;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.bean.BeanUtils;
import com.fastbee.iot.domain.GoviewProject;
import com.fastbee.iot.domain.GoviewProjectData;
import com.fastbee.iot.model.goview.GoviewProjectVo;
import com.fastbee.iot.service.IGoviewProjectDataService;
import com.fastbee.iot.service.IGoviewProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目Controller
 *
 * @author kami
 * @date 2022-10-27
 */
@Api(tags = "项目模块")
@RestController
@RequestMapping("/goview/project")
public class GoviewProjectController extends BaseController {

    @Autowired
    private IGoviewProjectService goviewProjectService;

    @Autowired
    private IGoviewProjectDataService goviewProjectDataService;

    /**
     * 查询项目列表
     */
    @ApiOperation("查询项目列表")
    @GetMapping("/list")
    public TableDataInfo list(GoviewProject goviewProject) {
        startPage();
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            goviewProject.setTenantId(user.getDept().getDeptUserId());
        } else {
            goviewProject.setTenantId(user.getUserId());
        }
        List<GoviewProject> list = goviewProjectService.selectGoviewProjectList(goviewProject);
        return getDataTable(list);
    }

    /**
     * 获取项目详细信息
     */
    @ApiOperation("获取项目详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(goviewProjectService.selectGoviewProjectById(id));
    }

    /**
     * 新增项目
     */
    @ApiOperation("新增项目")
    @Log(title = "项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GoviewProject goviewProject) {
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            goviewProject.setTenantId(user.getDept().getDeptUserId());
            goviewProject.setTenantName(user.getDept().getDeptUserName());
        } else {
            goviewProject.setTenantId(user.getUserId());
            goviewProject.setTenantName(user.getUserName());
        }
        String projectId = goviewProjectService.insertGoviewProject(goviewProject);
        if(StringUtils.isNotEmpty(projectId)){
            return AjaxResult.success(MessageUtils.message("create.success"),goviewProject);
        }else {
            return AjaxResult.error(MessageUtils.message("create.failed"));
        }
    }

    /**
     * 修改项目
     */
    @ApiOperation("修改项目")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GoviewProject goviewProject) {
        return toAjax(goviewProjectService.updateGoviewProject(goviewProject));
    }

    /**
     * 删除项目
     */
    @ApiOperation("删除项目")
    @Log(title = "项目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(goviewProjectService.deleteGoviewProjectByIds(ids));
    }


    /**
     * 获取项目存储数据
     * @param projectId 项目id
     * @return
     */
    @ApiOperation("获取项目存储数据")
    @GetMapping("/getData")
    public AjaxResult getData(String projectId) {
        GoviewProject goviewProject = goviewProjectService.selectGoviewProjectById(projectId);
        GoviewProjectData projectData = goviewProjectDataService.selectGoviewProjectDataByProjectId(projectId);
        GoviewProjectVo goviewProjectVo = new GoviewProjectVo();
        BeanUtils.copyBeanProp(goviewProjectVo,goviewProject);
        if(projectData != null) {
            goviewProjectVo.setContent(projectData.getDataToStr());
        }
        return AjaxResult.success(goviewProjectVo);
    }


    /**
     * 保存大屏内部数据(字节)
     * @param data
     * @return
     */
    @ApiOperation("保存大屏内部数据")
    @PostMapping("/save/data")
    public AjaxResult saveData(GoviewProjectData data) {
        GoviewProject goviewProject= goviewProjectService.selectGoviewProjectById(data.getProjectId());
        if(goviewProject == null) {
            return AjaxResult.error(MessageUtils.message("goview.project.data.save.failed.id.null"));
        }
        int i = goviewProjectDataService.insertOrUpdateGoviewProjectData(data);
        if(i > 0) {
            return AjaxResult.success(MessageUtils.message("save.success"));
        }
        return AjaxResult.error(MessageUtils.message("save.failed"));
    }


    /**
     * goview文件上传（同一个大屏覆盖保存）
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public AjaxResult uploadFile(@RequestBody MultipartFile object) throws Exception {
        try {
            String filePath = RuoYiConfig.getProfile();
            // 获取文件名和文件类型
            String fileName = object.getOriginalFilename();
            fileName = "/goview/" + getLoginUser().getUserId().toString() + "/" + fileName;
            //创建目录
            File desc = new File(filePath + File.separator + fileName);
            if (!desc.exists()) {
                if (!desc.getParentFile().exists()) {
                    desc.getParentFile().mkdirs();
                }
            }
            // 存储文件-覆盖存储（一个文件一个图，防止过多）
            object.transferTo(desc);
            String url = "/profile" + fileName;
            Map<String, Object> map=new HashMap<String, Object>(2);
            map.put("fileName", url);
            map.put("url", url);
            return AjaxResult.success("上传成功",map);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

}
