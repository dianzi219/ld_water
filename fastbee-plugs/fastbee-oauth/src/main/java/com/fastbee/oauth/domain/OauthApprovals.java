package com.fastbee.oauth.domain;

import com.fastbee.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 【请填写功能名称】对象 oauth_approvals
 *
 * @author kerwincui
 * @date 2024-03-20
 */
@Data
public class OauthApprovals
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String userid;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String clientid;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String scope;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String status;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private LocalDateTime expiresat;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private LocalDateTime lastmodifiedat;
}
