package com.fastbee.mq.queue;

import com.fastbee.iot.model.modbus.ModbusPollJob;
import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 设备属性获取存储列队
 * @author gsb
 * @date 2022/10/11 8:29
 */
public class DevicePropFetchQueue {
    private static final LinkedBlockingQueue<ModbusPollJob> queue = new LinkedBlockingQueue<>();

    /*元素加入队列,最后*/
    public static void offer(ModbusPollJob deviceJob){
        queue.offer(deviceJob);
    }
    /*取出队列元素 先进先出*/
    @SneakyThrows
    public static ModbusPollJob take(){
        return queue.take();
    }
}
