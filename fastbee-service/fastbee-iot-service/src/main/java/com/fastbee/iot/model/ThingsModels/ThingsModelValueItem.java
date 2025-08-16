package com.fastbee.iot.model.ThingsModels;

import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 物模型值的项
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class  ThingsModelValueItem {
    /**
     * 物模型唯一标识符
     */
    private String id;

    /**
     * 物模型名称
     **/
    private String name;


    /**
     * 中文物模型名称
     **/
    private String name_zh_CN;

    /**
     * 英文物模型名称
     **/
    private String name_en_US;

    /**
     * 物模型值
     */
    private String value;

    /**
     * 影子值
     **/
    private String shadow;

    /**
     * 是否为监测值
     **/
    private Integer isMonitor;

    /**
     * 是否为历史存储
     **/
    private Integer isHistory;

    /**
     * 是否为图表展示
     **/
    private Integer isChart;

    /**
     * 是否只读数据
     **/
    private Integer isReadonly;

    /**
     * 是否在APP展示
     */
    private Integer isApp;

    /**
     * 计算公式
     */
    private String formula;
    /**
     * modbus参数配置
     */
    private ModbusConfig config;

    private Integer groupId;

    /** 排序 */
    private Integer order;
    /**
     * 物模型类型
     */
    private Integer type;

    private Datatype datatype;

    private Integer isSharePerm;

    private Long modelId;


    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ts;

}
