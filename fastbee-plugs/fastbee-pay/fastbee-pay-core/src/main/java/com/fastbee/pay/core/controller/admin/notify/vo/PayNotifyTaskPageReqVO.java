package com.fastbee.pay.core.controller.admin.notify.vo;

import com.fastbee.common.core.domain.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.fastbee.common.utils.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;


@ApiModel(description = "管理后台 - 回调通知分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskPageReqVO extends PageParam {

    @ApiModelProperty(value = "应用编号", example = "10636")
    private Long appId;

    @ApiModelProperty(value = "通知类型", example = "2")
    private Integer type;

    @ApiModelProperty(value = "数据编号", example = "6722")
    private Long dataId;

    @ApiModelProperty(value = "通知状态", example = "1")
    private Integer status;

    @ApiModelProperty(value = "商户订单编号", example = "26697")
    private String merchantOrderId;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
