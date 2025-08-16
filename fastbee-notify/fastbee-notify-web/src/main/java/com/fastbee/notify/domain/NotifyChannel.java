package com.fastbee.notify.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 通知渠道对象 notify_channel
 *
 * @author kerwincui
 * @date 2023-12-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NotifyChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 通知名称 */
    @Excel(name = "通知名称")
    private String name;

    /** 发送渠道类型 */
    @Excel(name = "发送渠道类型")
    private String channelType;

    /** 服务商 */
    @Excel(name = "服务商")
    private String provider;

    /** 配置内容 */
    @Excel(name = "配置内容")
    private String configContent;

    /** 租户id */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    /** 逻辑删除标识 */
    private Integer delFlag;

}
