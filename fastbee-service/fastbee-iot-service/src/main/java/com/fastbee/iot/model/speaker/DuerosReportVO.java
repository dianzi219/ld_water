package com.fastbee.iot.model.speaker;

import lombok.Builder;
import lombok.Data;

/**
 * @author fastb
 * @date 2023-12-05 16:05
 */
@Builder
@Data
public class DuerosReportVO {


    private DiscoveryHeader header;
    private DiscoveryPayload payload;


    @Data
    public static class DiscoveryHeader {
        /**
         * 命名空间
         */
        String namespace;
        /**
         * 指令名
         */
        String name;
        /**
         * 消息ID
         */
        String messageId;
        /**
         * Payload版本号
         */
        String payloadVersion;
    }

    @Data
    public static class DiscoveryPayload {
        private String botId;

        private String openUid;

        private DiscoveredAppliance appliance;


        @Data
        public static class DiscoveredAppliance {
            /**
             * 设备标识符
             */
            String applianceId;
            /**
             * 设备的属性名称
             */
            String attributeName;
        }
    }

}
