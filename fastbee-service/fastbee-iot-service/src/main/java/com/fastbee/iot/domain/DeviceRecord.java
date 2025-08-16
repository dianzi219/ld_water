package com.fastbee.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 设备记录对象 iot_device_record
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("iot_device_record")
public class DeviceRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 操作者机构id */
    @Excel(name = "操作者机构id")
    private Long operateDeptId;

    /** 目标机构id */
    @Excel(name = "目标机构id")
    private Long targetDeptId;

    /** 产品id */
    @Excel(name = "产品id")
    private Long productId;

    /** 设备id */
    @Excel(name = "设备id")
    private Long deviceId;

    /** 设备记录类型（1-导入记录；2-回收记录；3-分配记录；4-分配详细记录） */
    @Excel(name = "设备记录类型", readConverterExp = "1=-导入记录；2-回收记录；3-分配记录；4-分配详细记录")
    private Integer type;

    /** 分配类型（1-选择分配；2-导入分配） */
    @Excel(name = "分配类型", readConverterExp = "1=-选择分配；2-导入分配")
    private Integer distributeType;

    /** 总数 */
    @Excel(name = "总数")
    private Integer total;

    /** 成功数量 */
    @Excel(name = "成功数量")
    private Integer successQuantity;

    /** 失败数量 */
    @Excel(name = "失败数量")
    private Integer failQuantity;

    /** 状态 */
    @Excel(name = "状态")
    private Integer status;

    /** 租户id */
    @Excel(name = "租户id")
    private Long tenantId;

    /** 租户名称 */
    @Excel(name = "租户名称")
    private String tenantName;

    /** 逻辑删除标识 */
    private Integer delFlag;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String serialNumber;

    /** 父id */
    @Excel(name = "父id")
    private Long parentId;

}
