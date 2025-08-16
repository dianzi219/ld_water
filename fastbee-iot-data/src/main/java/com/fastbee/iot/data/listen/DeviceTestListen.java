package com.fastbee.iot.data.listen;

import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceTestReportBo;
import com.fastbee.iot.data.consumer.DeviceTestConsumer;
import com.fastbee.mq.queue.DeviceTestQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author bill
 */
@Component
@Slf4j
public class DeviceTestListen {

    @Resource
    private DeviceTestConsumer deviceTestConsumer;

    @Async(FastBeeConstant.TASK.DEVICE_TEST_TASK)
    public void listen(){
        while (true){
            try {
                DeviceTestReportBo take = DeviceTestQueue.take();

                deviceTestConsumer.consume(take);
            }catch (Exception e){
                log.error("=>emq数据转发异常");
            }
        }
    }
}
