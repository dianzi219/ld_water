package com.fastbee.iot.model;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 设备记录对象 iot_device_record
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRecordVO extends BaseEntity
{

    /** 主键id */
    private Long id;

    /** 操作者机构id */
    @Excel(name = "归属机构id")
    private Long operateDeptId;

    /** 操作者机构id */
    @Excel(name = "归属机构名称")
    private String operateDeptName;

    /** 目标机构id */
    @Excel(name = "目标机构id")
    private Long targetDeptId;

    /** 目标机构id */
    @Excel(name = "目标机构名称")
    private String targetDeptName;

    /** 产品id */
    @Excel(name = "产品id")
    private Long productId;

    /** 产品id */
    @Excel(name = "产品名称")
    private String productName;

    /** 设备id */
    @Excel(name = "设备id")
    private Long deviceId;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String serialNumber;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 设备记录类型（1-导入记录；2-回收记录；3-分配记录；4-分配详细记录） */
//    @Excel(name = "设备记录类型", readConverterExp = "1=-导入记录；2-回收记录；3-分配记录；4-分配详细记录")
    private Integer type;

    /** 分配类型（1-选择分配；2-导入分配） */
//    @Excel(name = "分配类型", readConverterExp = "1=-选择分配；2-导入分配")
    private Integer distributeType;

    /** 分配类型描述（1-选择分配；2-导入分配） */
    @Excel(name = "分配类型")
    private String distributeTypeDesc;

    /** 总数 */
    private Long total;

    /** 成功数量 */
    private Long successQuantity;

    /** 失败数量 */
    private Long failQuantity;

    /** 状态（0-失败；1-成功） */
    @Excel(name = "状态（0-失败；1-成功）")
    private Integer status;

    /** 租户id */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    /** 父id */
    private Long parentId;

}
