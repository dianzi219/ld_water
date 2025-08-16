package com.fastbee.sip.handler.req;

import com.fastbee.sip.handler.IReqHandler;
import com.fastbee.sip.server.IGBListener;
import com.fastbee.sip.util.XmlUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

@Slf4j
@Component
public class NotifyReqHandler extends ReqAbstractHandler implements InitializingBean, IReqHandler {

    @Autowired
    private IGBListener sipListener;
    @Override
    public void processMsg(RequestEvent evt) {
        log.info("接收到NOTIFY信息");
        log.info("NOTIFY信息:{}",evt.getRequest());
        try {
            responseAck(evt);
            Element rootElement = getRootElement(evt);
            if (rootElement == null) {
                log.error("处理NOTIFY消息时未获取到消息体,{}", evt.getRequest());
                responseAck((SIPRequest) evt.getRequest(), Response.OK, null, null);
                return;
            }
            String cmd = XmlUtil.getText(rootElement, "CmdType");
            log.info("cmd:{}",cmd);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[回复ACK信息失败]，{}", e.getMessage());
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String method = "NOTIFY";
        sipListener.addRequestProcessor(method, this);
    }
}
