package com.fastbee.sip.handler.req;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.sip.domain.MediaServer;
import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.handler.IReqHandler;
import com.fastbee.sip.model.BroadcastItem;
import com.fastbee.sip.model.GbSdp;
import com.fastbee.sip.server.IGBListener;
import com.fastbee.sip.service.IMediaServerService;
import com.fastbee.sip.service.ISipDeviceService;
import com.fastbee.sip.util.SipUtil;
import com.fastbee.sip.util.ZlmRtpUtils;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sdp.*;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Slf4j
@Component
public class InviteReqHandler extends ReqAbstractHandler implements InitializingBean, IReqHandler {
    @Autowired
    private IGBListener sipListener;

    @Autowired
    private ISipDeviceService sipDeviceService;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private ZlmRtpUtils zlmRtpUtils;

    @Override
    public void processMsg(RequestEvent evt) {
        log.info("接收到INVITE信息");
        SIPRequest request = (SIPRequest) evt.getRequest();
        String deviceId = SipUtil.getUserIdFromFromHeader(request);
        if (deviceId == null) {
            log.info("无法从FromHeader的Address中获取到设备id，返回400");
            // 参数不全， 发400，请求错误
            try {
                responseAck(evt, Response.BAD_REQUEST, null);
            } catch (SipException | InvalidArgumentException | ParseException e) {
                log.error("[命令发送失败] invite BAD_REQUEST: {}", e.getMessage());
            }
            return;
        }
        inviteFromDeviceHandle(request, deviceId);
    }

    public void inviteFromDeviceHandle(SIPRequest request, String deviceId) {
        // 非上级平台请求，查询是否设备请求（通常为接收语音广播的设备）
        SipDevice device = sipDeviceService.selectSipDeviceBySipId(deviceId);
        if (device != null) {
            log.info("收到设备" + deviceId + "的语音广播Invite请求");
            try {
                responseAck(request, Response.TRYING);
            } catch (SipException | InvalidArgumentException | ParseException e) {
                log.error("[命令发送失败] invite BAD_REQUEST: {}", e.getMessage());
            }
            String contentString = new String(request.getRawContent());
            try {
                GbSdp gsdp = SipUtil.parseSDP(contentString);
                SessionDescription sdp = gsdp.getBaseSdb();
                //  获取支持的格式
                Vector mediaDescriptions = sdp.getMediaDescriptions(true);
                // 查看是否支持PS 负载96
                int port = -1;
                for (int i = 0; i < mediaDescriptions.size(); i++) {
                    MediaDescription mediaDescription = (MediaDescription) mediaDescriptions.get(i);
                    Media media = mediaDescription.getMedia();
                    port = media.getMediaPort();
                }
                if (port == -1) {
                    log.info("不支持的媒体格式，返回415");
                    // 回复不支持的格式
                    try {
                        responseAck(request, Response.UNSUPPORTED_MEDIA_TYPE); // 不支持的格式，发415
                    } catch (SipException | InvalidArgumentException | ParseException e) {
                        log.error("[命令发送失败] invite 不支持的媒体格式，返回415， {}", e.getMessage());
                    }
                    return;
                }
                String channelId = sdp.getOrigin().getUsername();
                String addressStr = sdp.getConnection().getAddress();
                log.info("设备{}请求语音流，地址：{}:{}，ssrc：{}", channelId, addressStr, port, gsdp.getSsrc());
                broadcastInviteOk(request, device, channelId, addressStr, port);
            } catch (SdpException e) {
                log.error("[SDP解析异常]", e);
            }
        } else {
            log.warn("来自无效设备/平台的请求");
            try {
                // 不支持的格式，发415
                responseAck(request, Response.BAD_REQUEST);
            } catch (SipException | InvalidArgumentException | ParseException e) {
                log.error("[命令发送失败] invite 来自无效设备/平台的请求， {}", e.getMessage());
            }
        }
    }

