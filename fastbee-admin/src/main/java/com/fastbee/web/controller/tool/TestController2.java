package com.fastbee.web.controller.tool;

import com.alibaba.fastjson2.JSON;
import com.fastbee.iot.domain.DeviceJob;
import com.fastbee.iot.domain.DeviceLog;
import com.fastbee.iot.mapper.DeviceJobMapper;
import com.fastbee.iot.mapper.DeviceLogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fastbee.common.annotation.Anonymous;
import com.fastbee.common.core.domain.ImportExcelVO;
import com.fastbee.common.core.domain.OutputExcelVO;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.AlertLog;
import com.fastbee.iot.mapper.AlertLogMapper;
import com.fastbee.iot.mapper.DeviceMapper;
import com.fastbee.iot.model.DeviceRelateAlertLogVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 测试类
 * @author fastb
 * @date 2023-09-13 11:42
 */
@Anonymous
@RestController
@RequestMapping("/test2")
public class TestController2 {

    @Resource
    private AlertLogMapper alertLogMapper;
    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private DeviceJobMapper deviceJobMapper;
    @Resource
    private DeviceLogMapper deviceLogMapper;

    @GetMapping("/add")
    public void add()
    {
        Set<String> deviceNumbers = new HashSet<>();
        deviceNumbers.add("D1PGLPG58K88");
        deviceNumbers.add("D1F0L7P84D8Z");
        deviceNumbers.add("D1F0L7P84D8Z_2");
        List<DeviceRelateAlertLogVO> deviceRelateAlertLogVOList = deviceMapper.selectDeviceBySerialNumbers(deviceNumbers);
        Map<String, DeviceRelateAlertLogVO> deviceRelateAlertLogVOMap = deviceRelateAlertLogVOList.stream().collect(Collectors.toMap(DeviceRelateAlertLogVO::getSerialNumber, Function.identity()));

        ArrayList<AlertLog> alertLogList = new ArrayList<>();
        for (String deviceNumber : deviceNumbers) {
            AlertLog alertLog = new AlertLog();
            alertLog.setSerialNumber(deviceNumber);
            alertLog.setAlertName("温度告警测试");
            alertLog.setAlertLevel(1L);
            alertLog.setStatus(1);
            alertLog.setProductId(1L);
            alertLog.setDetail("111");
            alertLog.setCreateTime(new Date());
            // 添加设备关联信息
            if (deviceRelateAlertLogVOMap.containsKey(deviceNumber)) {
                DeviceRelateAlertLogVO deviceRelateAlertLogVO = deviceRelateAlertLogVOMap.get(deviceNumber);
                alertLog.setDeviceName(deviceRelateAlertLogVO.getDeviceName());
                alertLog.setUserId(deviceRelateAlertLogVO.getUserId());
            }
            alertLogList.add(alertLog);
        }

        // 批量插入告警日志
        alertLogMapper.insertAlertLogBatch(alertLogList);
    }

