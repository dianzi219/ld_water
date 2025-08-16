package com.fastbee.iot.util;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.constant.MagicValueConstants;
import com.fastbee.common.constant.SceneModelConstants;
import com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO;

import java.util.List;

/**
 * @author gsb
 * @date 2024/6/21 8:30
 */
public class JobCronUtils {


    /**
     * 处理时间周期Cron表达式
     * @param cycleType 时间周期类型
     * @param: cycle 表达式
     * @return com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO
     */
    public static SceneModelTagCycleVO handleCronCycle(Integer cycleType, String cycle) {
        String cron = "";
        String desc = "";
        List<SceneModelTagCycleVO> list = JSON.parseArray(cycle, SceneModelTagCycleVO.class);
        SceneModelTagCycleVO sceneModelTagCycleVO = list.get(0);
        switch (cycleType) {
            // 周期循环
            case 1:
                // 几分钟运算一次
                if (null != sceneModelTagCycleVO.getInterval()) {
                    if (sceneModelTagCycleVO.getInterval() >= MagicValueConstants.VALUE_3600) {
                        int hour = sceneModelTagCycleVO.getInterval() / MagicValueConstants.VALUE_3600;
                        cron = "0 0 0/" + hour +" * * ?";
                        desc = "每" + hour + "小时";
                    } else {
                        int min = sceneModelTagCycleVO.getInterval() / MagicValueConstants.VALUE_60;
                        cron = "0 0/" + min + " * * * ?";
                        desc = "每" + min + "分钟";
                    }
                }
                // 每小时运算一次
                if (null != sceneModelTagCycleVO.getType()
                        && SceneModelConstants.CYCLE_HOUR.equals(sceneModelTagCycleVO.getType())) {
                    cron = "0 0 0/1 * * ?";
                    desc = "每1小时";
                }
                // 每天几时运算一次
                if (SceneModelConstants.CYCLE_DAY.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getTime() + " * * ?";
                    desc = "每天" + sceneModelTagCycleVO.getTime() + "时";
                }
                // 每周周几几时运算一次
                if (SceneModelConstants.CYCLE_WEEK.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getWeek() && null != sceneModelTagCycleVO.getTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getTime() + " ? * " + sceneModelTagCycleVO.getWeek();
                    desc = "每周周" + sceneModelTagCycleVO.getWeek() + " " + sceneModelTagCycleVO.getTime() + "时";
                }
                // 每月第几日几时运算一次
                if (SceneModelConstants.CYCLE_MONTH.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getDay() && null != sceneModelTagCycleVO.getTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getTime() + " " + sceneModelTagCycleVO.getDay() + " * ?";
                    desc = "每月第" + sceneModelTagCycleVO.getDay() + "日" + sceneModelTagCycleVO.getTime() + "时";
                }
                break;
            // 自定义时间段，取结尾作为时间节点
            case 2:
                // 每日几时
                if (SceneModelConstants.CYCLE_DAY.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getToTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getToTime() + " * * ?";
                }
                // 每周周几运算一次
                if (SceneModelConstants.CYCLE_WEEK.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getToWeek() && null != sceneModelTagCycleVO.getToTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getToTime() + " ? * " + sceneModelTagCycleVO.getToWeek();
                }
                // 每月几号几时运算一次
                if (SceneModelConstants.CYCLE_MONTH.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getToDay() && null != sceneModelTagCycleVO.getToTime()) {
                    cron = "0 0 " + sceneModelTagCycleVO.getToTime() + " " + sceneModelTagCycleVO.getToDay() + " * ?";
                }
                break;
            default:
                break;
        }
        sceneModelTagCycleVO.setCron(cron);
        sceneModelTagCycleVO.setDesc(desc);
        return sceneModelTagCycleVO;
    }
}
