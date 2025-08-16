package com.fastbee.pay.core.controller.admin.channel;

import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelCreateReqVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelRespVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelUpdateReqVO;
import com.fastbee.pay.core.convert.channel.PayChannelConvert;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.service.channel.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.utils.collection.CollectionUtils.convertSet;

@Api(tags = "管理后台 - 支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class PayChannelController {

    @Resource
    private PayChannelService channelService;

    @PostMapping("/create")
    @ApiOperation("创建支付渠道")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody PayChannelCreateReqVO createReqVO) {
        return success(channelService.createChannel(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付渠道")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody PayChannelUpdateReqVO updateReqVO) {
        channelService.updateChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付渠道")
    @ApiParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付渠道")
    @ApiParam(name = "id", value = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PayChannelRespVO> getChannel(@RequestParam(value = "id", required = false) Long id,
                                                     @RequestParam(value = "appId", required = false) Long appId,
                                                     @RequestParam(value = "code", required = false) String code) {
        PayChannel channel = null;
        if (id != null) {
            channel = channelService.getChannel(id);
        } else if (appId != null && code != null) {
            channel = channelService.getChannelByAppIdAndCode(appId, code);
        }
        return success(new PayChannelRespVO());
//        return success(PayChannelConvert.INSTANCE.convert(channel));
    }

    @GetMapping("/get-enable-code-list")
    @ApiOperation("获得指定应用的开启的支付渠道编码列表")
    @ApiParam(name = "appId", value = "应用编号", required = true, example = "1")
    public CommonResult<Set<String>> getEnableChannelCodeList(@RequestParam("appId") Long appId) {
        List<PayChannel> channels = channelService.getEnableChannelList(appId);
        return success(convertSet(channels, PayChannel::getCode));
    }

}
