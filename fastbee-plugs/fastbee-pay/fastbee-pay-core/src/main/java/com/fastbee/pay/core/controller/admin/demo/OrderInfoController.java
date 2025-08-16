package com.fastbee.pay.core.controller.admin.demo;

import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.common.core.domain.PageParam;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.api.api.notify.dto.PayOrderNotifyReqDTO;
import com.fastbee.pay.api.api.notify.dto.PayRefundNotifyReqDTO;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoCreateReqVO;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoRespVO;
import com.fastbee.pay.core.convert.demo.OrderInfoConvert;
import com.fastbee.pay.core.domain.dataobject.demo.OrderInfo;
import com.fastbee.pay.core.service.demo.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.utils.SecurityUtils.getLoginUser;
import static com.fastbee.common.utils.ServletUtils.getClientIP;

@Api(tags = "管理后台 - 示例订单")
@RestController
@RequestMapping("/order")
@Validated
public class OrderInfoController {

    @Resource
    private OrderInfoService OrderInfoService;

    @PostMapping("/create")
    @ApiOperation("创建示例订单")
    public CommonResult<Long> createOrder(@Valid @RequestBody OrderInfoCreateReqVO createReqVO) {
        return success(OrderInfoService.createOrder(getLoginUser().getUserId(), createReqVO));
    }

    @GetMapping("/page")
    @ApiOperation("获得示例订单分页")
    public CommonResult<PageResult<OrderInfoRespVO>> getOrderPage(@Valid PageParam pageVO) {
        PageResult<OrderInfo> pageResult = OrderInfoService.getOrderPage(pageVO);
        return success(OrderInfoConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/update-paid")
    @ApiOperation("更新示例订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 OrderInfoService 内部校验实现
//    @OperateLog(enable = false) // 禁用操作日志，因为没有操作人
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        OrderInfoService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @PutMapping("/refund")
    @ApiOperation("发起示例订单的退款")
    @ApiParam(name = "id", value = "编号", required = true, example = "1024")
    public CommonResult<Boolean> refundOrder(@RequestParam("id") Long id) {
        OrderInfoService.refundOrder(id, getClientIP());
        return success(true);
    }

    @PostMapping("/update-refunded")
    @ApiOperation("更新示例订单为已退款") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 OrderInfoService 内部校验实现
//    @OperateLog(enable = false) // 禁用操作日志，因为没有操作人
    public CommonResult<Boolean> updateOrderRefunded(@RequestBody PayRefundNotifyReqDTO notifyReqDTO) {
        OrderInfoService.updateOrderRefunded(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayRefundId());
        return success(true);
    }

}
