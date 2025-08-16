package com.fastbee.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fastbee.common.annotation.Excel;
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
 * 产品轮训任务列对象 iot_product_modbus_job
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */

@ApiModel(value = "ProductModbusJob", description = "产品轮训任务列 iot_product_modbus_job")
@Data
@TableName("iot_product_modbus_job" )
public class ProductModbusJob implements Serializable{
    private static final long serialVersionUID=1L;

    /** 任务id */
    @TableId(value = "task_id", type = IdType.AUTO)
    @ApiModelProperty("任务id")
    private Long taskId;

    /** 任务名称 */
    @ApiModelProperty("任务名称")
    private String jobName;

    /** 产品id */
    @ApiModelProperty("产品id")
    private Long productId;

    /** 指令 */
    @ApiModelProperty("指令")
    private String command;

    /** 创建者 */
    @ApiModelProperty("创建者")
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("创建时间")
    private Date createTime;

    /** 备注信息 */
    @ApiModelProperty("备注信息")
    private String remark;

    /**
     * 状态（0正常 1暂停）
     */
    @ApiModelProperty("状态")
    private String status;

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
