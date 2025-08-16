package com.fastbee.sip.server;

import com.fastbee.sip.domain.SipConfig;
import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.model.InviteInfo;
import com.fastbee.sip.model.VideoSessionInfo;
import com.fastbee.sip.service.IInviteService;
import com.fastbee.sip.service.ISipCacheService;
import com.fastbee.sip.util.SipUtil;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;

@Component
public class ReqMsgHeaderBuilder {

    @Autowired
    private SipLayer sipLayer;

    @Autowired
    private VideoSessionManager streamSession;

    @Autowired
    private ISipCacheService sipCacheService;

    @Autowired
    private IInviteService inviteService;

    /**
     * 创建请求消息
     *
     * @param device
     * @param content
     * @param fromTag
     * @return
     * @throws ParseException
     * @throws InvalidArgumentException
     * @throws PeerUnavailableException
     */
    public Request createMessageRequest(SipDevice device, SipConfig sipConfig, String content, String fromTag, CallIdHeader callIdHeader)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        // sipuri
        SipURI requestURI = SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceSipId(),
                device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(sipConfig.getIp(),
                sipConfig.getPort(), device.getTransport(), SipUtil.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(),
                sipConfig.getIp() + ":" + sipConfig.getPort());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, fromTag);
        // to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceSipId(),
                sipConfig.getDomain());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, SipUtil.getNewTag());

        // Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
        // ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.MESSAGE);

        request = SipFactory.getInstance().createMessageFactory().createRequest(requestURI, Request.MESSAGE, callIdHeader, cSeqHeader,
                fromHeader, toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION",
                "MANSCDP+xml");
        request.setContent(content, contentTypeHeader);

        return request;
    }

    public Request createInviteRequest(SipDevice device, SipConfig sipConfig, String channelId, String content, String ssrc, String fromTag, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        // 请求行
        SipURI requestLine = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(device.getIp(), device.getPort(),
                device.getTransport(), SipUtil.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(),
                sipConfig.getDomain());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, fromTag); // 必须要有标记，否则无法创建会话，无法回应ack
        // to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, sipConfig.getDomain());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, null);

        // Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);

        // ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.INVITE);
        request = SipFactory.getInstance().createMessageFactory().createRequest(requestLine, Request.INVITE, callIdHeader, cSeqHeader,
                fromHeader, toHeader, viaHeaders, maxForwards);

        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory()
                .createSipURI(sipConfig.getServerSipid(), sipConfig.getIp() + ":" + sipConfig.getPort()));

        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
        // Subject
        SubjectHeader subjectHeader = SipFactory.getInstance().createHeaderFactory()
                .createSubjectHeader(String.format("%s:%s,%s:%s", channelId, ssrc, sipConfig.getIp(), 0));
        request.addHeader(subjectHeader);
        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION",
                "SDP");
        request.setContent(content, contentTypeHeader);
        return request;
    }

    public Request createPlaybackInviteRequest(SipDevice device, SipConfig sipConfig, String channelId, String content, String viaTag, String fromTag, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        // 请求行
        SipURI requestLine = SipFactory.getInstance().createAddressFactory().createSipURI(channelId,
                device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(device.getIp(), device.getPort(),
                device.getTransport(), viaTag);
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(),
                sipConfig.getDomain());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, fromTag); // 必须要有标记，否则无法创建会话，无法回应ack
        // to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, null);

        // Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);

        // ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.INVITE);
        request = SipFactory.getInstance().createMessageFactory().createRequest(requestLine, Request.INVITE, callIdHeader, cSeqHeader,
                fromHeader, toHeader, viaHeaders, maxForwards);

        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory()
                .createSipURI(sipConfig.getServerSipid(), sipConfig.getIp() + ":" + sipConfig.getPort()));

        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));

        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION",
                "SDP");
        request.setContent(content, contentTypeHeader);
        return request;
    }

    public Request createByeRequest(SipDevice device, SipConfig sipConfig, String channelId, InviteInfo invite) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        //请求行
        SipURI requestLine = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(device.getIp(), device.getPort(),
                device.getTransport(), SipUtil.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        //from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(), sipConfig.getDomain());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, invite.getFromTag());
        //to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, SipUtil.getNewTag());
        //Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
        //ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.BYE);
        CallIdHeader callIdHeader = SipFactory.getInstance().createHeaderFactory().createCallIdHeader(invite.getCallId());
        request = SipFactory.getInstance().createMessageFactory().createRequest(requestLine, Request.BYE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory()
                .createSipURI(sipConfig.getServerSipid(), sipConfig.getIp() + ":" + sipConfig.getPort()));
        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
        return request;
    }

    public Request createRtspRequest(SipDevice device, SipConfig sipConfig, String channelId, String streamId, String content)
            throws PeerUnavailableException, ParseException, InvalidArgumentException {
        Request request = null;
        VideoSessionInfo info = streamSession.getSessionInfo(device.getDeviceSipId(), channelId, streamId, null);
        if (info == null) {
            return null;
        }
        Dialog dialog = streamSession.getclientTransaction(info).getDialog();
        if (dialog == null) {
            return null;
        }
        InviteInfo invite = inviteService.getInviteInfoBySSRC(info.getSsrc());
        if (invite == null) {
            return null;
        }
        // 请求行
        SipURI requestLine = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(sipLayer.getLocalIp(sipConfig.getIp()), sipConfig.getPort(),
                device.getTransport(), invite.getViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(),
                sipConfig.getDomain());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, invite.getFromTag());
        // to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(channelId, device.getHostaddress());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, dialog.getRemoteTag());
        // callid
        CallIdHeader callIdHeader = SipFactory.getInstance().createHeaderFactory().createCallIdHeader(invite.getCallId());;
        // Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
        // ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.INFO);

        request = SipFactory.getInstance().createMessageFactory().createRequest(requestLine, Request.INFO, callIdHeader, cSeqHeader,
                fromHeader, toHeader, viaHeaders, maxForwards);

        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory()
                .createSipURI(sipConfig.getServerSipid(), sipConfig.getIp() + ":" + sipConfig.getPort()));
        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));

        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("Application",
                "MANSRTSP");
        request.setContent(content, contentTypeHeader);
        return request;
    }

    public Request createSubscribeRequest(SipDevice device, SipConfig sipConfig, String content, SIPRequest requestOld, Integer expires, String event, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        // sipuri
        SipURI requestURI = SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceSipId(), device.getHostaddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(sipLayer.getLocalIp(sipConfig.getIp()), sipConfig.getPort(),
                device.getTransport(), SipUtil.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(), sipConfig.getDomain());
        Address fromAddress = SipFactory.getInstance().createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = SipFactory.getInstance().createHeaderFactory().createFromHeader(fromAddress, requestOld == null ? SipUtil.getNewFromTag() :requestOld.getFromTag());
        // to
        SipURI toSipURI = SipFactory.getInstance().createAddressFactory().createSipURI(device.getDeviceSipId(), device.getHostaddress());
        Address toAddress = SipFactory.getInstance().createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = SipFactory.getInstance().createHeaderFactory().createToHeader(toAddress, requestOld == null ? null :requestOld.getToTag());

        // Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);

        // ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipCacheService.getCSEQ(sipConfig.getServerSipid()), Request.SUBSCRIBE);

        request = SipFactory.getInstance().createMessageFactory().createRequest(requestURI, Request.SUBSCRIBE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);


        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(), sipLayer.getLocalIp(sipConfig.getIp())+":"+sipConfig.getPort()));
        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));

        // Expires
        ExpiresHeader expireHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(expires);
        request.addHeader(expireHeader);

        // Event
        EventHeader eventHeader = SipFactory.getInstance().createHeaderFactory().createEventHeader(event);

        int random = (int) Math.floor(Math.random() * 10000);
        eventHeader.setEventId(random + "");
        request.addHeader(eventHeader);

        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(content, contentTypeHeader);

        return request;
    }

    public Request createAckRequest(SipConfig sipConfig, String localIp, SipURI sipURI, SIPResponse sipResponse) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = SipFactory.getInstance().createHeaderFactory().createViaHeader(localIp, sipConfig.getPort(), sipResponse.getTopmostViaHeader().getTransport(), SipUtil.getNewViaTag());
        viaHeaders.add(viaHeader);
        //Forwards
        MaxForwardsHeader maxForwards = SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70);
        //ceq
        CSeqHeader cSeqHeader = SipFactory.getInstance().createHeaderFactory().createCSeqHeader(sipResponse.getCSeqHeader().getSeqNumber(), Request.ACK);
        Request request = SipFactory.getInstance().createMessageFactory().createRequest(sipURI, Request.ACK, sipResponse.getCallIdHeader(), cSeqHeader, sipResponse.getFromHeader(), sipResponse.getToHeader(), viaHeaders, maxForwards);
        Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(SipFactory.getInstance().createAddressFactory().createSipURI(sipConfig.getServerSipid(), localIp + ":"+sipConfig.getPort()));
        request.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
        return request;
    }

}
