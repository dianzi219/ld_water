package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel(description = "管理后台 - 支付应用信息更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppUpdateReqVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    @NotNull(message = "应用编号不能为空")
    private Long id;

}
