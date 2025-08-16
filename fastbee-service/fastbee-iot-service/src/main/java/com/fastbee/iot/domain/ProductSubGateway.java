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
 * 网关与子产品关联对象 iot_product_sub_gateway
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */

@ApiModel(value = "ProductSubGateway", description = "网关与子产品关联 iot_product_sub_gateway")
@Data
@TableName("iot_product_sub_gateway" )
public class ProductSubGateway implements Serializable{
    private static final long serialVersionUID=1L;

    /** 业务id */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("业务id")
    private Long id;

    /** 网关产品id */
    @ApiModelProperty("网关产品id")
    private Long gwProductId;

    /** 子产品id */
    @ApiModelProperty("子产品id")
    private Long subProductId;

    /** 从机地址 */
    @ApiModelProperty("从机地址")
    private Integer slaveId;

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
