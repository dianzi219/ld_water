package com.fastbee.iot.domain;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品固件对象 iot_firmware
 *
 * @author kerwincui
 * @date 2024-08-18
 */

@ApiModel(value = "Firmware" , description = "产品固件 iot_firmware" )
@Data
@TableName("iot_firmware" )
public class Firmware {
private static final long serialVersionUID=1L;


    /** 固件ID */
    @TableId(value = "firmware_id", type = IdType.AUTO)
    @ApiModelProperty("固件ID" )
    private Long firmwareId;


    /** 固件名称 */
    @ApiModelProperty("固件名称" )
    private String firmwareName;


    /** 1,二进制包升级2.http升级 */
    @ApiModelProperty("1,二进制包升级2.http升级" )
    private Integer firmwareType;


    /** 产品ID */
    @ApiModelProperty("产品ID" )
    private Long productId;


    /** 产品名称 */
    @ApiModelProperty("产品名称" )
    private String productName;


    /** 租户ID */
    @ApiModelProperty("租户ID" )
    private Long tenantId;


    /** 租户名称 */
    @ApiModelProperty("租户名称" )
    private String tenantName;


    /** 是否系统通用（0-否，1-是） */
    @ApiModelProperty("是否系统通用" )
    private Integer isSys;


    /** 是否最新版本（0-否，1-是） */
    @ApiModelProperty("是否最新版本" )
    private Integer isLatest;


    /** 固件版本 */
    @ApiModelProperty("固件版本" )
    private BigDecimal version;


    /** 分包字节大小 */
    @ApiModelProperty("分包字节大小" )
    private Integer byteSize;


    /** 文件路径 */
    @ApiModelProperty("文件路径" )
    private String filePath;


    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty("删除标志" )
    private String delFlag;


    /** 创建者 */
    @ApiModelProperty("创建者" )
    private String createBy;


    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd" )
    @ApiModelProperty("创建时间" )
    private Date createTime;


    /** 更新者 */
    @ApiModelProperty("更新者" )
    private String updateBy;


    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd" )
    @ApiModelProperty("更新时间" )
    private Date updateTime;


    /** 备注 */
    @ApiModelProperty("备注" )
    private String remark;

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

   public void setParams(Map<String, Object> params){
        this.params = params;
   }


}
