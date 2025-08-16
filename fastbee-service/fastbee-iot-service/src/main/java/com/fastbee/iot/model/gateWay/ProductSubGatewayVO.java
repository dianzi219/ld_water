package com.fastbee.iot.model.gateWay;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 网关与子产品关联对象 iot_product_sub_gateway
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */

@Data
public class ProductSubGatewayVO implements Serializable{
    private static final long serialVersionUID=1L;

    /** 业务id */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("业务id")
    private Long id;

    /** 网关产品id */
    private Long gwProductId;

    /** 子产品id */
    private Long subProductId;

    /**
     * 子产品名称
     */
    private String subProductName;

    /** 从机地址 */
    private Integer slaveId;

    /** 备注 */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 子产品定位方式
     */
    private Integer subLocationWay;

}
