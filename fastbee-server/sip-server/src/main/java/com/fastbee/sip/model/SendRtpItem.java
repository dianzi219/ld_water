package com.fastbee.sip.model;

import com.fastbee.sip.enums.InviteType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendRtpItem {
    private String ip;
    private int port;
    private String ssrc;
    private String platformId;
    private String deviceId;
    private String app;
    private String channelId;
    /**
     * 推流状态
     * 0 等待设备推流上来
     * 1 等待上级平台回复ack
     * 2 推流中
     */
    private int status = 0;
    private String streamId;
    private boolean tcp;
    private boolean tcpActive;
    private int localPort;
    private String mediaServerId;
    private String serverId;
    private String CallId;
    private String fromTag;
    private String toTag;
    private int pt = 96;
    private boolean usePs = true;
    private boolean onlyAudio = false;
    private boolean rtcp = false;
    private InviteType playType;
}
