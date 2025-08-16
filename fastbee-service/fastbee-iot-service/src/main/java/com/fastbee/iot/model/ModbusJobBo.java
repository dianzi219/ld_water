package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author gsb
 * @date 2024/7/2 13:48
 */
@Data
public class ModbusJobBo {
    /**
     * 任务名
     */
    private String jobName;
    /**
     * 执行指令
     */
    private String command;
}
