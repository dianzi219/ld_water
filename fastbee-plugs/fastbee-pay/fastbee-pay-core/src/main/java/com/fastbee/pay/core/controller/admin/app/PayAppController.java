package com.fastbee.pay.core.controller.admin.app;

import cn.hutool.core.collection.CollUtil;
import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.core.controller.admin.app.vo.*;
import com.fastbee.pay.core.convert.app.PayAppConvert;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.service.app.PayAppService;
import com.fastbee.pay.core.service.channel.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.utils.collection.CollectionUtils.convertList;


@Slf4j
@Api(tags = "管理后台 - 支付应用信息")
@RestController
@RequestMapping("/pay/app")
@Validated
public class PayAppController {

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;

    @PostMapping("/create")
    @ApiOperation("创建支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:create')")
    public CommonResult<Long> createApp(@Valid @RequestBody PayAppCreateReqVO createReqVO) {
        return success(appService.createApp(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateApp(@Valid @RequestBody PayAppUpdateReqVO updateReqVO) {
        appService.updateApp(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @ApiOperation("更新支付应用状态")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateAppStatus(@Valid @RequestBody PayAppUpdateStatusReqVO updateReqVO) {
        appService.updateAppStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付应用信息")
    @ApiParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:app:delete')")
    public CommonResult<Boolean> deleteApp(@RequestParam("id") Long id) {
        appService.deleteApp(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付应用信息")
    @ApiParam(name = "id", value = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PayAppRespVO> getApp(@RequestParam("id") Long id) {
        PayApp app = appService.getApp(id);
        return success(PayAppConvert.INSTANCE.convert(app));
    }

    @GetMapping("/page")
    @ApiOperation("获得支付应用信息分页")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PageResult<PayAppPageItemRespVO>> getAppPage(@Valid PayAppPageReqVO pageVO) {
        // 得到应用分页列表
        PageResult<PayApp> pageResult = appService.getAppPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 得到所有的应用编号，查出所有的渠道
        Collection<Long> appIds = convertList(pageResult.getList(), PayApp::getId);
        List<PayChannel> channels = channelService.getChannelListByAppIds(appIds);

        // 拼接后返回
        return success(PayAppConvert.INSTANCE.convertPage(pageResult, channels));
    }

    @GetMapping("/list")
    @ApiOperation("获得应用列表")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<PayAppRespVO>> getAppList() {
        List<PayApp> appListDO = appService.getAppList();
        return success(PayAppConvert.INSTANCE.convertList(appListDO));
    }

}
