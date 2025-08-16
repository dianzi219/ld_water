package com.fastbee.sip.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BroadcastItem {
    private String deviceId;
    private String channelId;
    private String localIp;
    private String ipcIp;
    private Integer localPort;
    private Integer ipcAudioPort;
    // 0是udp 1是tcp
    private Integer udpOrTcp;
    private String callId;
    private String ssrc;
    private String fromTag;
    private String toTag;
    private String viaBranch;
    private String app;
    private String stream;
}
