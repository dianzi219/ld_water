package com.fastbee.iot.domain;

import lombok.Data;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 规则引擎脚本对象 rule_script
 *
 * @author lizhuangpeng
 * @date 2023-07-01
 */
@Data
public class Script extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 脚本ID */
    @Excel(name = "脚本ID")
    private String scriptId;

    /** 用户ID */
    private Long userId;

    /** 用名称 */
    private String userName;

    /** 应用名 */
    @Excel(name = "应用名")
    private String applicationName;

    /** 脚本名 */
    @Excel(name = "脚本名")
    private String scriptName;

    /** 脚本数据 */
    @Excel(name = "脚本数据")
    private String scriptData;

    /** 脚本类型:script=普通脚本，switch_script=选择脚本,boolean_script=条件脚本，for_script=数量循环脚本 */
    @Excel(name = "脚本类型")
    private String scriptType;

    /** 场景ID */
    private Long sceneId;

    /** 脚本类型(1=设备上报，2=平台下发，3=设备上线，4=设备离线，5=http桥接数据，6=mqtt桥接数据) */
    private Integer scriptEvent;

    /** 脚本动作(1=消息重发，2=消息通知，3=Http推送，4=Mqtt桥接，5=数据库存储) */
    private Integer scriptAction;

    /** 脚本用途(1=数据流，2=触发器，3=执行动作) */
    private Integer scriptPurpose;

    /** 脚本执行顺序，值越大优先级越高 */
    private Integer scriptOrder;

    /** 脚本语言（groovy | qlexpress | js | python | lua | aviator） */
    @Excel(name = "脚本语言", readConverterExp = "groovy,qlexpress,js,python,lua,aviator")
    private String scriptLanguage;

    /** 产品ID */
    private Long productId;

    /** 产品名称 */

    private String productName;

    /**是否生效*/
    private Integer enable;

    private Long bridgeId;

    private String bridgeName;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

}
