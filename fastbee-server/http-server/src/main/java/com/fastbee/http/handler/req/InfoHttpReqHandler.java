package com.fastbee.http.handler.req;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.http.service.IHttpMqttService;
import com.fastbee.http.handler.IHttpReqHandler;
import com.fastbee.http.manager.HttpSessionManager;
import com.fastbee.http.server.HttpListener;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.service.IDeviceService;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class InfoHttpReqHandler implements InitializingBean, IHttpReqHandler {
    @Autowired
    private HttpListener httpListener;

    @Autowired
    private HttpSessionManager sessionManager;

    @Autowired
    private IHttpMqttService mqttService;

    @Autowired
    private IDeviceService deviceService;
    @Override
    public void afterPropertiesSet() throws Exception {
        String uri = "/info/post";
        httpListener.addRequestProcessor(uri, this);
    }

    @Override
    public void processMsg(FullHttpRequest req, HttpSession session) {
        //设备上报基本信息后保存到会话中
        Device device = JSON.parseObject(req.content().toString(StandardCharsets.UTF_8), Device.class);
        session.setAttribute("SerialNumber", device.getSerialNumber());
        session.setAttribute("productId", device.getProductId());
        device.setActiveTime(DateUtils.getNowDate());
        device.setStatus(DeviceStatus.ONLINE.getType());
        sessionManager.saveSession(session.getId(), session);
        //更新设备信息
        deviceService.updateDevice(device);
        //发布到mqtt
        mqttService.publishInfo(device);
    }
}
