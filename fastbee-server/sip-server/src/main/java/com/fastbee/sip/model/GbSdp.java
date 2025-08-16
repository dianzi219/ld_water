package com.fastbee.sip.model;

import javax.sdp.SessionDescription;

public class GbSdp {
    private SessionDescription baseSdb;
    private String ssrc;

    private String mediaDescription;

    public static GbSdp getInstance(SessionDescription baseSdb, String ssrc, String mediaDescription) {
        GbSdp gb28181Sdp = new GbSdp();
        gb28181Sdp.setBaseSdb(baseSdb);
        gb28181Sdp.setSsrc(ssrc);
        gb28181Sdp.setMediaDescription(mediaDescription);
        return gb28181Sdp;
    }


    public SessionDescription getBaseSdb() {
        return baseSdb;
    }

    public void setBaseSdb(SessionDescription baseSdb) {
        this.baseSdb = baseSdb;
    }

    public String getSsrc() {
        return ssrc;
    }

    public void setSsrc(String ssrc) {
        this.ssrc = ssrc;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
    }
}
