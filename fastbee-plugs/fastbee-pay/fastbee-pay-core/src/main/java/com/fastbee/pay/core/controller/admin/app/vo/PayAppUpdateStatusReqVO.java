package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel(description = "管理后台 - 应用更新状态 Request VO")
@Data
public class PayAppUpdateStatusReqVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    @NotNull(message = "应用编号不能为空")
    private Long id;

    @ApiModelProperty(value = "状态，见 SysCommonStatusEnum 枚举", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
