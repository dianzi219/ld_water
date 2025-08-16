package com.fastbee.common.utils.date;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类，用于 {@link LocalDateTime}
 *
 * @author fastbee
 */
public class LocalDateTimeUtils {

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 空的 LocalDateTime 对象，主要用于 DB 唯一索引的默认值
     */
    public static LocalDateTime EMPTY = buildTime(1970, 1, 1);

    public static LocalDateTime addTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

    public static boolean beforeNow(LocalDateTime date) {
        return date.isBefore(LocalDateTime.now());
    }

    public static boolean afterNow(LocalDateTime date) {
        return date.isAfter(LocalDateTime.now());
    }

    /**
     * 创建指定时间
     *
     * @param year  年
     * @param mouth 月
     * @param day   日
     * @return 指定时间
     */
    public static LocalDateTime buildTime(int year, int mouth, int day) {
        return LocalDateTime.of(year, mouth, day, 0, 0, 0);
    }

    public static LocalDateTime[] buildBetweenTime(int year1, int mouth1, int day1,
                                                   int year2, int mouth2, int day2) {
        return new LocalDateTime[]{buildTime(year1, mouth1, day1), buildTime(year2, mouth2, day2)};
    }

    /**
     * 判断当前时间是否在该时间范围内
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否
     */
    public static boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return LocalDateTimeUtil.isIn(LocalDateTime.now(), startTime, endTime);
    }

    /**
     * 时间转字符串
     * @param localDateTime 时间
     * @param: format 格式
     * @return java.lang.String
     */
    public static String localDateTimeToStr(LocalDateTime localDateTime, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(dateTimeFormatter);
    }
}
