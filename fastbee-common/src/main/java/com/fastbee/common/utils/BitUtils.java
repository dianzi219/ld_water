package com.fastbee.common.utils;

import java.util.Arrays;

/**
 *
 * @description 位运算工具
 * 用途：将二进制数中的每位数字1或0代表着某种开关标记，1为是,0为否，则一个数字可以代表N位的开关标记值，可有效减少过多的变量定义 或 过多的表字段
 */
public class BitUtils {

    /**
     * 获取二进制数字中指定位数的结果，如：1011,指定第2位，则结果是0，第3位，则结果是1
     *
     * @param num 二进制数（可以十进制数传入，也可使用0b开头的二进制数表示形式）
     * @param bit 位数（第几位，从右往左，从0开始）
     * @return
     */
    public static int getBitFlag(long num, int bit) {
        return (int) num >> bit & 1;
    }

    /**
     * 更新二进制数字中指定位的值
     *
     * @param num       二进制数（可以十进制数传入，也可使用0b开头的二进制数表示形式）
     * @param bit       位数（第几位，从右往左，从0开始）
     * @param flagValue 位标记值（true=1,false=0）
     * @return
     */
    public static long updateBitValue(long num, int bit, boolean flagValue) {
        if (flagValue) {
            //将某位由0改为1
            return num | (1 << bit);
        } else {
            //将某位由1改为0
            return num ^ (getBitFlag(num, bit) << bit);
        }
    }

    /**
     * 将数字转换为二制值形式字符串
     *
     * @param num
     * @return
     */
    public static String toBinaryString(long num) {
        return Long.toBinaryString(num);
    }

    /**
     * 判断10进制数，某位是0还是1
      * @param num
     * @param i
     * @return
     */
    public static int deter(int num, int i) {
        // 先将数字右移指定第i位，然后再用&与1运算
        return num >> (i-1) & 1;
    }

    public static String bin2hex(String input) {
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        System.out.println("原数据长度：" + (len / 8) + "字节");

        for (int i = 0; i < len / 4; i++){
            //每4个二进制位转换为1个十六进制位
            String temp = input.substring(i * 4, (i + 1) * 4);
            int tempInt = Integer.parseInt(temp, 2);
            String tempHex = Integer.toHexString(tempInt).toUpperCase();
            sb.append(tempHex);
        }

        return sb.toString();
    }
    public static int bin2Dec(String binaryString){
        int sum = 0;
        for(int i = 0;i < binaryString.length();i++){
            char ch = binaryString.charAt(i);
            if(ch > '2' || ch < '0')
                throw new NumberFormatException(String.valueOf(i));
            sum = sum * 2 + (binaryString.charAt(i) - '0');
        }
        return sum;
    }

    public static int[] string2Ins(String input) {
        StringBuilder in = new StringBuilder(input);
        int remainder = in.length() % 8;
        if (remainder > 0)
            for (int i = 0; i < 8 - remainder; i++)
                in.append("0");
        int[] result = new int[in.length() /8];

        // Step 8 Apply compression
        for (int i = 0; i < result.length; i++)
            result[i] =   Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);

        return result;
    }
    public static byte[] string2bytes(String input) {
        StringBuilder in = new StringBuilder(input);
        int remainder = in.length() % 8;
        if (remainder > 0)
            for (int i = 0; i < 8 - remainder; i++)
                in.insert(0,"0");
        byte[] bts = new byte[in.length() / 8];

        // Step 8 Apply compression
        for (int i = 0; i < bts.length; i++)
            bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);

        return bts;
    }

    /**
     * 取得十制数组的from~to位,并按照十六进制转化值
     *
     * @param data
     * @param from
     * @param to
     * @return
     */
    private static String getOctFromHexBytes(byte[] data, Object from, Object... to) {
        if (data != null && data.length > 0 && from != null) {
            try {
                byte[] value;
                int fromIndex = Integer.parseInt(from.toString());
                if (to != null && to.length > 0) {
                    int toIndex = Integer.parseInt(to[0].toString());
                    if (fromIndex >= toIndex || toIndex <= 0) {
                        value = Arrays.copyOfRange(data, fromIndex, fromIndex + 1);
                    } else {
                        value = Arrays.copyOfRange(data, fromIndex, toIndex + 1);
                    }
                } else {
                    value = Arrays.copyOfRange(data, fromIndex, fromIndex + 1);
                }
                if (value != null && value.length > 0) {
                    long octValue = 0L;
                    int j = -1;
                    for (int i = value.length - 1; i >= 0; i--, j++) {
                        int d = value[i];
                        if (d < 0) {
                            d += 256;
                        }
                        octValue += Math.round(d * Math.pow(16, 2 * j + 2));
                    }
                    return new Long(octValue).toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 十进制的字符串表示转成字节数组
     *
     * @param octString
     *            十进制格式的字符串
     * @param capacity
     *            需要填充的容量(可选)
     * @return 转换后的字节数组
     **/
    private static byte[] octInt2ByteArray(Integer oct, int... capacity) {
        return hexString2ByteArray(Integer.toHexString(oct), capacity);
    }
    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString
     *            16进制格式的字符串
     * @param capacity
     *            需要填充的容量(可选)
     * @return 转换后的字节数组
     **/
    private static byte[] hexString2ByteArray(String hexString, int... capacity) {
        hexString = hexString.toLowerCase();
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        int length = hexString.length() / 2;
        if (length < 1) {
            length = 1;
        }
        int size = length;
        if (capacity != null && capacity.length > 0 && capacity[0] >= length) {
            size = capacity[0];
        }
        final byte[] byteArray = new byte[size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            if (i < size - length) {
                byteArray[i] = 0;
            } else {
                byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
                if (k + 1 < hexString.length()) {
                    byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
                    byteArray[i] = (byte) (high << 4 | low);
                } else {
                    byteArray[i] = (byte) (high);
                }
                k += 2;
            }
        }
        return byteArray;

    }

    /**
     * 连接字节流
     *
     * @return
     */
    private static byte[] append(byte[] datas, byte[] data) {
        if (datas == null) {
            return data;
        }
        if (data == null) {
            return datas;
        } else {
            return concat(datas, data);
        }
    }

    /**
     * 字节流拼接
     *
     * @param data
     *            字节流
     * @return 拼接后的字节数组
     **/
    private static byte[] concat(byte[]... data) {
        if (data != null && data.length > 0) {
            int size = 0;
            for (int i = 0; i < data.length; i++) {
                size += data[i].length;
            }
            byte[] byteArray = new byte[size];
            int pos = 0;
            for (int i = 0; i < data.length; i++) {
                byte[] b = data[i];
                for (int j = 0; j < b.length; j++) {
                    byteArray[pos++] = b[j];
                }
            }
            return byteArray;
        }
        return null;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }




    public static void main(String[] args) {
        String s = bin2hex("1111111000000000");
        int i = bin2Dec("1111111000000000");
        byte[] ints = string2bytes("111111000000000");
        System.out.println(s);
        System.out.println(i);
    }

}
