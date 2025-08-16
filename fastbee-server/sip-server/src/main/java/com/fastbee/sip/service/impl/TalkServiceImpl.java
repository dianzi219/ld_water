package com.fastbee.sip.service.impl;

import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.model.Stream;
import com.fastbee.sip.server.ISipCmd;
import com.fastbee.sip.service.ISipDeviceService;
import com.fastbee.sip.service.ITalkService;
import com.fastbee.sip.service.IZmlHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TalkServiceImpl implements ITalkService {
    @Autowired
    private ISipCmd sipCmd;

    @Autowired
    private ISipDeviceService sipDeviceService;

    @Autowired
    private IZmlHookService zmlHookService;

    @Override
    public Stream getBroadcastUrl(String deviceId, String channelId) {
        return zmlHookService.buildPushRtc("broadcast", deviceId, channelId);
    }

    @Override
    public String broadcast(String deviceId, String channelId) {
        SipDevice dev = sipDeviceService.selectSipDeviceBySipId(deviceId);
        if (dev == null) {
            log.error("broadcast dev is null,deviceId:{},channelId:{}", deviceId, channelId);
            return null;
        }
        sipCmd.audioBroadcastCmd(dev, channelId);
        return "";
    }

    @Override
    public String broadcastStop(String deviceId, String channelId) {
        return "";
    }
}
