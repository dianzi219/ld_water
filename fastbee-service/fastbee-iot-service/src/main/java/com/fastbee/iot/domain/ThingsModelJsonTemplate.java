package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用物模型对象 iot_things_model_template
 *
 * @author kerwincui
 * @date 2023-01-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThingsModelJsonTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 物模型ID */
    private Long templateId;

    /** 物模型名称 */
    @Excel(name = "物模型名称",prompt = "必填")
    private String templateName;

    /** 标识符，产品下唯一 */
    @Excel(name = "标识符",prompt = "modbus不填,默认为寄存器地址")
    private String identifier;

    /** 模型类别（1-属性，2-功能，3-事件） */
    @Excel(name = "模型类别", readConverterExp = "1=属性,2=功能,3=事件",prompt ="1=属性,2=功能,3=事件")
    private Integer type;

    /** 数据类型（integer、decimal、string、bool、array、enum） */
    @Excel(name = "数据类型", prompt = "integer、decimal、string、bool、array、enum")
    private String datatype;

    /** 计算公式 */
    @Excel(name = "计算公式",prompt = "选填,例如:%s*10,%s是占位符")
    private String formula;

    @Excel(name = "有效值范围")
    private String limitValue;

    @Excel(name = "单位")
    private String unit;

    /** 是否只读数据(0-否，1-是) */
    @Excel(name = "是否只读",readConverterExp = "0=否,1=是",prompt = "0=否,1=是")
    private Integer isReadonly;

    /** 是否历史存储(0-否，1-是) */
    @Excel(name = "是否历史存储(0-否，1-是)")
    private Integer isHistory;

    /** 是否首页显示（0-否，1-是） */
    @Excel(name = "是否首页显示", readConverterExp = "0=否,1=是")
    private Integer isChart;

    /** 是否实时监测（0-否，1-是） */
    @Excel(name = "是否实时监测", readConverterExp = "0=否,1=是")
    private Integer isMonitor;


}
