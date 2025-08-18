package com.fastbee.slt427.model;

import com.fastbee.base.core.annotation.Node;
import com.fastbee.base.core.annotation.PakMapping;
import com.fastbee.base.session.Session;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.fastbee.modbus.pak.TcpDtu.心跳包;
import static com.fastbee.modbus.pak.TcpDtu.注册报文;

/**
 * @author gsb
 * @date 2023/5/19 14:09
 */
@Component
@Slf4j
@Node
public class FlowEndPoint {

    @PakMapping(types = 注册报文)
    public Message register(DeviceReport message, Session session){
        //检测设备是否注册，未注册，进行注册
        if (!session.isRegistered()){
            // 注册设备
            session.register(message);
        }
        return message;
    }
    
    @PakMapping(types = 心跳包)
    public void heartbeat(DeviceReport message, Session session){
        // 心跳包处理，更新会话访问时间以保持连接活跃
        session.access();
        log.info("=>SLT427设备心跳包，设备ID: {}", session.getClientId());
    }
}