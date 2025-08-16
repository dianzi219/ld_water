package com.fastbee.common.core.mq;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.enums.ThingsModelType;
import com.fastbee.common.utils.DateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * @author gsb
 * @date 2022/12/5 11:26
 */
@Data
public class InvokeReqDto {

    @ApiModelProperty(value = "设备编号")
    private String serialNumber;

    @NotNull(message = "标识符不能为空")
    @ApiModelProperty(value = "标识符")
    private String identifier;
    /**消息体*/
    @ApiModelProperty(value = "消息体")
    private JSONObject params;
    /**远程消息体*/
    @ApiModelProperty(value = "远程调用消息体")
    private Map<String,Object> remoteCommand;
    /**设备超时时间*/
    @ApiModelProperty(value = "设备超时响应时间，默认10s")
    private Integer timeOut = 10;

    @ApiModelProperty(value = "下发物模型类型")
    private Integer type = ThingsModelType.SERVICE.getCode();

    @ApiModelProperty(value = "是否是影子模式")
    private String isShadow ;

    @ApiModelProperty(value = "产品id")
    private Long productId;

    /**
     * 物模型名称
     */
    private String modelName;

    private Date timestamp = DateUtils.getNowDate();

    /**
     * 场景id
     */
    private Long sceneModelId;

    /**
     * 场景变量类型
     */
    private Integer variableType;

    private Long userId;
}
