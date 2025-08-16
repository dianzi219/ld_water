package com.fastbee.pay.core.controller.admin.order.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "管理后台 - 支付订单提交 Response VO")
@Data
public class PayOrderSubmitRespVO {

    @ApiModelProperty(value = "支付状态", required = true, example = "10") // 参见 PayOrderStatusEnum 枚举
    private Integer status;

    @ApiModelProperty(value = "展示模式", required = true, example = "url") // 参见 PayDisplayModeEnum 枚举
    private String displayMode;

    @ApiModelProperty(value = "展示内容", required = true)
    private String displayContent;

}
