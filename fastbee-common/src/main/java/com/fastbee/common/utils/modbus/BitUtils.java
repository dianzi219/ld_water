package com.fastbee.common.utils.modbus;

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
        i += 1;
        return num >> (i-1) & 1;
    }

    /**
     * 判断hex数据，某位的值
     * @param hex
     * @param i
     * @return
     */
    public static int deterHex(String hex,int i){
        return deter(Integer.parseInt(hex,16),i);
    }

    public static void main(String[] args) {
        int deter = deter(7, 0);
        int deterHex = deterHex("10", 4);
        System.out.println(deter);
        System.out.println(deterHex);
    }

}
