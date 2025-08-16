package com.fastbee.sip.service;

import com.fastbee.sip.model.Stream;

public interface ITalkService {
    Stream getBroadcastUrl(String deviceId, String channelId);
    String broadcast(String deviceId, String channelId);
    String broadcastStop(String deviceId, String channelId);
}
