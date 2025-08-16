package com.fastbee.common.enums;

import com.fastbee.common.core.text.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 通用状态枚举

 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    SUCCESS(1, "成功"),
    FAIL(0, "失败");


    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
