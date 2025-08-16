package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 支付应用信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppRespVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
