package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@ApiModel(description = "管理后台 - 支付应用信息分页查询 Response VO,相比于支付信息，还会多出应用渠道的开关信息")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppPageItemRespVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "已配置的支付渠道编码", required = true, example = "[alipay_pc, alipay_wap]")
    private Set<String> channelCodes;

}
