package com.fastbee.iot.data;

import com.fastbee.iot.data.listen.*;
import com.fastbee.mqttclient.PubMqttClient;
import com.fastbee.protocol.service.IProtocolManagerService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 启动类
 *
 * @author bill
 */
@Component
@Slf4j
@Order(2)
public class StartBoot implements ApplicationRunner {

    @Autowired
    private PubMqttClient mqttClient;
    @Autowired
    private DeviceReplyListen replyListen;
    @Autowired
    private DeviceReportListen reportListen;
    @Autowired
    private DeviceStatusListen statusListen;
    @Autowired
    private DevicePropFetchListen propFetchListen;
    @Autowired
    private UpgradeListen upgradeListen;
    @Autowired
    private FunctionInvokeListen invokeListen;
    @Resource
    private DeviceOtherListen otherListen;
    @Resource
    private DeviceTestListen testListen;
    @Resource
    private IProtocolManagerService protocolManagerService;
    @Resource
    private IMqttMessageListener subscribeCallback;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            new Thread(() -> {
                replyListen.listen();
                reportListen.listen();
                statusListen.listen();
                propFetchListen.listen();
                upgradeListen.listen();
                invokeListen.listen();
                otherListen.listen();
                testListen.listen();
                /*启动内部客户端,用来下发客户端服务*/
                mqttClient.setListener(subscribeCallback);
                mqttClient.initialize();
                protocolManagerService.getAllProtocols();
                log.info("=>设备监听队列启动成功");
            }).start();
        } catch (Exception e) {
            log.error("=>客户端启动失败:{}", e.getMessage(),e);
        }
    }
}
