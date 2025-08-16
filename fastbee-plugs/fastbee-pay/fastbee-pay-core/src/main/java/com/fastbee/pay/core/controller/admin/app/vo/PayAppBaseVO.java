package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

/**
* 支付应用信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayAppBaseVO {

    @ApiModelProperty(value = "应用名", required = true, example = "小豆")
    @NotNull(message = "应用名不能为空")
    private String name;

    @ApiModelProperty(value = "开启状态", required = true, example = "0")
    @NotNull(message = "开启状态不能为空")
//    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "备注", example = "我是一个测试应用")
    private String remark;

    @ApiModelProperty(value = "支付结果的回调地址", required = true, example = "http://127.0.0.1:48080/pay-callback")
    @NotNull(message = "支付结果的回调地址不能为空")
    @URL(message = "支付结果的回调地址必须为 URL 格式")
    private String orderNotifyUrl;

    @ApiModelProperty(value = "退款结果的回调地址", required = true, example = "http://127.0.0.1:48080/refund-callback")
    @NotNull(message = "退款结果的回调地址不能为空")
    @URL(message = "退款结果的回调地址必须为 URL 格式")
    private String refundNotifyUrl;

}
