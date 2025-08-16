package com.fastbee.iot.domain;

import com.fastbee.common.enums.DataEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 指令权限控制对象 order_control
 *
 * @author kerwincui
 * @date 2024-07-01
 */
@ApiModel(value = "OrderControl", description = "指令权限控制 order_control")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderControl extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 租户id
     */
    @Excel(name = "租户id")
    @ApiModelProperty("租户id")
    private Long tenantId;

    /**
     * 逗号分隔
     */
    @Excel(name = "逗号分隔")
    @ApiModelProperty("逗号分隔")
    private String selectOrder;

    /**
     * 是否生效 0-否 1-是
     */
    @Excel(name = "是否生效 0-否 1-是")
    @ApiModelProperty("是否生效 0-否 1-是")
    private String status;

    /**
     * 被限制的用户id
     */
    @Excel(name = "被限制的用户id")
    @ApiModelProperty("被限制的用户id")
    private Long userId;

    /**
     * 设备id
     */
    @Excel(name = "设备id")
    @ApiModelProperty("设备id")
    private Long deviceId;

    /**
     * 可操作次数
     */
    @Excel(name = "可操作次数")
    @ApiModelProperty("可操作次数")
    private Integer count;

    @ApiModelProperty("文件")
    private String filePath;

    @ApiModelProperty("图片")
    private String imgUrl;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private String modelNames;

    private String deviceName;

    private String userName;
}

