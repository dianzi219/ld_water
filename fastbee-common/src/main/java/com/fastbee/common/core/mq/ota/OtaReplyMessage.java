package com.fastbee.common.core.mq.ota;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * OTA升级回复model
 * @author gsb
 * @date 2022/10/24 17:20
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OtaReplyMessage {

    private Long taskId;
    private BigDecimal version;
    private int status;
    private int progress;
}
