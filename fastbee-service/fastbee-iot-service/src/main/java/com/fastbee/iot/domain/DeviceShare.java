package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import org.springframework.data.annotation.Transient;

/**
 * 设备分享对象 iot_device_share
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DeviceShare extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备id */
    @ApiModelProperty("设备ID")
    private Long deviceId;

    /** 用户id */
    private Long userId;

    /** 手机 */
    @Excel(name = "手机")
    private String phonenumber;

    /** 用户物模型权限，多个以英文逗号分隔 */
    @Excel(name = "用户物模型权限，多个以英文逗号分隔")
    private String perms;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    @Transient
    @ApiModelProperty("标识是否是拥有者")
    private Integer isOwner;

    @Transient
    private String userName;

}
