package com.fastbee.pay.core.controller.admin.app.vo;

import com.fastbee.common.core.domain.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.fastbee.common.utils.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;


@ApiModel(description = "管理后台 - 支付应用信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppPageReqVO extends PageParam {

    @ApiModelProperty(value = "应用名", example = "小豆")
    private String name;

    @ApiModelProperty(value = "开启状态", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime[] createTime;

}
