package com.fastbee.sip.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.sip.domain.MediaServer;
import com.fastbee.sip.model.SendRtpItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ZlmRtpUtils {

    @Autowired
    private ZlmApiUtils zlmApiUtils;


    public int createRTPServer(MediaServer media, String streamId, String ssrc, Boolean onlyAuto, Boolean reUsePort, Integer tcpMode) {
        int result = -1;
        // 查询此rtp server 是否已经存在
        JSONObject rtpInfo = zlmApiUtils.getRtpInfo(media, streamId);
        log.info(JSONObject.toJSONString(rtpInfo));
        if (rtpInfo.getInteger("code") == 0) {
            if (rtpInfo.getBoolean("exist")) {
                result = rtpInfo.getInteger("local_port");
                if (result == 0) {
                    // 此时说明rtpServer已经创建但是流还没有推上来
                    // 此时重新打开rtpServer
                    Map<String, Object> param = new HashMap<>();
                    param.put("stream_id", streamId);
                    JSONObject jsonObject = zlmApiUtils.closeRtpServer(media, param);
                    if (jsonObject != null) {
                        if (jsonObject.getInteger("code") == 0) {
                            return createRTPServer(media, streamId, ssrc, onlyAuto, reUsePort, tcpMode);
                        } else {
                            log.warn("[开启rtpServer], 重启RtpServer错误");
                        }
                    }
                }
                return result;
            }
        } else if (rtpInfo.getInteger("code") == -2) {
            return result;
        }

        if (media != null) {
            Map<String, Object> param = new HashMap<>();
            // 推流端口设置0则使用随机端口
            param.put("port", 0);
            if (tcpMode == null) {
                tcpMode = 0;
            }
            //0 udp 模式，1 tcp 被动模式, 2 tcp 主动模式。 (兼容enable_tcp 为0/1)
            param.put("tcp_mode", tcpMode);
            param.put("stream_id", streamId);
            if (reUsePort != null) {
                param.put("re_use_port", reUsePort ? "1" : "0");
            }
            if (onlyAuto != null) {
                param.put("only_audio", onlyAuto ? "1" : "0");
            }
            if (ssrc != null) {
                param.put("ssrc", ssrc);
            }
            JSONObject jsonObject = zlmApiUtils.openRtpServer(media, param);
            log.warn("param:{},createRTPServer:{}", param, jsonObject);
            if (jsonObject != null) {
                if (jsonObject.getInteger("code") == 0) {
                    result = jsonObject.getInteger("port");
                } else {
                    log.error("创建RTP Server 失败 {}: ", jsonObject.getString("msg"));
                }
            } else {
                log.error("创建RTP Server 失败: 请检查ZLM服务");
            }
        }
        return result;
    }

    public void closeRTPServer(MediaServer media, String streamId) {
        boolean result = false;
        Map<String, Object> param = new HashMap<>();
        param.put("stream_id", streamId);
        JSONObject jsonObject = zlmApiUtils.closeRtpServer(media, param);
        if (jsonObject != null) {
            if (jsonObject.getInteger("code") == 0) {
                result = jsonObject.getInteger("hit") == 1;
            } else {
                log.error("关闭RTP Server 失败: " + jsonObject.getString("msg"));
            }
        } else {
            //  检查ZLM状态
            log.error("关闭RTP Server 失败: 请检查ZLM服务");
        }
    }

    public int getFreePort(MediaServer mediaServerItem, int startPort, int endPort, List<Integer> usedFreelist) {
        if (endPort <= startPort) {
            return -1;
        }
        if (usedFreelist == null) {
            usedFreelist = new ArrayList<>();
        }
        JSONObject listRtpServerJsonResult = zlmApiUtils.listRtpServer(mediaServerItem);
        if (listRtpServerJsonResult != null) {
            JSONArray data = listRtpServerJsonResult.getJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONObject dataItem = data.getJSONObject(i);
                    usedFreelist.add(dataItem.getInteger("port"));
                }
            }
        }

        Map<String, Object> param = new HashMap<>();
        int result = -1;
        // 设置推流端口
        if (startPort % 2 == 1) {
            startPort++;
        }
        boolean checkPort = false;
        for (int i = startPort; i < endPort + 1; i += 2) {
            if (!usedFreelist.contains(i)) {
                checkPort = true;
                startPort = i;
                break;
            }
        }
        if (!checkPort) {
            log.warn("未找到节点{}上范围[{}-{}]的空闲端口", mediaServerItem.getId(), startPort, endPort);
            return -1;
        }
        param.put("port", startPort);
        String stream = UUID.randomUUID().toString();
        param.put("enable_tcp", 1);
        param.put("stream_id", stream);
        JSONObject openRtpServerResultJson = zlmApiUtils.openRtpServer(mediaServerItem, param);

        if (openRtpServerResultJson != null) {
            if (openRtpServerResultJson.getInteger("code") == 0) {
                result = openRtpServerResultJson.getInteger("port");
                Map<String, Object> closeRtpServerParam = new HashMap<>();
                closeRtpServerParam.put("stream_id", stream);
                zlmApiUtils.closeRtpServer(mediaServerItem, closeRtpServerParam);
            } else {
                usedFreelist.add(startPort);
                startPort += 2;
                result = getFreePort(mediaServerItem, startPort, endPort, usedFreelist);
            }
        } else {
            //  检查ZLM状态
            log.error("创建RTP Server 失败 {}: 请检查ZLM服务", param.get("port"));
        }
        return result;
    }

    public int createRTPServer(MediaServer mediaServerItem, String streamId, int ssrc, Integer port) {
        int result = -1;
        // 查询此rtp server 是否已经存在
        JSONObject rtpInfo = zlmApiUtils.getRtpInfo(mediaServerItem, streamId);
        log.info(JSONObject.toJSONString(rtpInfo));
        if (rtpInfo.getInteger("code") == 0) {
            if (rtpInfo.getBoolean("exist")) {
                result = rtpInfo.getInteger("local_port");
                if (result == 0) {
                    // 此时说明rtpServer已经创建但是流还没有推上来
                    // 此时重新打开rtpServer
                    Map<String, Object> param = new HashMap<>();
                    param.put("stream_id", streamId);
                    JSONObject jsonObject = zlmApiUtils.closeRtpServer(mediaServerItem, param);
                    if (jsonObject != null) {
                        if (jsonObject.getInteger("code") == 0) {
                            return createRTPServer(mediaServerItem, streamId, ssrc, port);
                        } else {
                            log.warn("[开启rtpServer], 重启RtpServer错误");
                        }
                    }
                }
                return result;
            }
        } else if (rtpInfo.getInteger("code") == -2) {
            return result;
        }

        Map<String, Object> param = new HashMap<>();

        param.put("enable_tcp", 1);
        param.put("stream_id", streamId);
        // 推流端口设置0则使用随机端口
        if (port == null) {
            param.put("port", 0);
        } else {
            param.put("port", port);
        }
        param.put("ssrc", ssrc);
        JSONObject openRtpServerResultJson = zlmApiUtils.openRtpServer(mediaServerItem, param);
        log.info(JSONObject.toJSONString(openRtpServerResultJson));
        if (openRtpServerResultJson != null) {
            if (openRtpServerResultJson.getInteger("code") == 0) {
                result = openRtpServerResultJson.getInteger("port");
            } else {
                log.error("创建RTP Server 失败 {}: ", openRtpServerResultJson.getString("msg"));
            }
        } else {
            //  检查ZLM状态
            log.error("创建RTP Server 失败 {}: 请检查ZLM服务", param.get("port"));
        }
        return result;
    }

    public boolean closeRtpServer(MediaServer serverItem, String streamId) {
        boolean result = false;
        if (serverItem != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("stream_id", streamId);
            JSONObject jsonObject = zlmApiUtils.closeRtpServer(serverItem, param);
            if (jsonObject != null) {
                if (jsonObject.getInteger("code") == 0) {
                    result = jsonObject.getInteger("hit") == 1;
                } else {
                    log.error("关闭RTP Server 失败: " + jsonObject.getString("msg"));
                }
            } else {
                //  检查ZLM状态
                log.error("关闭RTP Server 失败: 请检查ZLM服务");
            }
        }
        return result;
    }


    /**
     * 创建一个国标推流
     *
     * @param ip         推流ip
     * @param port       推流端口
     * @param ssrc       推流唯一标识
     * @param platformId 平台id
     * @param channelId  通道id
     * @param tcp        是否为tcp
     * @return SendRtpItem
     */
    public SendRtpItem createSendRtpItem(MediaServer serverItem, String ip, int port, String ssrc, String platformId, String deviceId, String channelId, boolean tcp, boolean rtcp) {
        // 默认为随机端口
        int localPort = 0;
        return SendRtpItem.builder()
                .ip(ip)
                .port(port)
                .channelId(channelId)
                .platformId(platformId)
                .deviceId(deviceId)
                .localPort(localPort)
                .serverId("fastbee")
                .mediaServerId(serverItem.getServerId())
                .ssrc(ssrc)
                .tcp(tcp)
                .rtcp(rtcp)
                .app("rtp")
                .build();
    }

    /**
     * 创建一个直播推流
     *
     * @param ip         推流ip
     * @param port       推流端口
     * @param ssrc       推流唯一标识
     * @param platformId 平台id
     * @param channelId  通道id
     * @param tcp        是否为tcp
     * @return SendRtpItem
     */
    public SendRtpItem createSendRtpItem(MediaServer serverItem, String ip, int port, String ssrc, String platformId, String app, String stream, String channelId, boolean tcp, boolean rtcp) {
        // 默认为随机端口
        int localPort = 0;
        return SendRtpItem.builder()
                .app(app)
                .ip(ip)
                .port(port)
                .channelId(channelId)
                .platformId(platformId)
                .streamId(stream)
                .localPort(localPort)
                .serverId("fastbee")
                .mediaServerId(serverItem.getServerId())
                .ssrc(ssrc)
                .tcp(tcp)
                .rtcp(rtcp)
                .build();
    }

    /**
     * 保持端口，直到需要需要发流时再释放
     */
    public int keepPort(MediaServer serverItem, String ssrc) {
        int localPort = 0;
        Map<String, Object> param = new HashMap<>(3);
        param.put("port", 0);
        param.put("enable_tcp", 1);
        param.put("stream_id", ssrc);
        JSONObject jsonObject = zlmApiUtils.openRtpServer(serverItem, param);
        if (jsonObject.getInteger("code") == 0) {
            localPort = jsonObject.getInteger("port");
        }
        log.info("[上级点播] {}->监听端口: {}", ssrc, localPort);
        return localPort;
    }

    /**
     * 释放保持的端口
     */
    public boolean releasePort(MediaServer serverItem, String ssrc) {
        log.info("[上级点播] {}->释放监听端口", ssrc);
        return closeRtpServer(serverItem, ssrc);
    }

    /**
     * 调用zlm RESTFUL API —— startSendRtp
     */
    public JSONObject startSendRtpStream(MediaServer mediaServerItem, Map<String, Object> param) {
        return zlmApiUtils.startSendRtp(mediaServerItem, param);
    }

    /**
     * 调用zlm RESTFUL API —— startSendRtpPassive
     */
    public JSONObject startSendRtpPassive(MediaServer mediaServerItem, Map<String, Object>param) {
        return zlmApiUtils.startSendRtpPassive(mediaServerItem, param);
    }

    /**
     * 查询待转推的流是否就绪
     */
    public Boolean isRtpReady(MediaServer mediaServerItem, String streamId) {
        JSONObject mediaInfo = zlmApiUtils.getMediaInfo(mediaServerItem, "rtp", "rtsp", streamId);
        return (mediaInfo.getInteger("code") == 0 && mediaInfo.getBoolean("online"));
    }

    /**
     * 查询待转推的流是否就绪
     */
    public Boolean isStreamReady(MediaServer mediaServerItem, String app, String streamId) {
        JSONObject mediaInfo = zlmApiUtils.getMediaList(mediaServerItem, app, streamId);
        return mediaInfo != null && (mediaInfo.getInteger("code") == 0
                && mediaInfo.getJSONArray("data") != null
                && !mediaInfo.getJSONArray("data").isEmpty());
    }

    /**
     * 查询转推的流是否有其它观看者
     *
     * @param streamId
     * @return
     */
    public int totalReaderCount(MediaServer mediaServerItem, String app, String streamId) {
        JSONObject mediaInfo = zlmApiUtils.getMediaInfo(mediaServerItem, app, "rtsp", streamId);
        if (mediaInfo == null) {
            return 0;
        }
        Integer code = mediaInfo.getInteger("code");
        if (code < 0) {
            log.warn("查询流({}/{})是否有其它观看者时得到： {}", app, streamId, mediaInfo.getString("msg"));
            return -1;
        }
        if (code == 0 && mediaInfo.getBoolean("online") != null && !mediaInfo.getBoolean("online")) {
            log.warn("查询流({}/{})是否有其它观看者时得到： {}", app, streamId, mediaInfo.getString("msg"));
            return -1;
        }
        return mediaInfo.getInteger("totalReaderCount");
    }

    /**
     * 调用zlm RESTful API —— stopSendRtp
     */
    public Boolean stopSendRtpStream(MediaServer mediaServerItem, Map<String, Object> param) {
        boolean result = false;
        JSONObject jsonObject = zlmApiUtils.stopSendRtp(mediaServerItem, param);
        if (jsonObject == null) {
            log.error("[停止RTP推流] 失败: 请检查ZLM服务");
        } else if (jsonObject.getInteger("code") == 0) {
            result = true;
            log.info("[停止RTP推流] 成功");
        } else {
            log.error("[停止RTP推流] 失败: {}, 参数：{}->\r\n{}", jsonObject.getString("msg"), JSON.toJSON(param), jsonObject);
        }
        return result;
    }
}