    @PostMapping("/handleExcel")
    public String handleExcel(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (null == file) {
            return "导入失败，请先上传文件！";
        }
        ExcelUtil<ImportExcelVO> util = new ExcelUtil<>(ImportExcelVO.class);
        List<ImportExcelVO> importExcelVOList = util.importExcel(file.getInputStream());
        if (CollectionUtils.isEmpty(importExcelVOList)) {
            return "导入失败，模板数据不能为空！";
        }
        System.out.println(importExcelVOList.size());
        List<String> oneCityName = new ArrayList<>();
        List<String> twoCityName = new ArrayList<>();
        List<String> threeCityName = new ArrayList<>();

        List<OutputExcelVO> oneCityList = new ArrayList<>();
        Map<String, List<OutputExcelVO>> twoCityMap = new HashMap<>(2);
        Map<String, List<OutputExcelVO>> threeCityMap = new HashMap<>(2);

        for (ImportExcelVO vo : importExcelVOList) {
            String city = vo.getCity();
            List<String> cityNameList = StringUtils.str2List(city, "/", true, true);
            int size = cityNameList.size();
            if (1 == size) {
                String name = cityNameList.get(0);
                if (!oneCityName.contains(name)) {
                    oneCityName.add(name);
                    OutputExcelVO outputExcelVO = new OutputExcelVO();
                    outputExcelVO.setName(name);
                    outputExcelVO.setCode(vo.getCode());
                    outputExcelVO.setLat(vo.getLat());
                    outputExcelVO.setLon(vo.getLon());
                    oneCityList.add(outputExcelVO);
                    twoCityMap.put(name, new ArrayList<>());
                }
            } else if (2 == size) {
                String oneName = cityNameList.get(0);
                if (!oneCityName.contains(oneName)) {
                    oneCityName.add(oneName);
                    OutputExcelVO outputExcelVO = new OutputExcelVO();
                    outputExcelVO.setName(oneName);
                    oneCityList.add(outputExcelVO);
                    twoCityMap.put(oneName, new ArrayList<>());
                }
                String twoName = cityNameList.get(1);
                OutputExcelVO outputExcelVO = new OutputExcelVO();
                outputExcelVO.setName(twoName);
                outputExcelVO.setCode(vo.getCode());
                outputExcelVO.setLat(vo.getLat());
                outputExcelVO.setLon(vo.getLon());
                if (!twoCityName.contains(twoName)) {
                    twoCityName.add(twoName);
                    List<OutputExcelVO> twoList = twoCityMap.get(oneName);
                    twoList.add(outputExcelVO);
                    twoCityMap.put(oneName, twoList);
                    threeCityMap.put(twoName, new ArrayList<>());
                }
            } else if (3 == size) {
                String oneName = cityNameList.get(0);
                if (!oneCityName.contains(oneName)) {
                    oneCityName.add(oneName);
                    OutputExcelVO outputExcelVO = new OutputExcelVO();
                    outputExcelVO.setName(oneName);
                    oneCityList.add(outputExcelVO);
                    twoCityMap.put(oneName, new ArrayList<>());
                }
                String twoName = cityNameList.get(1);
                if (!twoCityName.contains(twoName)) {
                    twoCityName.add(twoName);
                    OutputExcelVO outputExcelVO = new OutputExcelVO();
                    outputExcelVO.setName(twoName);
                    List<OutputExcelVO> twoList = twoCityMap.get(oneName);
                    twoList.add(outputExcelVO);
                    twoCityMap.put(oneName, twoList);
                    threeCityMap.put(twoName, new ArrayList<>());
                }
                String threeName = cityNameList.get(2);
                if (!threeCityName.contains(threeName)) {
                    threeCityName.add(threeName);
                    OutputExcelVO outputExcelVO = new OutputExcelVO();
                    outputExcelVO.setName(threeName);
                    outputExcelVO.setCode(vo.getCode());
                    outputExcelVO.setLat(vo.getLat());
                    outputExcelVO.setLon(vo.getLon());
                    List<OutputExcelVO> twoList = threeCityMap.get(twoName);
                    twoList.add(outputExcelVO);
                    threeCityMap.put(twoName, twoList);
                }
            }
        }
        int total = 0;
        total = total + oneCityList.size();
        for (OutputExcelVO outputExcelVO : oneCityList) {
            List<OutputExcelVO> voList = twoCityMap.get(outputExcelVO.getName());
            if (CollectionUtils.isNotEmpty(voList)) {
                total = total + voList.size();
                outputExcelVO.setChildren(voList);
                for (OutputExcelVO excelVO : voList) {
                    List<OutputExcelVO> voList1 = threeCityMap.get(excelVO.getName());
                    if (CollectionUtils.isNotEmpty(voList1)) {
                        total = total + voList1.size();
                        excelVO.setChildren(voList1);
                    }
                }
            }
        }
        System.out.println(total);
        return JSON.toJSONString(oneCityList);
    }

    /**
     * 计算场景运算型变量的值
     * @param id
     * @return java.lang.String
     */
    @GetMapping("/getJobList")
    public String getJobList() {
        Long[] ids = {1L, 2L};
        List<DeviceJob> deviceJobList = deviceJobMapper.selectListByJobTypeAndDatasourceIds(ids, 3);
        return JSON.toJSONString(deviceJobList);
    }

    @GetMapping("/getStatsValue")
    public String getStatsValue() {
        DeviceLog deviceLog = new DeviceLog();
        deviceLog.setLogType(1);
        deviceLog.setIdentity("k1");
        deviceLog.setSerialNumber("D10I741UL2P3_1");
        deviceLog.setBeginTime("2024-05-17 00:00:00");
        deviceLog.setEndTime("2024-05-17 23:59:59");
//        deviceLog.setOperation(5);
        List<String> list = deviceLogMapper.selectStatsValue(deviceLog);
        return JSON.toJSONString(list);
    }

}
