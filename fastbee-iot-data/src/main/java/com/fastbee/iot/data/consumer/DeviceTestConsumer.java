package com.fastbee.iot.data.consumer;

import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceTestReportBo;
import com.fastbee.iot.data.service.IDeviceTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author bill
 */
@Slf4j
@Component
public class DeviceTestConsumer {

    @Resource
    private IDeviceTestService testHandler;

    @Async(FastBeeConstant.TASK.DEVICE_TEST_TASK)
    public void consume(DeviceTestReportBo bo){
        try {
            //处理emq订阅的非 property/post 属性上报的消息 ，因为其他消息量小，放在一起处理
            testHandler.messageHandler(bo);
        }catch (Exception e){
            log.error("=>设备其他消息处理出错",e);
        }
    }
}
