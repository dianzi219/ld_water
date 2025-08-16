package com.fastbee.pay.core.controller.app.order.vo;

import com.fastbee.pay.core.controller.admin.order.vo.PayOrderSubmitReqVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(description = "用户 APP - 支付订单提交 Request VO")
@Data
public class AppPayOrderSubmitReqVO  extends PayOrderSubmitReqVO {
}
