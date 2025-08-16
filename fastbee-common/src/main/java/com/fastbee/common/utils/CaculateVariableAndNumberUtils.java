package com.fastbee.common.utils;

import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串公式计算工具
 */
public class CaculateVariableAndNumberUtils {

    /**
     * /*
     * 暂时只支持加减乘除及括号的应用
     */
    private static final String symbol = "+-,*/,(),%";

    private static final Map<String, Integer> symbol_map = new HashMap<String, Integer>(){{
        put("*", 1);
        put("/", 1);
        put("%", 1);
        put("+", 2);
        put("-", 2);
        put("(", 3);
        put(")", 3);
    }};


    /**
     * 公式计算 字符串
     *
     * @param exeStr
     */
    public static BigDecimal execute(String exeStr, Map<String, String> replaceMap) {
        List<String> list = suffixHandle(exeStr);
        System.out.println("计算结果： " + list);
        List<String> list1 = new ArrayList<>();
        for (String s : list) {
            String o = replaceMap.get(s);
            if (StringUtils.isNotEmpty(o)) {
                list1.add(o);
            } else {
                list1.add(s);
            }
        }
        return caculateAnalyse(list1);

    }

    /**
     * 公式计算 后序list
     *
     * @param suffixList
     * @return
     */
    public static BigDecimal caculateAnalyse(List<String> suffixList) {

        BigDecimal a = BigDecimal.ZERO;
        BigDecimal b = BigDecimal.ZERO;
        // 构建一个操作数栈  每当获得操作符号时取出最上面两个数进行计算。
        Stack<BigDecimal> caculateStack = new Stack<BigDecimal>();
        if (suffixList.size() > 1) {

            for (int i = 0; i < suffixList.size(); i++) {
                String temp = suffixList.get(i);
                if (symbol.contains(temp)) {
                    b = caculateStack.pop();
                    a = caculateStack.pop();
                    a = caculate(a, b, temp.toCharArray()[0]);
                    caculateStack.push(a);
                } else {
                    if (isNumber(suffixList.get(i))) {
                        caculateStack.push(new BigDecimal(suffixList.get(i)));
                    } else {
                        throw new RuntimeException("公式异常！");
                    }
                }
            }
        } else if (suffixList.size() == 1) {
            String temp = suffixList.get(0);
            if (isNumber(temp)) {
                a = BigDecimal.valueOf(Double.parseDouble(temp));
            } else {
                throw new RuntimeException("公式异常！");
            }
        }
        return a;
    }


    /**
     * 计算 使用double 进行计算  如果需要可以在这里使用bigdecimal 进行计算
     *
     * @param a
     * @param b
     * @param symbol
     * @return
     */
    public static BigDecimal caculate(BigDecimal a, BigDecimal b, char symbol) {
        switch (symbol) {
            case '+': {
                return a.add(b).stripTrailingZeros();
            }
            case '-':
                return a.subtract(b).stripTrailingZeros();
            case '*':
                return a.multiply(b);
            case '/':
            case '%':
                int length1 = getDivideLength(a, b);
                return a.divide(b, length1, BigDecimal.ROUND_HALF_UP);
            default:
                throw new RuntimeException("操作符号异常！");
        }

    }

    private static int getDivideLength(BigDecimal a, BigDecimal b) {
        String s1 = a.toString();
        String s2 = b.toString();
        int length1 = 0;
        int length2 = 0;
        if (s1.contains(".")) {
            length1 = s1.split("\\.")[1].length();
        }
        if (s2.contains(".")) {
            length2 = s2.split("\\.")[1].length();
        }
        if (length1 == 0 && length2 == 0) {
            return 2;
        } else {
            return Math.max(length1, length2);
        }
    }

    /**
     * 字符串直接 转 后序
     */
    public static List<String> suffixHandle(String exeStr) {
        StringBuilder buf = new StringBuilder();
        Stack stack = new Stack();
        char[] exeChars = exeStr.toCharArray();
        List<String> res = new ArrayList<>();
        for (char x : exeChars) {
            // 判断是不是操作符号
            if (symbol.indexOf(x) > -1) {
                // 不管怎样先将数据添加进列表
                if (buf.length() > 0) {
                    // 添加数据到res
                    String temp = buf.toString();
                    // 验证是否为变量或数字
                    if (!isVariableAndNumber(temp)) {
                        throw new RuntimeException(buf.append("  格式不对").toString());
                    }

                    // 添加到结果列表中
                    res.add(temp);
                    // 清空临时buf
                    buf.delete(0, buf.length());
                }
                if (!stack.isEmpty()) {

                    //2.判断是不是开是括号
                    if (x == '(') {
                        stack.push(x);
                        continue;
                    }
                    //3.判断是不是闭合括号
                    if (x == ')') {
                        boolean a = false;
                        while (!stack.isEmpty()) {
                            char con = (char) stack.peek();
                            if (con == '(' && !a) {
                                stack.pop();
                                a = true;
                            } else if (!a) {
                                res.add(String.valueOf(stack.pop()));
                            } else {
                                break;
                            }
                        }
                        continue;
                    }

                    // 遵循四则运算法则
                    int size = stack.size();
                    while (size > 0) {
                        char con = (char) stack.peek();
                        if (compare(con, x) > 0) {
                            res.add(String.valueOf(stack.pop()));
                        }
                        size--;
                    }
                    stack.push(x);

                } else {
                    stack.push(x);
                }
            } else {
                buf.append(x);
            }
        }
        if (buf.length() > 0) {
            res.add(buf.toString());
        }
        while (!stack.isEmpty()) {
            res.add(String.valueOf(stack.pop()));
        }
        return res;

    }

