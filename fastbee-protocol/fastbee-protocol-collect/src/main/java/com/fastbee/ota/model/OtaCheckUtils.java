package com.fastbee.ota.model;

import com.fastbee.common.utils.gateway.protocol.ByteUtils;
import io.netty.buffer.ByteBufUtil;

/**
 * @author bill
 */
public class OtaCheckUtils {

    /**
     * 校验和
     * 从帧头开始，按字节求和，得出的结果对256求余
     * @param source
     * @return
     */
    public static byte sumCheck(byte[] source) {
        int sum = 0;
        for (byte b : source) {
            //& 0xff 转换成无符号整型
            sum = sum + (b & 0xff);
        }
        sum = sum % 256;
        return (byte) (sum & 0xff);
    }

    /**
     * 添加校验和并返回
     * @param source
     * @return
     */
    public static byte[] addSumCheck(byte[] source) {
        byte[] result = new byte[source.length + 1];
        System.arraycopy(source, 0, result, 0, source.length);
        byte sumCheck = sumCheck(source);
        result[source.length] = sumCheck;
        return result;
    }

    public static void main(String[] args) {
        String hex = "55aa000A000101";
        byte[] bytes = ByteUtils.hexToByte(hex);
        byte[] addSumCheck = addSumCheck(bytes);
        System.out.println(ByteBufUtil.hexDump(addSumCheck));
        sumCheck(bytes);
    }
}
