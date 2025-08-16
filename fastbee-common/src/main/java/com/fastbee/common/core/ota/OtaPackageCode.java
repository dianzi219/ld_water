package com.fastbee.common.core.ota;

import com.fastbee.common.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OtaPackageCode {

    OTA_01("查询产品工装号",(byte) 0x01),
    OTA_0A("OTA升级启动",(byte) 0x0A),
    OTA_0B("OTA升级包传输",(byte) 0x0B)
    ;

    private String desc;
    private byte code;

    public static OtaPackageCode getInstance(int code) {
        switch ((byte)code) {
            case 0x01:
                return OTA_01;
            case 0x0A:
                return OTA_0A;
            case 0x0B:
                return OTA_0B;
            default:
                throw new ServiceException("功能码[" + code + "],未定义");
        }
    }

    public static String getDes(int code){
        switch ((byte)code) {
            case 0x01:
                return OTA_01.desc;
            case 0x0A:
                return OTA_0A.desc;
            case 0x0B:
                return OTA_0B.desc;
            default:
                return "UNKOWN";
        }
    }

}
