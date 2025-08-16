package com.fastbee.common.core.thingsModel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
@Data
@Builder
public class ThingsModelRuleItem {
    /** 物模型唯一标识符 */
    private String id;

    /** 物模型值 */
    private String value;
    private String operator;
    private String triggerValue;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts;

    /** 备注 **/
    private String remark;

    private String timestamp;
}
