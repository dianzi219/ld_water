package com.fastbee.http.handler.req;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.http.service.IHttpMqttService;
import com.fastbee.http.handler.IHttpReqHandler;
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
import java.util.List;

@Slf4j
@Component
public class EventHttpReqHandler implements InitializingBean, IHttpReqHandler {
    @Autowired
    private HttpListener httpListener;

    @Autowired
    private IHttpMqttService mqttService;

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        String uri = "/event/post";
        httpListener.addRequestProcessor(uri, this);
    }

    @Override
    public void processMsg(FullHttpRequest req, HttpSession session) {
        String serialNumber = (String) session.getAttribute("SerialNumber");
        Device device = deviceService.selectDeviceBySerialNumber(serialNumber);
        if (device != null) {
            List<ThingsModelSimpleItem> thingsModelSimpleItems = JSON.parseArray(req.content().toString(StandardCharsets.UTF_8), ThingsModelSimpleItem.class);
            mqttService.publishEvent(device,thingsModelSimpleItems);
        }
    }
}
