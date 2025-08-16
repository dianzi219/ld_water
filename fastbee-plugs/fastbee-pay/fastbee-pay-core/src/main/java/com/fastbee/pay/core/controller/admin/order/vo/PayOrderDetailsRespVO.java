package com.fastbee.pay.core.controller.admin.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 支付订单详细信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderDetailsRespVO extends PayOrderBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "应用名称", required = true, example = "fastbee")
    private String appName;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", required = true)
    private LocalDateTime updateTime;

    /**
     * 支付订单扩展
     */
    private PayOrderExtension extension;

    @Data
    @ApiModel(description = "支付订单扩展")
    public static class PayOrderExtension {

        @ApiModelProperty(value = "支付订单号", required = true, example = "1024")
        private String no;

        @ApiModelProperty(value = "支付异步通知的内容")
        private String channelNotifyData;

    }

}
