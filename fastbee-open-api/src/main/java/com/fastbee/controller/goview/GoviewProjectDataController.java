package com.fastbee.controller.goview;

import com.fastbee.common.annotation.Anonymous;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.service.IGoviewProjectDataService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * goview 获取数据接口
 * 可自己根据需求在此添加获取数据接口
 * @author fastb
 * @date 2023-11-06 15:07
 */
@Anonymous
@Api(tags = "大屏管理获取数据")
@RestController
@RequestMapping("/goview/projectData")
public class GoviewProjectDataController {

    @Resource
    private IGoviewProjectDataService goviewProjectDataService;

    /**
     * 根据sql获取组件数据接口
     * @param sql sql
     * @return 组件数据
     */
    @PostMapping("/executeSql")
    public AjaxResult executeSql(@RequestParam String sql) {
        if (StringUtils.isEmpty(sql)) {
            return AjaxResult.error(MessageUtils.message("goview.project.data.execute.sql.failed"));
        }
        return goviewProjectDataService.executeSql(sql);
    }
}
