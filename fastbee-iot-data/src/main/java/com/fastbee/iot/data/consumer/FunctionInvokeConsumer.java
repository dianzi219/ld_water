package com.fastbee.iot.data.consumer;

import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 指令(服务)下发处理类
 *
 * @author gsb
 * @date 2022/10/11 8:17
 */
@Slf4j
@Component
public class FunctionInvokeConsumer {


    @Autowired
    private IMqttMessagePublish functionSendService;

    @Async(FastBeeConstant.TASK.FUNCTION_INVOKE_TASK)
    public void handler(MQSendMessageBo bo) {
        try {
            functionSendService.funcSend(bo);
        } catch (Exception e) {
            log.error("=>服务下发异常", e);
        }
    }
}
