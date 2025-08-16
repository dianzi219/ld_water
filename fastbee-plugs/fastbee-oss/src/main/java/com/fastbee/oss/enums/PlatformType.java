package com.fastbee.oss.enums;

import com.fastbee.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public enum PlatformType {
    UNKNOWN(0, "未知"),
    LOCAL(1, "本地"),
    TENCENT(2, "腾讯云"),
    ALIYUN(3, "阿里云"),
    QINIU(4, "七牛云");
    private final Integer value;

    private final String text;

    public static PlatformType getTypeByValue(Integer value){
        if (StringUtils.isNull(value)){
            return null;
        }
        for (PlatformType enums : PlatformType.values()) {
            if (Objects.equals(enums.getValue(), value)) {
                return enums;
            }
        }
        return null;
    }
}
