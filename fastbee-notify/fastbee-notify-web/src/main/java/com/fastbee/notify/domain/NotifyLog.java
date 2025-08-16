package com.fastbee.notify.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 通知日志对象 notify_log
 * 
 * @author fastbee
 * @date 2023-12-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NotifyLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编号 */
    private Long id;

    /** 通知模版编号 */
    @Excel(name = "通知模版编号")
    private Long notifyTemplateId;

    /** 渠道编号 */
    @Excel(name = "渠道编号")
    private Long channelId;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String msgContent;

    /** 发送账号 */
    @Excel(name = "发送账号")
    private String sendAccount;

    /** 发送状态 */
    @Excel(name = "发送状态")
    private Integer sendStatus;

    /** 返回内容 */
    @Excel(name = "返回内容")
    private String resultContent;

    /** 逻辑删除标识 */
    private Integer delFlag;

    /** 渠道名称 */
    private String channelName;

    /** 模板名称 */
    private String templateName;

    /** 租户id */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;
    /** 业务编码 */
    @Excel(name = "业务编码")
    private String serviceCode;

}
