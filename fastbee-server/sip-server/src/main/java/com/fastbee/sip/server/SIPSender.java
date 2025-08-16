package com.fastbee.sip.server;

import gov.nist.javax.sip.SipProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

@Slf4j
@Component
public class SIPSender {
    @Autowired
    private SipLayer sipLayer;

    public void transmitRequest(String ip, Message message) throws SipException {
            ViaHeader viaHeader = (ViaHeader)message.getHeader(ViaHeader.NAME);
            String transport = "UDP";
            if (viaHeader == null) {
                log.warn("[消息头缺失]： ViaHeader， 使用默认的UDP方式处理数据");
            }else {
                transport = viaHeader.getTransport();
            }

            if ("TCP".equals(transport)) {
                SipProviderImpl tcpSipProvider = sipLayer.getTcpSipProvider(ip);
                if (tcpSipProvider == null) {
                    log.error("[发送信息失败] 未找到tcp://{}的监听信息", ip);
                    return;
                }
                if (message instanceof Request) {
                    tcpSipProvider.sendRequest((Request)message);
                }else if (message instanceof Response) {
                    tcpSipProvider.sendResponse((Response)message);
                }

            } else if ("UDP".equals(transport)) {
                SipProviderImpl sipProvider = sipLayer.getUdpSipProvider(ip);
                if (sipProvider == null) {
                    log.error("[发送信息失败] 未找到udp://{}的监听信息", ip);
                    return;
                }
                if (message instanceof Request) {
                    sipProvider.sendRequest((Request)message);
                }else if (message instanceof Response) {
                    sipProvider.sendResponse((Response)message);
                }
            }
    }

    public CallIdHeader getNewCallIdHeader(String ip, String transport){
        if (ObjectUtils.isEmpty(transport)) {
            return sipLayer.getUdpSipProvider().getNewCallId();
        }
        SipProviderImpl sipProvider;
        if (ObjectUtils.isEmpty(ip)) {
            sipProvider = transport.equalsIgnoreCase("TCP") ? sipLayer.getTcpSipProvider()
                    : sipLayer.getUdpSipProvider();
        }else {
            sipProvider = transport.equalsIgnoreCase("TCP") ? sipLayer.getTcpSipProvider(ip)
                    : sipLayer.getUdpSipProvider(ip);
        }

        if (sipProvider == null) {
            sipProvider = sipLayer.getUdpSipProvider();
        }

        if (sipProvider != null) {
            return sipProvider.getNewCallId();
        } else {
            log.warn("[新建CallIdHeader失败]， ip={}, transport={}", ip, transport);
            return null;
        }
    }
}
