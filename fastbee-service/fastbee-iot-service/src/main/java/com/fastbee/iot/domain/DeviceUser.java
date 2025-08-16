package com.fastbee.iot.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import org.springframework.data.annotation.Transient;

/**
 * 设备用户对象 iot_device_user
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "DeviceUser", description = "设备用户对象 iot_device_user")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 固件ID */
    @ApiModelProperty("设备ID")
    private Long deviceId;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 手机号码 */
    @ApiModelProperty("手机号码")
    private String phonenumber;

    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty("删除标志（0代表存在 2代表删除）")
    private String delFlag;

    @Transient
    private String userName;

}
