package com.fastbee.common.core.mq.ota;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * OTA远程升级
 * @author gsb
 * @date 2022/10/10 10:22
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OtaUpgradeBo {

    // 设备编码
    private String serialNumber;
    // 升级任务ID
    private Long taskId;
    // 消息内容
    private byte[] msg;
    // 设备状态
    private int status;
    // topic
    private String topicName;
    // 固件包版本
    private BigDecimal version;
    // 固件包http地址
    private String url;
    // 固件包分包传输大小
    private int packageSize;
    // 固件包传输偏移量
    private int offset;
    // 升级进度
    private int progress;

}
