package com.fastbee.oauth.vo;

import com.fastbee.common.core.text.KeyValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenAuthorizeInfoRespVO {

    /**
     * 客户端
     */
    private Client client;

    private List<KeyValue<String, Boolean>> scopes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Client {

        private String name;

        private String logo;

    }

}
