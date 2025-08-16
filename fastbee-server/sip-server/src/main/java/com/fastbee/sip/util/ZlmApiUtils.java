package com.fastbee.sip.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.fastbee.common.utils.file.FileUploadUtils;
import com.fastbee.sip.domain.MediaServer;
import com.fastbee.sip.model.ZlmMediaServer;
import com.fastbee.sip.client.ZlmForestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class ZlmApiUtils {

    @Resource
    ZlmForestClient zlmForestClient;

    public JSONObject execute(MediaServer media, ForestRequest<?> request, Map<String, Object> param) {
        request.host(media.getIp())
                .port(Math.toIntExact(media.getPortHttp()))
                .contentFormUrlEncoded()
                .addBody("secret", media.getSecret());
        if (param != null) {
            request.addBody(param);
        }
        return request.execute(JSONObject.class);
    }

    public ZlmMediaServer getMediaServerConfig(MediaServer media) {
        ZlmMediaServer mediaServer = null;
        ForestRequest<?> request = zlmForestClient.getMediaServerConfig();
        JSONObject res = this.execute(media, request, null);
        if (res != null) {
            JSONArray data = res.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                log.info("流媒体服务配置:{}}", data);
                mediaServer = JSON.parseObject(JSON.toJSONString(data.get(0)), ZlmMediaServer.class);
            }
        } else {
            log.error("获取流媒体服务配置失败！");
            log.info("res:{}", res);
        }
        return mediaServer;
    }

    public JSONObject setServerConfig(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.setServerConfig();
        return this.execute(media, request, param);

    }

    public JSONObject restartServer(MediaServer media) {
        ForestRequest<?> request = zlmForestClient.restartServer();
        return this.execute(media, request, null);
    }

    // stream info
    public JSONObject getMediaList(MediaServer media, String app, String schema, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("vhost", "__defaultVhost__");
        if (stream != null) {
            param.put("stream", stream);
        }
        if (schema != null) {
            param.put("schema", schema);
        }
        ForestRequest<?> request = zlmForestClient.getMediaList();
        return this.execute(media, request, param);
    }

    public JSONObject getMediaList(MediaServer media, String app, String stream) {
        return getMediaList(media, app, stream, null);
    }

    public JSONObject getMediaInfo(MediaServer media, String app, String schema, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("schema", schema);
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.getMediaInfo();
        return this.execute(media, request, param);
    }

    //Record
    public JSONObject startRecord(MediaServer media, String type, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("type", type); //0为hls，1为mp4
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.startRecord();
        return this.execute(media, request, param);
    }

    public JSONObject stopRecord(MediaServer media, String type, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("type", type); //0为hls，1为mp4
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.stopRecord();
        return this.execute(media, request, param);
    }

    public JSONObject isRecording(MediaServer media, String type, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("type", type); //0为hls，1为mp4
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.isRecording();
        return this.execute(media, request, param);
    }

    public JSONObject getMp4RecordFile(MediaServer media, String period, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("period", period); //示例值：2020-05-26
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.getMP4RecordFile();
        return this.execute(media, request, param);
    }

    public JSONObject setRecordSpeed(MediaServer media, String speed, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("speed", speed); //要设置的录像倍速
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.setRecordSpeed();
        return this.execute(media, request, param);
    }

    public JSONObject seekRecordStamp(MediaServer media, String stamp, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stamp", stamp); //要设置的录像播放位置
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        ForestRequest<?> request = zlmForestClient.seekRecordStamp();
        return this.execute(media, request, param);
    }

    //Rtp
    public JSONObject getRtpInfo(MediaServer media, String stream_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("stream_id", stream_id);
        ForestRequest<?> request = zlmForestClient.getRtpInfo();
        return this.execute(media, request, param);
    }

    public JSONObject openRtpServer(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.openRtpServer();
        return this.execute(media, request, param);
    }

    public JSONObject closeRtpServer(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.closeRtpServer();
        return this.execute(media, request, param);
    }

    public JSONObject listRtpServer(MediaServer media) {
        Map<String, Object> param = new HashMap<>();
        ForestRequest<?> request = zlmForestClient.listRtpServer();
        return this.execute(media, request, param);
    }

    public JSONObject startSendRtp(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.startSendRtp();
        return this.execute(media, request, param);
    }

    public JSONObject startSendRtpPassive(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.startSendRtpPassive();
        return this.execute(media, request, param);
    }

    public JSONObject stopSendRtp(MediaServer media, Map<String, Object> param) {
        ForestRequest<?> request = zlmForestClient.stopSendRtp();
        return this.execute(media, request, param);
    }

    public JSONObject startSendRtp(MediaServer media, String app, String stream, String ssrc, String dst_url, String dst_port, String is_udp) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("vhost", "__defaultVhost__");
        param.put("stream", stream);
        param.put("ssrc", ssrc); //rtp推流的ssrc，ssrc不同时，可以推流到多个上级服务器
        param.put("dst_url", dst_url); // 目标ip或域名
        param.put("dst_port", dst_port); //`目标端口
        param.put("is_udp", is_udp); //是否udp方式
        // 可选参数 src_port 使用的本机端口，为0或不传时默认为随机端口
        // 可选参数 pt 发送时，rtp的pt（uint8_t）,不传时默认为96
        // 可选参数 use_ps 发送时，rtp的负载类型。为1时，负载为ps；为0时，为es；不传时默认为1
        // 可选参数 only_audio 当use_ps 为0时，有效。为1时，发送音频；为0时，发送视频；不传时默认为0
        ForestRequest<?> request = zlmForestClient.startSendRtp();
        return this.execute(media, request, param);
    }

    public JSONObject startSendRtpPassive(MediaServer media, String ssrc, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        param.put("ssrc", ssrc); //rtp推流的ssrc，ssrc不同时，可以推流到多个上级服务器
        // 可选参数 src_port 使用的本机端口，为0或不传时默认为随机端口
        // 可选参数 pt 发送时，rtp的pt（uint8_t）,不传时默认为96
        // 可选参数 use_ps 发送时，rtp的负载类型。为1时，负载为ps；为0时，为es；不传时默认为1
        // 可选参数 only_audio 当use_ps 为0时，有效。为1时，发送音频；为0时，发送视频；不传时默认为0
        ForestRequest<?> request = zlmForestClient.startSendRtpPassive();
        return this.execute(media, request, param);
    }

    public JSONObject stopSendRtp(MediaServer media, String ssrc, String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stream", stream);
        param.put("vhost", "__defaultVhost__");
        param.put("ssrc", ssrc); //可选参数 根据ssrc关停某路rtp推流，置空时关闭所有流
        ForestRequest<?> request = zlmForestClient.stopSendRtp();
        return this.execute(media, request, param);
    }

    //获取截图
    public void getSnap(MediaServer media, String streamUrl, int timeout_sec, int expire_sec, String fileName) {
        Map<String, Object> param = new HashMap<>(3);
        param.put("url", streamUrl);
        param.put("timeout_sec", timeout_sec);
        param.put("expire_sec", expire_sec);
        ForestRequest<?> request = zlmForestClient.getSnap();
        request.host(media.getIp())
                .port(Math.toIntExact(media.getPortHttp()))
                .contentFormUrlEncoded()
                .addBody("secret", media.getSecret())
                .addBody(param);
        ForestResponse res = request.execute(ForestResponse.class);
        if (res.isSuccess()) {
            try {
                File snapFolder = new File(FileUploadUtils.getDefaultBaseDir() + File.separator + "snap");
                if (!snapFolder.exists()) {
                    if (!snapFolder.mkdirs()) {
                        log.warn("{}路径创建失败", snapFolder.getAbsolutePath());
                    }
                }
                File snapFile = new File(snapFolder.getAbsolutePath() + File.separator + fileName);
                log.info("截图成功：{}", snapFile.getAbsolutePath());
                FileOutputStream outStream = new FileOutputStream(snapFile);
                // 获取响应流
                byte[] in = res.getByteArray();
                outStream.write(in);
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
