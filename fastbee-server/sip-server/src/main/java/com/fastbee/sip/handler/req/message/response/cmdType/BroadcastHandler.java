package com.fastbee.sip.handler.req.message.response.cmdType;

import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.handler.req.ReqAbstractHandler;
import com.fastbee.sip.handler.req.message.IMessageHandler;
import com.fastbee.sip.handler.req.message.response.ResponseMessageHandler;
import com.fastbee.sip.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import java.text.ParseException;

@Slf4j
@Component
public class BroadcastHandler extends ReqAbstractHandler implements InitializingBean, IMessageHandler {

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Override
    public void handlerCmdType(RequestEvent evt, SipDevice device, Element element) {
        try {
            Element rootElement = getRootElement(evt);
            String channelId = XmlUtil.getText(rootElement, "DeviceID");
            ///todo 相同请求是否存在
            String result = XmlUtil.getText(rootElement, "Result");
            Element infoElement = rootElement.element("Info");
            String reason = null;
            if (infoElement != null) {
                reason = XmlUtil.getText(infoElement, "Reason");
            }
            log.info("[语音广播]回复：{}, {}/{}", reason == null? result : result + ": " + reason, device.getDeviceId(), channelId);
            // 回复200 OK
            responseAck(evt);
        } catch (DocumentException | SipException | InvalidArgumentException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String cmdType = "Broadcast";
        responseMessageHandler.addHandler(cmdType, this);
    }
}
