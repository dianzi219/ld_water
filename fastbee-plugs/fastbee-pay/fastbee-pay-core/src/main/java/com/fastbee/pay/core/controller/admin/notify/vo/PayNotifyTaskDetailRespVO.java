
package com.fastbee.pay.core.controller.admin.notify.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(description = "管理后台 - 回调通知的明细 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskDetailRespVO extends PayNotifyTaskBaseVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "3380")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", required = true)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "应用名称", example = "wx_pay")
    private String appName;

    @ApiModelProperty(value = "回调日志列表")
    private List<Log> logs;

    @ApiModel(description = "管理后台 - 回调日志")
    @Data
    public static class Log {

        @ApiModelProperty(value = "日志编号", required = true, example = "8848")
        private Long id;

        @ApiModelProperty(value = "通知状态", required = true, example = "1")
        private Byte status;

        @ApiModelProperty(value = "当前通知次数", required = true)
        private Byte notifyTimes;

        @ApiModelProperty(value = "HTTP 响应结果", required = true)
        private String response;

        @ApiModelProperty(value = "创建时间", required = true)
        private LocalDateTime createTime;

    }

}
