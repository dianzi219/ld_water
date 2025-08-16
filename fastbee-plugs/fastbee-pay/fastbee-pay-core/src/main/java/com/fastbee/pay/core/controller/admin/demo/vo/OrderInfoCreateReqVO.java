package com.fastbee.pay.core.controller.admin.demo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel(description = "管理后台 - 示例订单创建 Request VO")
@Data
public class OrderInfoCreateReqVO {

    @ApiModelProperty(value = "商品编号", required = true, example = "17682")
    @NotNull(message = "商品编号不能为空")
    private Long spuId;

}
