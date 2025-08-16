package com.fastbee.iot.model;

import com.fastbee.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建固件升级任务对象对象 iot_firmware_task
 *
 * @author kami
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FirmwareTaskDetailOutput extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 关联固件ID */
    private Long firmwareId;

    /**
     * 任务id
     */
    private Long taskId;

    /** 任务名称 */
    private String taskName;

    /** 设备名称 */
    private String deviceName;

    /** 设备序列号 */
    private String serialNumber;

    /** 消息ID */
    private String messageId;

    /** 当前固件版本 */
    private String version;

    /** 0:等待升级 1:已发送设备 2:设备收到  3:升级成功 4:升级失败 */
    private Integer upgradeStatus;

    /** 描述 */
    private String detailMsg;

    /** 升级进度 */
    private int progress;

}
