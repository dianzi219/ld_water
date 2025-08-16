package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.DeviceTestReportBo;

/**
 * @author bill
 */
public interface IDeviceTestService {

    public void messageHandler(DeviceTestReportBo testReportBo);
}
