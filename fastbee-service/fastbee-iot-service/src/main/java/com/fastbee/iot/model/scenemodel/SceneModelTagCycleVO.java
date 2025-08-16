package com.fastbee.iot.model.scenemodel;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author fastb
 * @version 1.0
 * @description: 运算变量时间周期
 * @date 2024-06-04 10:57
 */
@Data
public class SceneModelTagCycleVO {

    public static void main(String[] args) {
        String s = "[{\"day\": \"1\", \"time\": \"00\", \"type\": \"month\", \"toDay\": \"3\", \"toTime\": \"02\", \"toType\": \"4\"}]";
        List<SceneModelTagCycleVO> sceneModelTagCycleVO = JSON.parseArray(s, SceneModelTagCycleVO.class);
        System.out.println(JSON.toJSONString(sceneModelTagCycleVO.get(0)));
    }

    /**
     * 秒   几分钟、几小时运算一次
     */
    private Integer interval;

    /**
     * 运算时间  hour-每小时  day-每日  week-每周  month-月
     */
    private String type;

    /**
     * 每日几时、每周周几几时、每月几日几时
     */
    private Integer time;

    /**
     * 周几
     */
    private Integer week;

    /**
     * 每月几日
     */
    private Integer day;

    /**
     * 当日、次日几时
     */
    private Integer toTime;

    /**
     * 1-当日；2-次日；3-周；4-月
     */
    private Integer toType;

    /**
     * 至周几
     */
    private Integer toWeek;

    /**
     * 至几日
     */
    private Integer toDay;

    /**
     * 转换后的cron
     */
    private String cron;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 描述
     */
    private String desc;
}
