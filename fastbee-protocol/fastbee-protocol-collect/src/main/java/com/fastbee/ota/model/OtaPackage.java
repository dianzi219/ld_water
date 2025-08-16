package com.fastbee.ota.model;


import com.fastbee.common.core.protocol.Message;
import com.fastbee.protocol.base.annotation.Column;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor
public class OtaPackage extends Message {
    /**
     * 1. 查询设备产品类型
     * 消息格式   55aa   00   01   0004   00000100   05
     *          帧头  版本  命令  数据长度  数据  校验和
     * 数据   00   00   01   00
     *      预留 产品类型 产品类型识别号 预留
     */
    @Column(length = 2, desc = "固定报文头：55aa", version = 0)
    protected int start = 0x55aa;
    @Column(length = 1, desc = "版本", version = 0)
    protected int version = 0x00;
    @Column(length = 1, desc = "命令字", version = 0)
    protected int code;
    @Column(length = 2, desc = "字节数", version = 0)
    protected int bitCount;
    @Column(totalUnit = 0, desc = "数据", version = 0)
    protected byte[] data;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        this.bitCount = bitCount;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OtaPackage{" +
                "start[固定报文头]=" + start +
                ", version[版本]=" + version +
                ", code[命令字]=" + code +
                ", bitCount[字节数]=" + bitCount +
                ", data[数据]=" + Arrays.toString(data) +
                '}';
    }
}
