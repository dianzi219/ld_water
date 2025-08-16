package com.fastbee.pay.core.controller.admin.notify;

import cn.hutool.core.collection.CollUtil;
import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.pay.core.controller.admin.notify.vo.PayNotifyTaskDetailRespVO;
import com.fastbee.pay.core.controller.admin.notify.vo.PayNotifyTaskPageReqVO;
import com.fastbee.pay.core.controller.admin.notify.vo.PayNotifyTaskRespVO;
import com.fastbee.pay.core.convert.notify.PayNotifyTaskConvert;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.notify.PayNotifyLog;
import com.fastbee.pay.core.domain.dataobject.notify.PayNotifyTask;
import com.fastbee.pay.core.service.app.PayAppService;
import com.fastbee.pay.core.service.notify.PayNotifyService;
import com.fastbee.pay.core.service.order.PayOrderService;
import com.fastbee.pay.core.service.refund.PayRefundService;
import com.fastbee.pay.framework.client.PayClient;
import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.client.dto.order.PayOrderRespDTO;
import com.fastbee.pay.framework.client.dto.refund.PayRefundRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.common.utils.collection.CollectionUtils.convertList;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_NOT_FOUND;


@Api(tags = "管理后台 - 回调通知")
@RestController
@RequestMapping("/pay/notify")
@Validated
@Slf4j
public class PayNotifyController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayRefundService refundService;
    @Resource
    private PayNotifyService notifyService;
    @Resource
    private PayAppService appService;

    @Resource
    private PayClientFactory payClientFactory;

    @PostMapping(value = "/order/{channelId}")
    @ApiOperation("支付渠道的统一【支付】回调")
    @PermitAll
//    @OperateLog(enable = false) // 回调地址，无需记录操作日志
    public String notifyOrder(@PathVariable("channelId") Long channelId,
                              @RequestParam(required = false) Map<String, String> params,
                              @RequestBody(required = false) String body) {
        log.info("[notifyOrder][channelId({}) 回调数据({}/{})]", channelId, params, body);
        // 1. 校验支付渠道是否存在
        PayClient payClient = payClientFactory.getPayClient(channelId);
        if (payClient == null) {
            log.error("[notifyCallback][渠道编号({}) 找不到对应的支付客户端]", channelId);
            throw exception(CHANNEL_NOT_FOUND);
        }

        // 2. 解析通知数据
        PayOrderRespDTO notify = payClient.parseOrderNotify(params, body);
        orderService.notifyOrder(channelId, notify);
        return MessageUtils.message("success");
    }

    @PostMapping(value = "/refund/{channelId}")
    @ApiOperation("支付渠道的统一【退款】回调")
    @PermitAll
//    @OperateLog(enable = false) // 回调地址，无需记录操作日志
    public String notifyRefund(@PathVariable("channelId") Long channelId,
                              @RequestParam(required = false) Map<String, String> params,
                              @RequestBody(required = false) String body) {
        log.info("[notifyRefund][channelId({}) 回调数据({}/{})]", channelId, params, body);
        // 1. 校验支付渠道是否存在
        PayClient payClient = payClientFactory.getPayClient(channelId);
        if (payClient == null) {
            log.error("[notifyCallback][渠道编号({}) 找不到对应的支付客户端]", channelId);
            throw exception(CHANNEL_NOT_FOUND);
        }

        // 2. 解析通知数据
        PayRefundRespDTO notify = payClient.parseRefundNotify(params, body);
        refundService.notifyRefund(channelId, notify);
        return MessageUtils.message("success");
    }

    @GetMapping("/get-detail")
    @ApiOperation("获得回调通知的明细")
    @ApiParam(name = "id", value = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:notify:query')")
    public CommonResult<PayNotifyTaskDetailRespVO> getNotifyTaskDetail(@RequestParam("id") Long id) {
        PayNotifyTask task = notifyService.getNotifyTask(id);
        if (task == null) {
            return success(null);
        }
        // 拼接返回
        PayApp app = appService.getApp(task.getAppId());
        List<PayNotifyLog> logs = notifyService.getNotifyLogList(id);
        return success(PayNotifyTaskConvert.INSTANCE.convert(task, app, logs));
    }

    @GetMapping("/page")
    @ApiOperation("获得回调通知分页")
    @PreAuthorize("@ss.hasPermission('pay:notify:query')")
    public CommonResult<PageResult<PayNotifyTaskRespVO>> getNotifyTaskPage(@Valid PayNotifyTaskPageReqVO pageVO) {
        PageResult<PayNotifyTask> pageResult = notifyService.getNotifyTaskPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 拼接返回
        Map<Long, PayApp> appMap = appService.getAppMap(convertList(pageResult.getList(), PayNotifyTask::getAppId));
        return success(PayNotifyTaskConvert.INSTANCE.convertPage(pageResult, appMap));
    }

}
