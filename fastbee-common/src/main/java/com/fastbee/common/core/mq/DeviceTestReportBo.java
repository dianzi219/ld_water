package com.fastbee.common.core.mq;

import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import lombok.Data;

import java.util.List;

/**
 * @author bill
 */
@Data
public class DeviceTestReportBo {

    private Long productId;
    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 是否是回复数据
     */
    private Boolean isReply;

    /**
     * 设备物模型值的集合
     */
    private List<ThingsModelSimpleItem> thingsModelSimpleItem;
    /**
     * 原报文
     */
    private Object sources;
}
