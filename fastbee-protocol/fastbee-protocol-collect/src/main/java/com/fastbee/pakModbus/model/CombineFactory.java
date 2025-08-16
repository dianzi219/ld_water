package com.fastbee.pakModbus.model;


import com.fastbee.protocol.util.ByteToHexUtil;
import com.fastbee.protocol.util.IntegerToByteUtil;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * bit位数据组装
 *
 * @author gsb
 * @date 2022/5/24 11:26
 */

public class CombineFactory {

    /**
     * 是否是位值修改的物模型 0-单值single_value 1-多选multiple_bit 2-单选single_bit 3-多值multiple_value,4-单选值 single_select_value
     *
     * @param type     MCU数据类型
     * @param map      position:value
     * @param oldValue 最新记录的数据
     * @return 结果
     */
    public static byte[] toBytes(int type, Map<Integer, Integer> map, Object oldValue) {
        switch (type) {
            case 1:
                return toOldBitBytes(map, oldValue);
            case 2:
                return toBitBytes(map);
            case 3:
                return toByte2Bytes(map);
            default:
                return toValueBytes(map);
        }
    }

    /**
     * 1-多选multiple_bit
     *
     * @param map      数据map
     * @param oldValue 原始数据值
     * @return 结果
     */
    private static byte[] toOldBitBytes(Map<Integer, Integer> map, Object oldValue) {
        oldValue = oldValue == null ? 0 : oldValue;
        String strV = String.join("", oldValue.toString().split(","));
        int data = IntegerToByteUtil.binaryToInt(strV);
        int result = IntegerToByteUtil.bitOperationBatch(data, map);
        return IntegerToByteUtil.intToByteArr(result, 2);
    }

    /**
     * 多值multiple_value
     *
     * @param map 数据
     * @return 结果
     */
    private static byte[] toByte2Bytes(Map<Integer, Integer> map) {
        byte[] result = new byte[2];
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() ==0){
                result[1] = (byte)entry.getValue().intValue();
            }
            if (entry.getKey() ==1){
                result[0] = (byte)entry.getValue().intValue();
            }
        }
        return result;
    }

    /**
     * 2-单选single_bit
     *
     * @param map 数据值
     * @return 结果
     */
    private static byte[] toBitBytes(Map<Integer, Integer> map) {
        if (!CollectionUtils.isEmpty(map)) {
            int result = IntegerToByteUtil.bitOperationBatch(0, map);
            return IntegerToByteUtil.intToByteArr(result, 2);
        }
        return null;
    }

    /**
     * 0-单值single_value
     *
     * @param map 数据
     * @return 结果
     */
    private static byte[] toValueBytes(Map<Integer, Integer> map) {
        if (!CollectionUtils.isEmpty(map) && map.size() == 1) {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                return IntegerToByteUtil.intToBytes2(entry.getValue());
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0,0);
        map.put(1,1);
        map.put(2,0);
        //map.put(1,15); 0000001100000110
        byte[] bytes = toOldBitBytes(map,"0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1");
        byte[] bytes1 = toBitBytes(map);
        String string = ByteToHexUtil.bytesToHexString(bytes1);
        System.out.println(string);

    }


}
