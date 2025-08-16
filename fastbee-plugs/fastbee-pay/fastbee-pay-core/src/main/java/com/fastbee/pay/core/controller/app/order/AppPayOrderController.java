package com.fastbee.pay.core.controller.app.order;

import com.fastbee.common.core.domain.CommonResult;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderRespVO;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderSubmitRespVO;
import com.fastbee.pay.core.controller.app.order.vo.AppPayOrderSubmitReqVO;
import com.fastbee.pay.core.controller.app.order.vo.AppPayOrderSubmitRespVO;
import com.fastbee.pay.core.convert.order.PayOrderConvert;
import com.fastbee.pay.core.service.order.PayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.fastbee.common.core.domain.CommonResult.success;
import static com.fastbee.common.utils.ServletUtils.getClientIP;


@Api(tags = "用户 APP - 支付订单")
@RestController
@RequestMapping("/pay/order")
@Validated
@Slf4j
public class AppPayOrderController {

    @Resource
    private PayOrderService payOrderService;

    // TODO fastbee：临时 demo，技术打样。
    @GetMapping("/get")
    @ApiOperation("获得支付订单")
    @ApiParam(name = "id", value = "编号", required = true, example = "1024")
    public CommonResult<PayOrderRespVO> getOrder(@RequestParam("id") Long id) {
        return success(PayOrderConvert.INSTANCE.convert(payOrderService.getOrder(id)));
    }

    @PostMapping("/submit")
    @ApiOperation("提交支付订单")
    public CommonResult<AppPayOrderSubmitRespVO> submitPayOrder(@RequestBody AppPayOrderSubmitReqVO reqVO) {
        PayOrderSubmitRespVO respVO = payOrderService.submitOrder(reqVO, getClientIP());
        return success(PayOrderConvert.INSTANCE.convert3(respVO));
    }

}
