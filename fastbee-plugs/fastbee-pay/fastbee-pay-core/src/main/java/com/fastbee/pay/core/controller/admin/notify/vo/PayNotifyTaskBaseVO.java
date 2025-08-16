package com.fastbee.pay.core.controller.admin.notify.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回调通知 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PayNotifyTaskBaseVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "10636")
    private Long appId;

    @ApiModelProperty(value = "通知类型", required = true, example = "2")
    private Byte type;

    @ApiModelProperty(value = "数据编号", required = true, example = "6722")
    private Long dataId;

    @ApiModelProperty(value = "通知状态", required = true, example = "1")
    private Byte status;

    @ApiModelProperty(value = "商户订单编号", required = true, example = "26697")
    private String merchantOrderId;

    @ApiModelProperty(value = "下一次通知时间", required = true)
    private LocalDateTime nextNotifyTime;

    @ApiModelProperty(value = "最后一次执行时间", required = true)
    private LocalDateTime lastExecuteTime;

    @ApiModelProperty(value = "当前通知次数", required = true)
    private Byte notifyTimes;

    @ApiModelProperty(value = "最大可通知次数", required = true)
    private Byte maxNotifyTimes;

    @ApiModelProperty(value = "异步通知地址", required = true, example = "https://www.iocoder.cn")
    private String notifyUrl;

}
