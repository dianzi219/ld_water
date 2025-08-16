package com.fastbee.sip.handler.res;

import com.fastbee.sip.domain.SipConfig;
import com.fastbee.sip.handler.IResHandler;
import com.fastbee.sip.model.GbSdp;
import com.fastbee.sip.server.IGBListener;
import com.fastbee.sip.server.ReqMsgHeaderBuilder;
import com.fastbee.sip.server.SIPSender;
import com.fastbee.sip.service.ISipConfigService;
import com.fastbee.sip.util.SipUtil;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;

@Slf4j
@Component
public class InviteResHandler implements InitializingBean,IResHandler {

    @Autowired
    private IGBListener sipListener;

    @Autowired
    private SIPSender sipSender;

    @Autowired
    private ReqMsgHeaderBuilder headerBuilder;

    @Autowired
    private ISipConfigService sipConfigService;

    @Override
    public void processMsg(ResponseEvent evt) throws ParseException {
        try {
            SIPResponse response = (SIPResponse)evt.getResponse();
            FromHeader fromHeader = (FromHeader) response.getHeader(FromHeader.NAME);
            String deviceId = SipUtil.getUserIdFromFromHeader(fromHeader);
            SipConfig sipConfig = sipConfigService.selectSipConfigBydeviceSipId(deviceId);
            if (sipConfig == null) {
                log.error("processMsg sipConfig is null");
                return ;
            }
            int statusCode = response.getStatusCode();
            // 下发ack
            if (statusCode == Response.OK) {
                ResponseEventExt event = (ResponseEventExt)evt;
                String contentString = new String(response.getRawContent());
                GbSdp gb28181Sdp = SipUtil.parseSDP(contentString);
                SessionDescription sdp = gb28181Sdp.getBaseSdb();
                SipURI requestUri = SipFactory.getInstance().createAddressFactory().createSipURI(sdp.getOrigin().getUsername(), event.getRemoteIpAddress() + ":" + event.getRemotePort());
                Request reqAck = headerBuilder.createAckRequest(sipConfig, response.getLocalAddress().getHostAddress(), requestUri, response);
                log.info("[回复ack] {}-> {}:{} ", sdp.getOrigin().getUsername(), event.getRemoteIpAddress(), event.getRemotePort());
                sipSender.transmitRequest( response.getLocalAddress().getHostAddress(), reqAck);
            }
        } catch (InvalidArgumentException | ParseException | SipException | SdpParseException e) {
            log.info("[点播回复ACK]，异常：", e );
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String method = "INVITE";
        sipListener.addResponseProcessor(method,this);
    }
}