    /**
     * 比较两个操作符号的优先级
     *
     * @param a
     * @param b
     * @return
     */
    public static int compare(char a, char b) {
        String s1 = String.valueOf(a);
        String s2 = String.valueOf(b);
        Integer ai = symbol_map.get(s1);
        Integer bi = symbol_map.get(s2);
        if (null != ai && null != bi) {
            if (ai <= bi) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }


    /**
     * 判断是否为数 字符串
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 判断是否为数 字符串
     *
     * @param str
     * @return
     */
    public static boolean isVariable(String str) {
        Pattern pattern = Pattern.compile("^[A-Z]+$");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 判断是否为数 字符串
     *
     * @param str
     * @return
     */
    public static boolean isVariableAndNumber(String str) {
        Pattern pattern = Pattern.compile("[A-Z]|-?\\d+(\\.\\d+)?");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static String caculateReplace(String str, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            str = str.replaceAll(entry.getKey(), entry.getValue()==null ? "1" : entry.getValue());
        }
        return str;
    }

    public static String toFloat(byte[] bytes) throws IOException {
        ByteArrayInputStream mByteArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream mDataInputStream = new DataInputStream(mByteArrayInputStream);
        try {
            float v = mDataInputStream.readFloat();
            return String.format("%.6f",v);
        }catch (Exception e){
            throw new ServiceException("modbus16转浮点数错误");
        }
        finally {
            mDataInputStream.close();
            mByteArrayInputStream.close();
        }
    }

    /**
     * 转16位无符号整形
     * @param value
     * @return
     */
    public static String toUnSign16(long value) {
        long unSigned = value & 0xFFFF;
        return unSigned +""; // 将字节数组转换为十六进制字符串
    }

    /**
     * 32位有符号CDAB数据类型
     * @param value
     * @return
     */
    public static String toSign32_CDAB(long value) {
        byte[] bytes = intToBytes2((int) value);
        return bytesToInt2(bytes)+"";
    }

    /**
     * 32位无符号ABCD数据类型
     * @param value
     * @return
     */
    public static String toUnSign32_ABCD(long value) {
        return Integer.toUnsignedString((int) value);
    }

    /**
     * 32位无符号CDAB数据类型
     * @param value
     * @return
     */
    public static String toUnSign32_CDAB(long value) {
        byte[] bytes = intToBytes2((int) value);
        int val = bytesToInt2(bytes);
        return Integer.toUnsignedString(val);
    }

    /**
     * 转32位浮点数 ABCD
     * @param bytes
     * @return
     */
    public static float toFloat32_ABCD(byte[] bytes) {
        int intValue = (bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        return Float.intBitsToFloat(intValue);
    }

    /**
     * 转32位浮点数 CDAB
     * @param bytes
     * @return
     */
    public static Float toFloat32_CDAB(byte[] bytes) {
        int intValue = ((bytes[2] & 0xFF) << 24) | ((bytes[3] & 0xFF) << 16) |  ((bytes[0] & 0xFF) << 8) | ((bytes[1] & 0xFF)) ;
        return Float.intBitsToFloat(intValue);
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src) {
        return (((src[2] & 0xFF) << 24) | ((src[3] & 0xFF) << 16) | ((src[0] & 0xFF) << 8) | (src[1] & 0xFF));
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static String subHexValue(String hexString){
        //截取报文中的值
        String substring = hexString.substring(4, 6);
        int index = Integer.parseInt(substring);
        return hexString.substring(6, 6 + index*2);
    }


    public static void main(String[] args) throws IOException {
        String s1 = "A/B*C";  // 1.5
        String s2 = "E-((A+B)-(C+D))%10";  // 10.4
        String s3 = "A-B-C*(D-E)+10*5";  // 67
        String s4 = "A-B-C*(D+E)-(A+B)+(2+3)"; // -41
        String s5 = "A-(A-(B-C)*(D+E))%10+B"; // 1.5
        String s6 = "A-(B+C)*D+10";  // -9
        String s7 = "1+2*3-2+2*(1-2+3*4+5-6/2+(2-1)+3*4-2)%10"; // 9.8


        boolean number = isNumber("-10");
        System.out.println(number);

        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("A", "1");
        replaceMap.put("B", "2");
        replaceMap.put("C", "3");
        replaceMap.put("D", "4");
        replaceMap.put("E", "10");
        BigDecimal execute = execute(s7, replaceMap);
        System.out.println(execute);

    }

}
