package com.fastbee.protocol.util;

import org.apache.commons.lang3.ArrayUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.UnsupportedEncodingException;

/**
 * Bytes和 Hex转换工具类
 */
public class ByteToHexUtil {

    /**
     * 单个Hex字符串转byte
     *
     * @param hexStr
     * @return
     */
    public static byte hexToByte(String hexStr) {
        return (byte) Integer.parseInt(hexStr, 16);
    }

    /**
     * hex字符串转为 byte数组
     *
     * @param inHex 转换的 hex字符串
     * @return 转换后的 byte数组
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            // 奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            // 偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * 单个字节转换十六进制
     *
     * @param b byte字节
     * @return 转换后的单个hex字符
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    /**
     * byte数组转换炒年糕十六进制字符串
     *
     * @param bArray byte数组
     * @return hex字符串
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        for (int i = 0; i < bArray.length; i++) {
            String hexStr = Integer.toHexString(0xFF & bArray[i]);
            if (hexStr.length() < 2)
                sb.append(0);
            sb.append(hexStr.toUpperCase());
        }
        return sb.toString();
    }





    /**
     * 将hex转为正负数 2个字节
     *
     * @param hexStr hex字符串
     * @return 结果
     */
    public static int parseHex2(String hexStr) {
        if (hexStr.length() != 4) {
            throw new NumberFormatException("Wrong length: " + hexStr.length() + ", must be 4.");
        }
        int ret = Integer.parseInt(hexStr, 16);
        ret = ((ret & 0x8000) > 0) ? (ret - 0x10000) : (ret);
        return  ret;
    }
    public static Integer cutMessageHexTo(byte[] source, int startIndex, int endIndex){
        byte[] subarray = ArrayUtils.subarray(source, startIndex, endIndex);
        String s = bytesToHexString(subarray);
        return Integer.parseInt(s,16);
    }

    public static String bSubstring(String s,int start, int length) throws Exception
    {

        byte[] bytes = s.getBytes("Unicode");
        int n = 0; // 表示当前的字节数
        int i = start; // 要截取的字节数，从第3个字节开始
        for (; i < bytes.length && n < length; i++)
        {
            // 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
            if (i % 2 == 1)
            {
                n++; // 在UCS2第二个字节时n加1
            }
            else
            {
                // 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
                if (bytes[i] != 0)
                {
                    n++;
                }
            }
        }
        // 如果i为奇数时，处理成偶数
        if (i % 2 == 1)

        {
            // 该UCS2字符是汉字时，去掉这个截一半的汉字
            if (bytes[i - 1] != 0)
                i = i - 1;
                // 该UCS2字符是字母或数字，则保留该字符
            else
                i = i + 1;
        }

        return new String(bytes, 0, i, "Unicode");
    }

    /**
     *
     * @param orignal 要截取的字符串
     * @param start   开始下标
     * @param count   截取长度
     * @return
     */
    public static String substringByte(String orignal, int start, int count) {
        // 如果目标字符串为空，则直接返回，不进入截取逻辑；
        if (orignal == null || "".equals(orignal)){
            return orignal;
        }

        // 截取Byte长度必须>0
        if (count <= 0) {
            return orignal;
        }

        // 截取的起始字节数必须比
        if (start < 0) {
            start = 0;
        }

        // 目标char Pull buff缓存区间；
        StringBuffer buff = new StringBuffer();

        try {
            // 截取字节起始字节位置大于目标String的Byte的length则返回空值
            if (start >= getStringByteLenths(orignal)) {
                return null;
            }

            int len = 0;
            char c;

            // 遍历String的每一个Char字符，计算当前总长度
            // 如果到当前Char的的字节长度大于要截取的字符总长度，则跳出循环返回截取的字符串。
            for (int i = 0; i < orignal.toCharArray().length; i++) {

                c = orignal.charAt(i);

                // 当起始位置为0时候
                if (start == 0) {
                    len += String.valueOf(c).getBytes("GBK").length;
                    if (len <= count) {
                        buff.append(c);
                    }else {
                        break;
                    }
                } else {
                    // 截取字符串从非0位置开始
                    len += String.valueOf(c).getBytes("GBK").length;
                    if (len > start && len <= start + count) {
                        buff.append(c);
                    }
                    if (len > start + count) {
                        break;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 返回最终截取的字符结果;
        // 创建String对象，传入目标char Buff对象
        return new String(buff);
    }

    public static int getStringByteLenths(String args) throws UnsupportedEncodingException {
        return args != null && args != "" ? args.getBytes("Unicode").length : 0;
    }



    public static void main(String[] args) {

        //String str = "000000000111";
        //byte[] result = hexToByteArray(str);
        //System.out.println("hex字符串转byte数组 ---" + Arrays.toString(result));
        //
        //System.out.println("hex字符串---"+bytesToHexString(result));
        //int parseInt = Integer.parseInt(str, 16);
        //System.out.println(parseInt);
        //
        //int va = -40;
        //byte[] bytes = IntegerToByteUtil.intToBytes2(va);
        //System.out.println("hex字符串转byte数组 ---" + Arrays.toString(bytes));
        //
        //int num = 1713;
        //String hexString = Integer.toHexString(num);
        //System.out.println(hexString);

        ScriptEngine js = new ScriptEngineManager().getEngineByName("JavaScript");
        String str = "8*%s";
        try {
            System.out.println(js.eval(str.replace("%s","0.42")));
        }catch (Exception e){

        }
    }

}
