package com.fastbee.notify.vo;

import lombok.Data;

import java.util.List;

/**
 * 渠道服务商VO类
 * @author fastb
 * @date 2023-12-01 14:06
 */
@Data
public class ChannelProviderVO {

    /**
     * 渠道类型
     */
    private String channelType;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 服务商集合
     */
    private List<Provider> providerList;

    /**
     * 服务商
     */
    @Data
    public static class Provider{

        /**
         * 服务商英文标识
         */
        private String provider;

        /**
         * 服务商名称
         */
        private String providerName;

        /**
         * 所属渠道标识
         */
        private String category;

    }
}


