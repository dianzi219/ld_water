package com.fastbee.iot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 设备对象 iot_device
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Device", description = "设备对象 iot_device")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Device extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备ID */
    @ApiModelProperty("设备ID")
    private Long deviceId;

    /** 设备名称 */
    @ApiModelProperty("设备名称")
    @Excel(name = "设备名称")
    private String deviceName;

    /** 产品ID */
    @ApiModelProperty("产品ID")
    @Excel(name = "产品ID")
    private Long productId;

    /** 产品名称 */
    @ApiModelProperty("产品名称")
    @Excel(name = "产品名称")
    private String productName;


    /** 租户ID */
    @ApiModelProperty("租户ID")
    @Excel(name = "租户ID")
    private Long tenantId;

    /** 租户名称 */
    @ApiModelProperty("租户名称")
    @Excel(name = "租户名称")
    private String tenantName;

    /** 设备编号 */
    @ApiModelProperty("设备编号")
    @Excel(name = "设备编号")
    private String serialNumber;

    /** 固件版本 */
    @ApiModelProperty("固件版本")
    @Excel(name = "固件版本")
    private BigDecimal firmwareVersion;


    /** WIFI固件版本 */
    @ApiModelProperty("HTTP推送固件版本")
    @Excel(name = "HTTP推送固件版本")
    private BigDecimal wirelessVersion;

    /** 设备类型（1-直连设备、2-网关设备、3-监控设备） */
    @ApiModelProperty("设备类型（1-直连设备、2-网关设备、3-监控设备 4-网关子设备）")
    private Integer deviceType;

    /** 设备状态（1-未激活，2-禁用，3-在线，4-离线） */
    @ApiModelProperty("设备状态（1-未激活，2-禁用，3-在线，4-离线）")
    @Excel(name = "设备状态")
    private Integer status;

    /** wifi信号强度（信号极好4格[-55— 0]，信号好3格[-70— -55]，信号一般2格[-85— -70]，信号差1格[-100— -85]） */
    @ApiModelProperty("wifi信号强度（信号极好4格[-55— 0]，信号好3格[-70— -55]，信号一般2格[-85— -70]，信号差1格[-100— -85]）")
    @Excel(name = "wifi信号强度")
    private Integer rssi;

    /** 设备影子 */
    @ApiModelProperty("是否启用设备影子(0=禁用，1=启用)")
    private Integer isShadow;

    /** 设备所在地址 */
    @ApiModelProperty("设备所在地址")
    @Excel(name = "设备所在地址")
    private String networkAddress;

    /** 设备入网IP */
    @ApiModelProperty("设备入网IP")
    @Excel(name = "设备入网IP")
    private String networkIp;

    /** 设备经度 */
    @ApiModelProperty("设备经度")
    @Excel(name = "设备经度")
    private BigDecimal longitude;

    /** 设备纬度 */
    @ApiModelProperty("设备纬度")
    @Excel(name = "设备纬度")
    private BigDecimal latitude;

    /** 激活时间 */
    @ApiModelProperty("激活时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "激活时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date activeTime;

    /** 子设备网关编号 */
    @ApiModelProperty("子设备网关编号")
    @Excel(name = "网关设备编号(子设备使用)")
    private String gwDevCode;

    /** 物模型值 */
    @ApiModelProperty("物模型值")
    @Excel(name = "物模型")
    private String thingsModelValue;

    /** 图片地址 */
    @ApiModelProperty("图片地址")
    private String imgUrl;

    /** 是否自定义位置 **/
    @ApiModelProperty("定位方式(1=ip自动定位，2=设备定位，3=自定义)")
    private Integer locationWay;

    /** 设备摘要 **/
    @ApiModelProperty("设备摘要")
    private String summary;

    /** 分组ID，用于分组查询 **/
    @ApiModelProperty("分组ID，用于分组查询")
    private Long groupId;

    /** 是否设备所有者，用于查询 **/
    @ApiModelProperty("是否设备所有者，用于查询")
    private Integer isOwner;
    /**子设备数量*/
    @ApiModelProperty("子设备数量")
    private Integer subDeviceCount;
    /**是否是模拟设备*/
    @ApiModelProperty("是否是模拟设备")
    private Integer isSimulate;
    /**子设备地址*/
    @ApiModelProperty("子设备地址")
    private Integer slaveId;
    /**设备传输协议*/
    @ApiModelProperty("设备传输协议")
    private String transport;

    @Transient
    @ApiModelProperty("机构id")
    private Long deptId;

    @Transient
    @ApiModelProperty("是否显示下级")
    private Boolean showChild;

    private List<Device> subDeviceList;

    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty("删除标志")
    private String delFlag;

    @Transient
    @ApiModelProperty("用户id")
    private Long userId;

    /**
     *  协议
     */
    private String protocolCode;

    /**
     * 关联组态,来源产品
     */
    private String guid;

    private List<SipRelation> sipRelationList;

    /**
     * 是否可以配置轮询
     */
    private Boolean canConfigPoll = false;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

}
