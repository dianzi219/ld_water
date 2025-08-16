package com.fastbee.common.core.notify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知配置参数属性VO类
 * @date 2024-01-09 14:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotifyConfigVO {

    /**
     * 配置属性
     */
    private String attribute;

    /**
     * 配置属性名称
     */
    private String name;

    /**
     * 配置属性样式 string-字符串；text-富文本；file-文件；boolean-启用；int-整数
     */
    private String type;

    /**
     * 值
     */
    private String value;
}
