package com.fastbee.protocol.util;

import java.util.Arrays;
import java.util.Map;

/**
 * Integer转 byte
 */
public class IntegerToByteUtil {

    /**
     * 将int转换为byte数组并指定长度
     *
     * @param data    int数据
     * @param byteLen 指定长度
     * @return byte[]
     */
    public static byte[] intToByteArr(int data, int byteLen) {
        byte[] bytes = new byte[byteLen];
        for (int i = 0; i < bytes.length; i++) {
            bytes[byteLen - i - 1] = (byte) (data % 256);
            data = data / 256;
        }
        return bytes;
    }


    /**
     * byte转int
     *
     * @param b
     * @return
     */
    public static int byteToInt(byte b) {
        return b & 0xff;
    }

    /**
     * byte[]转int
     *
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] src) {
            int value;
            int offset = 0;
            value = (int) ((src[offset] & 0xFF)
                    | ((src[offset+1] & 0xFF)<<8)
                    | ((src[offset+2] & 0xFF)<<16)
                    | ((src[offset+3] & 0xFF)<<24));
            return value;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占2个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /**
     * 将不满4位的byte数组转为四位
     *
     * @param src 不满四位数组
     * @return 满4位数组
     */
    public static byte[] bytesToBytes4(byte[] src) {
        if (src.length < 4) {
            byte[] result = new byte[4];
            for (int i = 0; i < 4; i++) {
                if (i >= 4 - src.length) {
                    result[i] = src[Math.abs(4 - i - src.length)];
                } else {
                    result[i] = 0; //补全0
                }
            }
            return result;
        } else {
            return src;
        }
    }

    /**
     * 将16进制转换为二进制
     */
    public static String hexString2binaryString(int num, int radix) {

        StringBuffer sb = new StringBuffer();
        switch (radix) {
            case 2:
                String s = Integer.toBinaryString(num);
                for (int i = 0; i < 16 - s.length(); i++) {
                    sb.append("0");
                }
                return sb.append(s).toString();
            case 8:
                return Integer.toOctalString(num);
            case 16:
                return Integer.toHexString(num);
            default:
                return null;
        }
    }


    public static int binaryToInt(String binary) {
        return Integer.parseInt(binary, 2);
    }

    /**
     * 将某个数的n位变为1或0
     *
     * @param value    1/0
     * @param data     待修改的数据
     * @param position 更改位置
     * @return 结果
     */
    public static int bitOperation(int data, int value, int position) {

        if (value == 0) {
            return data & ~(1 << position);
        } else if (value == 1) {
            return data | (1 << position);
        }
        return data;
    }

    /**
     * 批量修改bit位
     *
     * @param data 源数据
     * @param map  数据map
     * @return 结果
     */
    public static int bitOperationBatch(int data, Map<Integer, Integer> map) {
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 0) {
                data = data & ~(1 << entry.getKey());
            } else if (entry.getValue() == 1) {
                data = data | (1 << entry.getKey());
            }
        }
        return data;
    }

    private static String encodeHexString(byte[] data) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] out = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xFF;//取byte的后八位
            out[i * 2] = hexArray[v >>> 4];
            out[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(out);

    }

    public static String intToHexString6(int number){
        StringBuilder sb = new StringBuilder();
        String hexString = Integer.toHexString(number);
        for (int i = 0; i < 12 - hexString.length(); i++) {
            sb.append("0");
        }
       return sb.append(hexString).toString();
    }


    public static void main(String[] args) {

        byte[] bytes = intToByteArr(-40, 2);
        System.out.println("将整数转换为byte数组并指定长度---" + Arrays.toString(bytes));
        System.out.println(IntegerToByteUtil.encodeHexString(bytes));

        String hexString6 = intToHexString6(17);
        byte[] bytes1 = ByteToHexUtil.hexToByteArray(hexString6);

        int ret = bitOperation(0, 98, 6);
        byte[] bytes2 = IntegerToByteUtil.intToByteArr(ret, 2);
        byte[] b1 = {(byte) 0x01,(byte) 0x02};
        System.out.println(ByteToHexUtil.bytesToHexString(b1));
        System.out.println(ByteToHexUtil.bytesToHexString(bytes2));

    }
}
