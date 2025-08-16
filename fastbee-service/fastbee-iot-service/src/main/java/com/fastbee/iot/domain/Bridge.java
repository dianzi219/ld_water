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
 * 数据桥接对象 bridge
 *
 * @author kerwincui
 * @date 2024-08-20
 */

@ApiModel(value = "Bridge", description = "数据桥接 bridge")
@Data
@TableName("bridge" )
public class Bridge implements Serializable{
    private static final long serialVersionUID=1L;

    /** id唯一标识 */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("id唯一标识")
    private Long id;

    /** 桥接配置信息 */
    @ApiModelProperty("桥接配置信息")
    private String configJson;

    /** 连接器名称 */
    @ApiModelProperty("连接器名称")
    private String name;

    /** 是否生效（0-不生效，1-生效） */
    @ApiModelProperty("是否生效")
    private String enable;

    /** 状态（0-未连接，1-连接中） */
    @ApiModelProperty("状态")
    private String status;

    /** 桥接类型(3=Http推送，4=Mqtt桥接，5=数据库存储) */
    @ApiModelProperty("桥接类型(3=Http推送，4=Mqtt桥接，5=数据库存储)")
    private Integer type;

    /** 桥接方向(1=输入，2=输出) */
    @ApiModelProperty("桥接方向(1=输入，2=输出)")
    private Integer direction;

    /** 转发路由（mqtt topic，http url） */
    @ApiModelProperty("转发路由")
    private String route;

    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty("删除标志")
    private String delFlag;

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
