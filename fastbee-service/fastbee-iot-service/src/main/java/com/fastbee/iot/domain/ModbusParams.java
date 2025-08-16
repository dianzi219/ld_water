package com.fastbee.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 产品modbus配置参数对象 iot_modbus_params
 *
 * @date 2024-08-20
 */

@ApiModel(value = "ModbusParams", description = "产品modbus配置参数 iot_modbus_params")
@Data
@TableName("iot_modbus_params" )
public class ModbusParams implements Serializable{
    private static final long serialVersionUID=1L;

    /** 业务id */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("业务id")
    private Long id;

    /** 产品id */
    @ApiModelProperty("产品id")
    private Long productId;

    /** 是否启动云端轮训(1-云端轮训,2-边缘采集) */
    @ApiModelProperty("是否启动云端轮训(0-云端轮训,1-边缘采集)")
    private Integer pollType;

    /** 默认的子设备地址 */
    @ApiModelProperty("默认的子设备地址")
    private Integer slaveId;

    /** 子设备状态判断方式 1-设备数据 2- 网关 */
    @ApiModelProperty("子设备状态判断方式 1-设备数据 2- 网关")
    private Integer statusDeter;

    /** 设备数据来判断子设备状态的时长(s) */
    @ApiModelProperty("设备数据来判断子设备状态的时长(s)")
    private String deterTimer;

    /** 批量读取的个数 */
    @ApiModelProperty("批量读取的个数")
    private Integer pollLength;

    /** 创建者 */
    @ApiModelProperty("创建者")
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("创建时间")
    private Date createTime;

    /** 更新者 */
    @ApiModelProperty("更新者")
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /** 备注 */
    @ApiModelProperty("备注")
    private String remark;

    @Setter
    @TableField(exist = false)
    @ApiModelProperty("请求参数")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;

    public Map<String, Object> getParams(){
        if (params == null){
            params = new HashMap<>();
        }
        return params;
    }

}
