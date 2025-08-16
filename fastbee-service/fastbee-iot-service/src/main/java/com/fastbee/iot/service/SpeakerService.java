package com.fastbee.iot.service;


import java.io.IOException;
import java.util.List;

/**
 * 其他模块对接音箱服务类
 * 由于循环依赖，所以暂时写在这
 * @author fastb
 * @date 2023-12-07 9:47
 */
public interface SpeakerService {

    /**
     * 上报物模型值给小度
     * @param serialNumber 设备
     * @param identifierList 物模型标识
     * @throws IOException
     */
    void reportDuerosAttribute(String serialNumber, List<String> identifierList) throws IOException;

}
