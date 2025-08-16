package com.fastbee.sip.util;

import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.http.ForestRequest;
import com.fastbee.sip.client.ZlmRecordForestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RecordApiUtils {
    @Resource
    ZlmRecordForestClient zlmRecordForestClient;

    public interface RequestCallback {
        void run(JSONObject response);
    }

    public JSONObject execute(String recoreUrl, ForestRequest<?> request, Map<String, Object> param, RequestCallback callback) {
        request.url(recoreUrl+request.getPath());
        if (param != null) {
            request.addQuery(param);
        }
        return request.execute(JSONObject.class);
    }

    public JSONObject fileDuration(String recoreUrl, String app, String stream, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stream", stream);
        param.put("recordIng", true);
        ForestRequest<?> request = zlmRecordForestClient.fileDuration();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject getInfo(String recoreUrl, RequestCallback callback) {
        ForestRequest<?> request = zlmRecordForestClient.getInfo();
        return this.execute(recoreUrl, request, null, callback);
    }

    public JSONObject getRecordlist(String recoreUrl, Integer pageNum, Integer pageSize, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNum", pageNum);
        param.put("pageSize", pageSize);
        ForestRequest<?> request = zlmRecordForestClient.getRecordlist();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject getRecordDatelist(String recoreUrl, Integer year, Integer month, String app, String stream, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        if(year != null) {
            param.put("year", year);
        }
        if(year != null) {
            param.put("month", month);
        }
        param.put("app", app);
        param.put("stream", stream);
        ForestRequest<?> request = zlmRecordForestClient.getRecordDatelist();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject getRecordStreamlist(String recoreUrl, Integer pageNum, Integer pageSize, String app, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageNum);
        param.put("count", pageSize);
        param.put("app", app);
        ForestRequest<?> request = zlmRecordForestClient.getRecordStreamlist();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject getRecordApplist(String recoreUrl, Integer pageNum, Integer pageSize, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageNum);
        param.put("count", pageSize);
        ForestRequest<?> request = zlmRecordForestClient.getRecordApplist();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject getRecordFilelist(String recoreUrl, Integer pageNum, Integer pageSize, String app, String stream, String startTime, String endTime, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageNum);
        param.put("count", pageSize);
        param.put("app", app);
        param.put("stream", stream);
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        ForestRequest<?> request = zlmRecordForestClient.getRecordFilelist();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject addStreamCallInfo(String recoreUrl, String app, String stream, String callId, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("app", app);
        param.put("stream", stream);
        param.put("callId", callId);
        ForestRequest<?> request = zlmRecordForestClient.addStreamCallInfo();
        return this.execute(recoreUrl, request, param, callback);
    }

    public JSONObject uploadOss(String recoreUrl, String file, RequestCallback callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("resourcePath", file);
        ForestRequest<?> request = zlmRecordForestClient.uploadOss();
        return this.execute(recoreUrl, request, param, callback);
    }
}
