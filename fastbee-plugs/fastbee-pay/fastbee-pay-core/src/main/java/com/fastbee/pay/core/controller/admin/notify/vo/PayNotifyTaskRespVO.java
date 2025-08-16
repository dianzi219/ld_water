package com.fastbee.pay.core.controller.admin.notify.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 回调通知 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskRespVO extends PayNotifyTaskBaseVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "3380")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "应用名称", example = "wx_pay")
    private String  appName;

}
