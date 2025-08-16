package com.fastbee.mq.queue;

import com.fastbee.common.core.mq.DeviceTestReportBo;
import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author bill
 */
public class DeviceTestQueue {


    private static final LinkedBlockingQueue<DeviceTestReportBo> queue = new LinkedBlockingQueue<>();

    /*元素加入队列,最后*/
    public static void offer(DeviceTestReportBo dto){
        queue.offer(dto);
    }
    /*取出队列元素 先进先出*/
    @SneakyThrows
    public static DeviceTestReportBo take(){
        return queue.take();
    }
}
