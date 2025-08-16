package com.fastbee.common.core.thingsModel;

import com.fastbee.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;

/**
 * 物模型值的项
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@AllArgsConstructor
@Builder
public class ThingsModelSimpleItem
{
    /** 物模型唯一标识符 */
    private String id;

    /** 物模型值 */
    private String value;

    private String name;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts;

    /** 备注 **/
    private String remark;


    public ThingsModelSimpleItem(String id, String value , String remark){
        this.id=id;
        this.value=value;
        this.remark=remark;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts  != null ? ts : DateUtils.getNowDate();
    }

    public ThingsModelSimpleItem(){}

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
