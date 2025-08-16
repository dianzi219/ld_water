package com.fastbee.ld.model;

public class FlowDevParser {

    /**
     * 解析时间标签ts
     * 格式要求：6字节BCD码，对应秒、分、时、日、月、年（后两位）
     * 参考文档1-266、1-267、1-268（表10）
     *
     * @param ts 时间标签字符串，长度为12
     * @return 解析后的时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String parseTs(String ts) {
        if (ts == null || ts.length() != 12) {
            throw new IllegalArgumentException("ts格式错误，长度应为12");
        }
        String sec = ts.substring(0, 2);
        String min = ts.substring(2, 4);
        String hour = ts.substring(4, 6);
        String day = ts.substring(6, 8);
        String month = ts.substring(8, 10);
        String year = ts.substring(10, 12);
        return String.format("20%s-%s-%s %s:%s:%s", year, month, day, hour, min, sec);
    }

    /**
     * 解析累积流量sum
     * 格式要求：5字节压缩BCD码，单位m³
     * 参考文档1-483（表36）
     *
     * @param sum 累积流量字符串，长度为10
     * @return 解析后的累积流量值
     */
    public static String parseSum(String sum) {
        if (sum == null || sum.length() != 10) {
            throw new IllegalArgumentException("sum格式错误，长度应为10");
        }
        long byte1 = Long.parseLong(sum.substring(0, 2));
        long byte2 = Long.parseLong(sum.substring(2, 4)) * 100;
        long byte3 = Long.parseLong(sum.substring(4, 6)) * 10000;
        long byte4 = Long.parseLong(sum.substring(6, 8)) * 1000000;
        long byte5 = Long.parseLong(sum.substring(8, 10)) * 100000000;
        return String.valueOf(byte1 + byte2 + byte3 + byte4 + byte5);
    }

    /**
     * 解析瞬时流量instant
     * 格式要求：5字节压缩BCD码，单位m³/s或m³/h
     * 参考文档1-481（表35）：BYTE5中D3-D0为BCD码十万位，D5-D4为单位（00B=m³/s，11B=m³/h）
     *
     * @param instant 瞬时流量字符串，长度为10
     * @return 解析后的瞬时流量信息（含数值和单位）
     */
    public static String parseInstant(String instant) {
        if (instant == null || instant.length() != 10) {
            throw new IllegalArgumentException("instant格式错误，长度应为10");
        }
        // 拆分各字节（每2位对应1字节）
        String byte1 = instant.substring(0, 2); // 千分位、百分位（BCD码）
        String byte2 = instant.substring(2, 4); // 个位、十分位（BCD码）
        String byte3 = instant.substring(4, 6); // 百位、十位（BCD码）
        String byte4 = instant.substring(6, 8); // 万位、千位（BCD码）
        String byte5Hex = instant.substring(8, 10); // BYTE5（十六进制，含符号位、单位、十万位）

        // 计算数值部分
        // 千分位+百分位：byte1为2位BCD码，对应0.00x
        double decimal = Integer.parseInt(byte2.substring(0, 1)) * 100 + Integer.parseInt(byte1) / 1000.0;
        // 个位 + 十分位：byte2低1位为个位，高1位为十分位
        int unitPart = Integer.parseInt(byte2.substring(1));
        // 百位 + 十位×10：byte3低1位为百位，高1位为十位
        int hundredPart = Integer.parseInt(byte3.substring(1)) * 100 + Integer.parseInt(byte3.substring(0, 1)) * 10;
        // 万位 + 千位×1000：byte4低1位为万位，高1位为千位
        int tenThousandPart = Integer.parseInt(byte4.substring(1)) * 10000 + Integer.parseInt(byte4.substring(0, 1)) * 1000;
        // 十万位：BYTE5的D3-D0为BCD码（低4位），对应×100000
        int byte5Value = Integer.parseInt(byte5Hex, 16); // 十六进制转整数
        int hundredThousand = (byte5Value & 0x0F); // 提取D3-D0（低4位），即BCD码十万位
        int hundredThousandPart = hundredThousand * 100000;

        double value = hundredThousandPart + tenThousandPart + hundredPart + unitPart + decimal;

        // 解析单位：BYTE5的D5-D4位（二进制）
        String byte5Binary = String.format("%8s", Integer.toBinaryString(byte5Value)).replace(' ', '0'); // 转为8位二进制
        String unitBits = byte5Binary.substring(2, 4); // 提取D5-D4位
        String unit;
        if ("00".equals(unitBits)) {
            unit = "m³/s";
        } else if ("11".equals(unitBits)) {
            unit = "m³/h";
        } else {
            unit = "m³/s（单位字段异常，D5-D4=" + unitBits + "）";
        }

//        return String.format("%.3f %s", value, unit);
        return String.format("%.3f", value);
    }
}