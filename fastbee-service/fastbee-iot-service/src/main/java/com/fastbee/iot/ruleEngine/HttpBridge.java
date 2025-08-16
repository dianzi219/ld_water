package com.fastbee.iot.ruleEngine;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.http.ForestRequest;
import com.fastbee.http.service.HttpClientFactory;
import com.fastbee.iot.domain.Bridge;
import com.fastbee.iot.domain.HttpClient;
import com.fastbee.iot.service.IBridgeService;
import com.fastbee.iot.service.IHttpClientService;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Map;

@LiteflowComponent("httpBridge")
public class HttpBridge extends NodeComponent {

    @Autowired
    private IHttpClientService httpClientService;
    @Autowired
    private IBridgeService bridgeService;

    @Override
    public void process() throws Exception {
        // 获取上下文对象
        MsgContext cxt = this.getContextBean(MsgContext.class);
        Integer id = cxt.getData("httpBridgeID");
        Bridge bridge = bridgeService.queryByIdWithCache(Long.valueOf(id));
        if (bridge != null) {
            HttpClient config = JSON.parseObject(bridge.getConfigJson(), HttpClient.class);
            if (config.getRequestConfig() == null || config.getRequestConfig() == "") {
                ForestRequest request = HttpClientFactory.instance(httpClientService.buildhttpclientconfig(config));
                if (config.getRequestBody() != "") {
                    request.addBody(cxt.placeholders(config.getRequestBody()));
                } else {
                    request.addBody(cxt.getPayload());
                }
                request.execute();
            } else { //海康平台获取直播流接口
                String hostUrl = config.getHostUrl();
                URI uri = new URI(hostUrl);
                String host = uri.getHost();
                int port = uri.getPort();
                String urlPath = uri.getPath();
                // 构建请求path
                Map<String, String> path = MapUtil.builder("https://",urlPath).build();
                ArtemisConfig artemisConfig = new ArtemisConfig();
                JSONObject configJson = JSON.parseObject(config.getRequestConfig());
                String appKey = (String) configJson.get("appKey");
                String appSecret = (String) configJson.get("appSecret");
                artemisConfig.setHost(host + ":" + port);
                artemisConfig.setAppKey(appKey);
                artemisConfig.setAppSecret(appSecret);
                JSONObject querysJson = JSON.parseObject(config.getRequestQuerys());
                ArtemisHttpUtil.doPostStringArtemis(path, querysJson.toString(), null, null, "application/json");
            }
        }
    }
}
