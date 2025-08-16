package com.fastbee.sip.handler.req;

import com.fastbee.sip.model.ResponseAckExtraParam;
import com.fastbee.sip.server.SIPSender;
import com.fastbee.sip.util.SipUtil;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class ReqAbstractHandler {
    @Autowired
    private SIPSender sipSender;

    public ServerTransaction getServerTransaction(RequestEvent evt) {
        Request request = evt.getRequest();
        ServerTransaction serverTransaction = evt.getServerTransaction();
        // 判断TCP还是UDP
        boolean isTcp = false;
        ViaHeader reqViaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
        String transport = reqViaHeader.getTransport();
        if (transport.equals("TCP")) {
            isTcp = true;
        }

        if (serverTransaction == null) {

        }
        return serverTransaction;
    }

    public HeaderFactory getHeaderFactory() {
        try {
            return SipFactory.getInstance().createHeaderFactory();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MessageFactory getMessageFactory() {
        try {
            return SipFactory.getInstance().createMessageFactory();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void responseAck(RequestEvent evt) throws SipException, InvalidArgumentException, ParseException {
        responseAck(evt, Response.OK,null);
    }

    public void responseAck(RequestEvent evt, int statusCode, String msg) throws SipException, InvalidArgumentException, ParseException {
        Response response = getMessageFactory().createResponse(statusCode, evt.getRequest());
        if (msg != null) {
            response.setReasonPhrase(msg);
        }
        SIPRequest sipRequest = (SIPRequest)evt.getRequest();
        sipSender.transmitRequest(sipRequest.getLocalAddress().getHostAddress(), response);
    }

    public SIPResponse responseSdpAck(SIPRequest request, String sdp) throws SipException, InvalidArgumentException, ParseException {
        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION", "SDP");
        // 兼容国标中的使用编码@域名作为RequestURI的情况
        SipURI sipURI = (SipURI)request.getRequestURI();
        if (sipURI.getPort() == -1) {
            log.error("[参数不全] sipURI port为：-1");
        }
        ResponseAckExtraParam responseAckExtraParam = new ResponseAckExtraParam();
        responseAckExtraParam.setContent(sdp);
        responseAckExtraParam.setContentTypeHeader(contentTypeHeader);
        responseAckExtraParam.setSipURI(sipURI);
        return responseAck(request, Response.OK, null, responseAckExtraParam);
    }
    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode) throws SipException, InvalidArgumentException, ParseException {
        return responseAck(sipRequest, statusCode, null);
    }

    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg) throws SipException, InvalidArgumentException, ParseException {
        return responseAck(sipRequest, statusCode, msg, null);
    }

    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg, ResponseAckExtraParam responseAckExtraParam) throws SipException, InvalidArgumentException, ParseException {
        if (sipRequest.getToHeader().getTag() == null) {
            sipRequest.getToHeader().setTag(SipUtil.getNewTag());
        }
        SIPResponse response = (SIPResponse)getMessageFactory().createResponse(statusCode, sipRequest);
        response.setStatusCode(statusCode);
        if (msg != null) {
            response.setReasonPhrase(msg);
        }

        if (responseAckExtraParam != null) {
            if (responseAckExtraParam.getSipURI() != null && sipRequest.getMethod().equals(Request.INVITE)) {
                log.debug("responseSdpAck SipURI: {}:{}", responseAckExtraParam.getSipURI().getHost(), responseAckExtraParam.getSipURI().getPort());
                Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(
                        SipFactory.getInstance().createAddressFactory().createSipURI(responseAckExtraParam.getSipURI().getUser(),  responseAckExtraParam.getSipURI().getHost()+":"+responseAckExtraParam.getSipURI().getPort()
                        ));
                response.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
            }
            if (responseAckExtraParam.getContentTypeHeader() != null) {
                response.setContent(responseAckExtraParam.getContent(), responseAckExtraParam.getContentTypeHeader());
            }

            if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
                if (responseAckExtraParam.getExpires() == -1) {
                    log.error("[参数不全] 2xx的SUBSCRIBE回复，必须设置Expires header");
                }else {
                    ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(responseAckExtraParam.getExpires());
                    response.addHeader(expiresHeader);
                }
            }
        } else {
            if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
                log.error("[参数不全] 2xx的SUBSCRIBE回复，必须设置Expires header");
            }
        }

        // 发送response
        sipSender.transmitRequest(sipRequest.getLocalAddress().getHostAddress(), response);

        return response;
    }



    public Element getRootElement(RequestEvent evt) throws DocumentException {
        return getRootElement(evt, "gb2312");
    }
    public Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
        if (charset == null) {
            charset = "gb2312";
        }
        Request request = evt.getRequest();
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        // 对海康出现的未转义字符做处理。
        String[] destStrArray = new String[]{"&lt;","&gt;","&amp;","&apos;","&quot;"};
        char despChar = '&'; // 或许可扩展兼容其他字符
        byte destBye = (byte) despChar;
        List<Byte> result = new ArrayList<>();
        byte[] rawContent = request.getRawContent();
        if (rawContent == null) {
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            log.error("rawContent is null,from:{} length:{}",fromHeader.toString(),request.getContentLength());
            return null;
        }
        for (int i = 0; i < rawContent.length; i++) {
            if (rawContent[i] == destBye) {
                boolean resul = false;
                for (String destStr : destStrArray) {
                    if (i + destStr.length() <= rawContent.length) {
                        byte[] bytes = Arrays.copyOfRange(rawContent, i, i + destStr.length());
                        resul = resul || (Arrays.equals(bytes,destStr.getBytes()));
                    }
                }
                if (resul) {
                    result.add(rawContent[i]);
                }
            } else {
                result.add(rawContent[i]);
            }
        }
        Byte[] bytes = new Byte[0];
        byte[] bytesResult = ArrayUtils.toPrimitive(result.toArray(bytes));
        String content = new String(bytesResult);
        //过滤掉非法字符
        String ret = XMLChars(content);
        Document xml = reader.read(new ByteArrayInputStream(bytesResult));
        return xml.getRootElement();

    }
    public String XMLChars(String s) {
        if (s == null || "".equals(s)) {
            return s;
        }
        return s.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
    }

}