    public void broadcastInviteOk(SIPRequest request, SipDevice device, String channelId, String addressStr, int port) {
        try {
            String[] content1 = request.toString().replaceAll("(\r\n|\n)", "<br/>").split("<br/>");
            HashMap<String, String> mapContent = new HashMap<>();
            for (String s : content1) {
                if (s.length() > 2) {
                    if ("=".equals(s.substring(1, 2))) {
                        String content2[] = s.split("=");
                        mapContent.put(content2[0], content2[1]);
                    } else {
                        if (s.startsWith("From")) {
                            String content2[] = s.split(":");
                            mapContent.put("From", content2[2]);
                        } else if (s.startsWith("To")) {
                            String content2[] = s.split(":");
                            mapContent.put("To", content2[2]);
                        }
                    }
                }
            }
            String[] to = mapContent.get("To").split("@");
            String ssrc = mapContent.get("y");
            String m = mapContent.get("m");
            String protocolType = null;
            String payloadNum = null;
            Integer pt = null;
            Integer isUdp = null;
            Integer usePS = null;
            Integer onlyAudio = null;
            StringBuilder resultMStr = new StringBuilder();
            StringBuilder rtpMapStr = new StringBuilder();
            if (m.contains("RTP/AVP")) {
                protocolType = "RTP/AVP";
                payloadNum = m.substring(m.indexOf(protocolType) + protocolType.length() + 1);
                isUdp = 1;
            } else if (m.contains("RTP/AVP/TCP")) {
                protocolType = "RTP/AVP/TCP";
                payloadNum = m.substring(m.indexOf(protocolType) + protocolType.length() + 1);
                isUdp = 0;
            }
            resultMStr.append(" ").append(protocolType).append(" ").append(payloadNum).append("\r\n");
            String[] coderTypeArr = payloadNum.split(" ");
            for (String code : coderTypeArr) {
                if ("8".equals(code)) {
                    pt = 8;
                    usePS = 0;
                    onlyAudio = 1;
                    rtpMapStr.append("a=rtpmap:8 PCMA/8000\r\n");
                    break;
                } else if ("96".equals(code)) {
                    pt = 96;
                    usePS = 1;
                    onlyAudio = 0;
                    rtpMapStr.append("a=rtpmap:96 PS/9000\r\n");
                }
            }
            //回复前先开端口
            BroadcastItem broadcastItem = BroadcastItem.builder()
                    .app("broadcast")
                    .stream(String.format("%s_%s_%s", SipUtil.PREFIX_TALK, device.getDeviceSipId(), channelId))
                    .build();
            MediaServer mediaInfo = mediaServerService.selectMediaServerBydeviceSipId(device.getDeviceSipId());
            if (mediaInfo == null) {
                log.error("broadcastInviteOk mediaInfo is null");
                return;
            }
            Map<String, Object> params = new HashMap<>(16);
            params.put("vhost", "__defaultVhost__");
            params.put("app", broadcastItem.getApp());
            params.put("stream", broadcastItem.getStream());
            params.put("ssrc", ssrc);
            params.put("pt", pt);
            params.put("use_ps", usePS);
            params.put("only_audio", onlyAudio);
            JSONObject startSendRtpResponse;
            if (isUdp == 1) {
                params.put("is_udp", isUdp);
                params.put("dst_url", addressStr);
                params.put("dst_port", port);
                startSendRtpResponse = zlmRtpUtils.startSendRtpStream(mediaInfo, params);
            } else {
                startSendRtpResponse = zlmRtpUtils.startSendRtpPassive(mediaInfo, params);
            }
            Integer localPort = null;
            if (startSendRtpResponse != null && startSendRtpResponse.getInteger("code").equals(0)) {
                localPort = startSendRtpResponse.getInteger("local_port");
                //2.回复设备invite
                StringBuffer contentEnd = new StringBuffer("v=0\r\n")
                        .append("o=").append(to[0]).append(" 0 0 IN IP4 ").append(mediaInfo.getIp()).append("\r\n")
                        .append("s=Play\r\n")
                        .append("c=IN IP4 ").append(mediaInfo.getIp()).append("\r\n")
                        .append("t=0 0\r\n")
                        .append("m=audio ").append(localPort).append(resultMStr)
                        .append(rtpMapStr)
                        .append("a=sendonly\r\n")
                        .append("a=setup:passive\r\n")
                        .append("y=").append(ssrc).append("\r\n");
                if (mapContent.get("f") != null) {
                    contentEnd.append("f=").append(mapContent.get("f").replaceAll("0", "")).append("\r\n");
                }
                responseSdpAck(request, contentEnd.toString());
            }
        } catch (SipException | InvalidArgumentException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String method = "INVITE";
        sipListener.addRequestProcessor(method, this);
    }
}
