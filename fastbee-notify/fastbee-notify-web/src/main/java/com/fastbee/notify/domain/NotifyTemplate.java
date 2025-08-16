package com.fastbee.notify.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 通知模版对象 notify_template
 *
 * @author kerwincui
 * @date 2023-12-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NotifyTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 模版名称 */
    @Excel(name = "模版名称")
    private String name;

    /** 通知渠道 */
    @Excel(name = "通知渠道")
    private Long channelId;

    /** 业务编码 */
    @Excel(name = "业务编码")
    private String serviceCode;

    @ApiModelProperty("模版配置参数")
    private String msgParams;

    /** 发送账号 */
    @Excel(name = "是否启用，0-否 1-是")
    private Integer status;

    /** 逻辑删除标识 */
    private Integer delFlag;

    private String channelName;

    /** 发送渠道类型 */
    @Excel(name = "发送渠道类型")
    private String channelType;

    /** 服务商 */
    @Excel(name = "服务商")
    private String provider;

    /** 租户id */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;


}
