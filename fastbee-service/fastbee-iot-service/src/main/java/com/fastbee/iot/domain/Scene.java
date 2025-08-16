package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * 场景联动对象 iot_scene
 *
 * @author kerwincui
 * @date 2023-12-27
 */
@Data
public class Scene extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 场景ID */
    private Long sceneId;

    private Long recoverId;

    /** 场景名称 */
    @Excel(name = "场景名称")
    private String sceneName;

    /** 规则名称 */
    private String chainName;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名称 */
    @Excel(name = "用户名称")
    private String userName;

    /** 执行条件（1=或、任一条件，2=且、所有条件，3=非，不满足） */
    @Excel(name = "执行条件", readConverterExp = "1==或、任一条件，2=且、所有条件，3=非，不满足")
    private Integer cond;

    /** 静默周期（分钟） */
    @Excel(name = "静默周期", readConverterExp = "分=钟")
    private Integer silentPeriod;

    /** 执行方式（1=串行，顺序执行，2=并行，同时执行） */
    @Excel(name = "执行方式", readConverterExp = "1==串行，顺序执行，2=并行，同时执行")
    private Integer executeMode;

    /** 延时执行（秒钟） */
    @Excel(name = "延时执行", readConverterExp = "秒=钟")
    private Integer executeDelay;

    @Excel(name = "延时匹配", readConverterExp = "秒=钟")
    private Integer checkDelay;

    /** 是否包含告警推送（1=包含，2=不包含） */
    @Excel(name = "是否包含告警推送", readConverterExp = "1==包含，2=不包含")
    private Integer hasAlert;

    /** 场景状态（1-启动，2-停止） */
    @Excel(name = "场景状态", readConverterExp = "1=-启动，2-停止")
    private Integer enable;

    /** 规则数据 */
    @Excel(name = "规则数据")
    private String elData;

    /** 应用名称 */
    @Excel(name = "应用名称")
    private String applicationName;

    /** 接收的触发器列表 */
    private List<SceneScript> triggers;

    /** 接收的执行动作列表 */
    private List<SceneScript> actions;

    /**
     * 执行动作数量
     */
    private Integer actionCount = 0;

    /**
     * 是否是终端用户 1-是；0-否
     */
    private Integer terminalUser;
}
