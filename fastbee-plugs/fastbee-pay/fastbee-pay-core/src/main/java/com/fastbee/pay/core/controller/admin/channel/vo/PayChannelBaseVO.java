package com.fastbee.pay.core.controller.admin.channel.vo;

import com.fastbee.common.enums.CommonStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 支付渠道 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayChannelBaseVO {

    @ApiModelProperty(value = "开启状态", required = true, example = "1")
    @NotNull(message = "开启状态不能为空")
//    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "备注", example = "我是小备注")
    private String remark;

    @ApiModelProperty(value = "渠道费率，单位：百分比", required = true, example = "10")
    @NotNull(message = "渠道费率，单位：百分比不能为空")
    private Double feeRate;

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    @NotNull(message = "应用编号不能为空")
    private Long appId;

}
