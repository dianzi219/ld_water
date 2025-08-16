package com.fastbee.pay.core.controller.admin.refund.vo;

import com.fastbee.common.core.domain.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.fastbee.common.utils.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;


@ApiModel(description = "管理后台 - 退款订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundPageReqVO extends PageParam {

    @ApiModelProperty(value = "应用编号", example = "1024")
    private Long appId;

    @ApiModelProperty(value = "渠道编码", example = "wx_app")
    private String channelCode;

    @ApiModelProperty(value = "商户支付单号", example = "10")
    private String merchantOrderId;

    @ApiModelProperty(value = "商户退款单号", example = "20")
    private String merchantRefundId;

    @ApiModelProperty(value = "渠道支付单号", example = "30")
    private String channelOrderNo;

    @ApiModelProperty(value = "渠道退款单号", example = "40")
    private String channelRefundNo;

    @ApiModelProperty(value = "退款状态", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime[] createTime;

}
