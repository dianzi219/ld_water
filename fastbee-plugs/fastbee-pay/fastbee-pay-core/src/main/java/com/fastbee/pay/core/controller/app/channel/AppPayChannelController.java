package com.fastbee.pay.core.controller.app.channel;

import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.service.channel.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.utils.collection.CollectionUtils.convertSet;


@Api(tags = "用户 App - 支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class AppPayChannelController {

    @Resource
    private PayChannelService channelService;

    @GetMapping("/get-enable-code-list")
    @ApiOperation("获得指定应用的开启的支付渠道编码列表")
    @ApiParam(name = "appId", value = "应用编号", required = true, example = "1")
    public CommonResult<Set<String>> getEnableChannelCodeList(@RequestParam("appId") Long appId) {
        List<PayChannel> channels = channelService.getEnableChannelList(appId);
        return success(convertSet(channels, PayChannel::getCode));
    }

}
