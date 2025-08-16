package com.fastbee.zqwl;

import lombok.Data;

import java.util.List;

/**
 * 设备DI DO ABC ，脉冲数据
 * @author gsb
 * @date 2024/3/13 11:41
 */
@Data
public class ZQWLDIDORet {

    /**
     *设备地址
     */
    private Integer addr;
    /**
     * 设备应答命令
     */
    private String cmd;
    /**
     * 8路DI状态
     */
    private List<Integer> x;
    /**
     * 8路DO状态
     */
    private List<Integer> y;
    /**
     * 8路DI脉冲计算值
     */
    private List<Integer> count;
    /**
     * 2路ADC值
     */
    private List<Integer> abc;

}
