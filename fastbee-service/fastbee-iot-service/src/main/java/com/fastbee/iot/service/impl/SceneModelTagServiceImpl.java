package com.fastbee.iot.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.constant.MagicValueConstants;
import com.fastbee.common.constant.SceneModelConstants;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.DeviceLogTypeEnum;
import com.fastbee.common.enums.scenemodel.SceneModelTagOpreationEnum;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.exception.job.TaskException;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.date.LocalDateTimeUtils;
import com.fastbee.iot.cache.SceneModelTagCache;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.mapper.SceneModelDataMapper;
import com.fastbee.iot.mapper.SceneModelDeviceMapper;
import com.fastbee.iot.mapper.SceneModelTagMapper;
import com.fastbee.iot.mapper.SceneTagPointsMapper;
import com.fastbee.iot.model.scenemodel.SceneDeviceThingsModelVO;
import com.fastbee.iot.model.scenemodel.SceneModelDeviceVO;
import com.fastbee.iot.model.scenemodel.SceneModelTagCacheVO;
import com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO;
import com.fastbee.iot.service.IDeviceJobService;
import com.fastbee.iot.service.ISceneModelTagService;
import com.fastbee.iot.service.IThingsModelService;
import com.fastbee.iot.tdengine.service.ILogService;
import com.fastbee.iot.util.JobCronUtils;
import com.fastbee.quartz.util.CronUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 场景录入型变量Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Slf4j
@Service
public class SceneModelTagServiceImpl implements ISceneModelTagService
{
    @Resource
    private SceneModelTagMapper sceneModelTagMapper;

    @Resource
    private SceneModelDataMapper sceneModelDataMapper;

    @Resource
    private SceneTagPointsMapper sceneTagPointsMapper;

    @Resource
    private SceneModelDeviceMapper sceneModelDeviceMapper;

    @Resource
    private SceneModelTagCache sceneModelTagCache;

    @Resource
    private ILogService logService;

    @Resource
    private IDeviceJobService deviceJobService;

    @Resource
    private IThingsModelService thingsModelService;

    @Resource
    private RedisCache redisCache;

    /**
     * 计算表达式变量英文大写字母
     */
    private final static String PATTERN_ENGLISH_LETTER = "[a-zA-Z]";

    /**
     * 查询场景录入型变量
     *
     * @param id 场景录入型变量主键
     * @return 场景录入型变量
     */
    @Override
    public SceneModelTag selectSceneModelTagById(Long id)
    {
        SceneModelTag sceneModelTag = sceneModelTagMapper.selectSceneModelTagById(id);
        if (null == sceneModelTag) {
            return null;
        }
        List<SceneTagPoints> sceneTagPointsList = sceneTagPointsMapper.selectListByTagId(sceneModelTag.getId());
        if (CollectionUtils.isNotEmpty(sceneTagPointsList)) {
            List<Long> dataSourceIdList = sceneTagPointsList.stream().map(SceneTagPoints::getSceneModelDataId).collect(Collectors.toList());
            List<SceneModelDeviceVO> sceneModelDeviceVOList = sceneModelDataMapper.selectSceneModelDeviceByDataIdList(dataSourceIdList);
            Map<Long, SceneModelDeviceVO> sceneModelDeviceMap = sceneModelDeviceVOList.stream().collect(Collectors.toMap(SceneModelDeviceVO::getSceneModelDataId, Function.identity()));
            for (SceneTagPoints sceneTagPoints : sceneTagPointsList) {
                SceneModelDeviceVO sceneModelDeviceVO = sceneModelDeviceMap.get(sceneTagPoints.getSceneModelDataId());
                if (null != sceneModelDeviceVO) {
                    sceneTagPoints.setSceneModelDeviceId(sceneModelDeviceVO.getSceneModelDeviceId());
                    sceneTagPoints.setSceneModelDeviceName(sceneModelDeviceVO.getSceneModelDeviceName());
                }
            }
            sceneModelTag.setTagPointsList(sceneTagPointsList);
        }
        return sceneModelTag;
    }

    /**
     * 查询场景录入型变量列表
     *
     * @param sceneModelTag 场景录入型变量
     * @return 场景录入型变量
     */
    @Override
    public List<SceneModelTag> selectSceneModelTagList(SceneModelTag sceneModelTag)
    {
        List<SceneModelTag> sceneModelTagList = sceneModelTagMapper.selectSceneModelTagList(sceneModelTag);
        if (CollectionUtils.isEmpty(sceneModelTagList)) {
            return new ArrayList<>();
        }
        SceneModelData sceneModelData = new SceneModelData();
        sceneModelData.setSceneModelId(sceneModelTag.getSceneModelId());
        sceneModelData.setVariableType(sceneModelTag.getVariableType());
        List<SceneModelData> sceneModelDataList = sceneModelDataMapper.selectSceneModelDataList(sceneModelData);
        Map<Long, SceneModelData> map = sceneModelDataList.stream().collect(Collectors.toMap(SceneModelData::getDatasourceId, Function.identity()));
        for (SceneModelTag modelTag : sceneModelTagList) {
            SceneModelData sceneModelData1 = map.get(modelTag.getId());
            if (null != sceneModelData1) {
                modelTag.setEnable(sceneModelData1.getEnable());
            }
        }
        return sceneModelTagList;
    }

    /**
     * 新增场景录入型变量
     *
     * @param sceneModelTag 场景录入型变量
     * @return 结果
     */
    @Override
    public int insertSceneModelTag(SceneModelTag sceneModelTag)
    {
        if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneModelTag.getVariableType())) {
            String msg =  this.checkAliasFormule(sceneModelTag);
            if (StringUtils.isNotEmpty(msg)) {
                throw new ServiceException(msg);
            }
        }
        if (!SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneModelTag.getVariableType())) {
            SceneModelDevice sceneModelDevice = sceneModelDeviceMapper.selectOneNoDeviceBySceneModelId(sceneModelTag.getSceneModelId(), sceneModelTag.getVariableType());
            sceneModelTag.setSceneModelDeviceId(sceneModelDevice.getId());
        }
        SysUser user = getLoginUser().getUser();
        sceneModelTag.setCreateBy(user.getCreateBy());
        sceneModelTag.setUpdateBy(user.getUpdateBy());
        int result = sceneModelTagMapper.insertSceneModelTag(sceneModelTag);
        if (result <= 0) {
            return 0;
        }
        SceneModelData sceneModelData = new SceneModelData();
        sceneModelData.setSceneModelId(sceneModelTag.getSceneModelId());
        sceneModelData.setSceneModelDeviceId(sceneModelTag.getSceneModelDeviceId());
        sceneModelData.setVariableType(sceneModelTag.getVariableType());
        sceneModelData.setEnable(1);
        sceneModelData.setDatasourceId(sceneModelTag.getId());
        sceneModelData.setIdentifier(sceneModelTag.getId().toString());
        sceneModelData.setSourceName(sceneModelTag.getName());
        sceneModelDataMapper.insertSceneModelData(sceneModelData);
        for (SceneTagPoints sceneTagPoints : sceneModelTag.getTagPointsList()) {
            sceneTagPoints.setTagId(sceneModelTag.getId());
            sceneTagPointsMapper.insertSceneTagPoints(sceneTagPoints);
        }
        // 添加定时任务
        if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneModelTag.getVariableType())) {
            SceneModelTagCycleVO sceneModelTagCycleVO = JobCronUtils.handleCronCycle(sceneModelTag.getCycleType(), sceneModelTag.getCycle());
            sceneModelTagCycleVO.setStatus(1);
            this.createSceneTagTask(sceneModelTag.getId(), sceneModelTagCycleVO);
        }
        return result;
    }

    /**
     * 校验计算公式
     * @param sceneModelTag 变量实体类
     * @return java.lang.String
     */
    @Override
    public String checkAliasFormule(SceneModelTag sceneModelTag) {
        if (StringUtils.isEmpty(sceneModelTag.getAliasFormule()) && CollectionUtils.isNotEmpty(sceneModelTag.getTagPointsList())) {
            return "计算公式不能为空！";
        }
        if (StringUtils.isNotEmpty(sceneModelTag.getAliasFormule()) && CollectionUtils.isEmpty(sceneModelTag.getTagPointsList())) {
            return "变量不能为空！";
        }
        String aliasFormule = sceneModelTag.getAliasFormule();
        Pattern pattern = Pattern.compile(PATTERN_ENGLISH_LETTER);
        Matcher matcher = pattern.matcher(aliasFormule);
        List<String> aliasList = new ArrayList<>();
        while (matcher.find()) {
            if (!aliasList.contains(matcher.group())) {
                aliasList.add(matcher.group());
            }
        }
        List<SceneTagPoints> tagPointsList = sceneModelTag.getTagPointsList();
        List<String> aliasList1 = tagPointsList.stream().map(SceneTagPoints::getAlias).collect(Collectors.toList());
        Collections.sort(aliasList);
        Collections.sort(aliasList1);
        if (!aliasList.equals(aliasList1)) {
            return "计算公式和变量个数不一致，请检查后重试";
        }
        return "";
    }

    /**
     * 修改场景录入型变量
     *
     * @param sceneModelTag 场景录入型变量
     * @return 结果
     */
    @Override
    public int updateSceneModelTag(SceneModelTag sceneModelTag)
    {
        SceneModelTag checkName = sceneModelTagMapper.checkName(sceneModelTag);
        if (null != checkName) {
            throw new ServiceException("变量名称已存在，请修改后重试！");
        }
        if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneModelTag.getVariableType())) {
            String msg =  this.checkAliasFormule(sceneModelTag);
            if (StringUtils.isNotEmpty(msg)) {
                throw new ServiceException(msg);
            }
        }
        SysUser user = getLoginUser().getUser();
        sceneModelTag.setUpdateBy(user.getUserName());
        int i = sceneModelTagMapper.updateSceneModelTag(sceneModelTag);
        if (i > 0) {
            sceneTagPointsMapper.deleteByTagIds(new Long[]{sceneModelTag.getId()});
            for (SceneTagPoints sceneTagPoints : sceneModelTag.getTagPointsList()) {
                sceneTagPoints.setTagId(sceneModelTag.getId());
                sceneTagPointsMapper.insertSceneTagPoints(sceneTagPoints);
            }
            // 添加定时任务
            if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneModelTag.getVariableType())) {
                // 删除定时任务
                try {
                    deviceJobService.deleteJobByJobTypeAndDatasourceIds(new Long[]{sceneModelTag.getId()}, 4);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
                SceneModelTagCycleVO sceneModelTagCycleVO = JobCronUtils.handleCronCycle(sceneModelTag.getCycleType(), sceneModelTag.getCycle());
                sceneModelTagCycleVO.setStatus(sceneModelTag.getEnable());
                this.createSceneTagTask(sceneModelTag.getId(), sceneModelTagCycleVO);
            }
            // 同步变量
            SceneModelData updateSceneModelData = new SceneModelData();
            updateSceneModelData.setSceneModelId(sceneModelTag.getSceneModelId());
            updateSceneModelData.setVariableType(sceneModelTag.getVariableType());
            updateSceneModelData.setDatasourceId(sceneModelTag.getId());
            updateSceneModelData.setSourceName(sceneModelTag.getName());
            sceneModelDataMapper.updateSceneModelDataByDatasourceId(updateSceneModelData);
        }
        return i;
    }

    /**
     * 批量删除场景录入型变量
     *
     * @param ids 需要删除的场景录入型变量主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelTagByIds(Long[] ids)
    {
        SceneModelTag sceneModelTag = sceneModelTagMapper.selectSceneModelTagById(ids[0]);
        int i = sceneModelTagMapper.deleteSceneModelTagByIds(ids);
        if (i > 0) {
            sceneModelDataMapper.deleteBySourceIds(ids);
            sceneTagPointsMapper.deleteByTagIds(ids);
            // 批量删除定时任务
            try {
                deviceJobService.deleteJobByJobTypeAndDatasourceIds(ids, 4);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            // 删除缓存
            if (null != sceneModelTag && null != sceneModelTag.getSceneModelId()) {
                String key = RedisKeyBuilder.buildSceneModelTagCacheKey(sceneModelTag.getSceneModelId());
                for (Long id : ids) {
                    redisCache.deleteCacheMapValue(key, id.toString());
                }
            }

        }
        return i;
    }

    /**
     * 删除场景录入型变量信息
     *
     * @param id 场景录入型变量主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelTagById(Long id)
    {
        return sceneModelTagMapper.deleteSceneModelTagById(id);
    }

    /**
     * 获取物模型的值
     * @param sceneTagPoints 变量
     * @return java.lang.String
     */
    @Override
    public String getSceneModelDataValue(SceneTagPoints sceneTagPoints, SceneModelTagCycleVO sceneModelTagCycleVO) {
        Integer operation = sceneTagPoints.getOperation();
        String value = " ";
        SceneDeviceThingsModelVO sceneDeviceThingsModelVO = new SceneDeviceThingsModelVO();
        SceneModelData sceneModelData = new SceneModelData();
        if (SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneTagPoints.getVariableType())) {
            sceneDeviceThingsModelVO = sceneModelDataMapper.selectDeviceThingsModelById(sceneTagPoints.getSceneModelDataId());
        } else {
            // 运算型
            sceneModelData = sceneModelDataMapper.selectSceneModelDataById(sceneTagPoints.getSceneModelDataId());
        }
        if (!SceneModelTagOpreationEnum.ORIGINAL_VALUE.getCode().equals(operation)) {
            DeviceLog deviceLog = new DeviceLog();
            if (SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneTagPoints.getVariableType())) {
                deviceLog.setLogType(DeviceLogTypeEnum.ATTRIBUTE_REPORT.getType());
                deviceLog.setIdentity(sceneDeviceThingsModelVO.getIdentifier());
                deviceLog.setSerialNumber(sceneDeviceThingsModelVO.getSerialNumber());
            } else {
                deviceLog.setLogType(DeviceLogTypeEnum.SCENE_VARIABLE_REPORT.getType());
                deviceLog.setIdentity(null != sceneModelData ? sceneModelData.getDatasourceId().toString() : "");
            }
            deviceLog.setBeginTime(LocalDateTimeUtils.localDateTimeToStr(sceneModelTagCycleVO.getBeginTime(), LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS));
            deviceLog.setEndTime(LocalDateTimeUtils.localDateTimeToStr(sceneModelTagCycleVO.getEndTime(), LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS));
            deviceLog.setOperation(operation);
            List<String> valueList = logService.selectStatsValue(deviceLog);
            if (CollectionUtils.isNotEmpty(valueList)) {
                value = this.handleValueByOperation(valueList, operation);
            }
        } else {
            if (SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneTagPoints.getVariableType())) {
                if (null != sceneDeviceThingsModelVO) {
                    value = thingsModelService.getCacheIdentifierValue(sceneDeviceThingsModelVO.getProductId(), sceneDeviceThingsModelVO.getSerialNumber(), sceneDeviceThingsModelVO.getIdentifier());
                }
            } else if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneTagPoints.getVariableType())) {
                SceneModelTagCacheVO sceneModelTagCacheVO = sceneModelTagCache.getSceneModelTagValue(sceneModelData.getSceneModelId(), sceneModelData.getDatasourceId());
                value = null != sceneModelTagCacheVO ? sceneModelTagCacheVO.getValue() : "";
            } else {
                value = sceneModelDataMapper.selectInputTagDefaultValueById(sceneTagPoints.getSceneModelDataId());
            }
        }
        return value;
    }

    /**
     * 统计方式计算值
     * @param valueList 值集合
     * @param: operation 统计方式
     * @return java.lang.String
     */
    private String handleValueByOperation(List<String> valueList, Integer operation) {
        String resultValue = "";
        List<BigDecimal> numberList = valueList.stream()
                .map(BigDecimal::new)
                .collect(Collectors.toList());
        SceneModelTagOpreationEnum opreationEnum = SceneModelTagOpreationEnum.getByCode(operation);
        switch (Objects.requireNonNull(opreationEnum)) {
            case CUMULATIVE:
                BigDecimal sum = numberList.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                resultValue = String.valueOf(sum);
                break;
            case AVERAGE_VALUE:
                BigDecimal sum1 = numberList.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal average = sum1.divide(new BigDecimal(numberList.size()), 2, RoundingMode.HALF_UP);
                resultValue = String.valueOf(average);
                break;
            case MAX_VALUE:
                BigDecimal max = numberList.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                resultValue = String.valueOf(max);
                break;
            case MIN_VALUE:
                BigDecimal min = numberList.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                resultValue = String.valueOf(min);
                break;
            default:
                break;
        }
        return resultValue;
    }


    /**
     * 处理时间周期 开始、结束时间
     * @param cycleType 时间周期类型
     * @param: cycle 表达式
     * @return com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO
     */
    @Override
    public SceneModelTagCycleVO handleTimeCycle(Integer cycleType, String cycle, LocalDateTime executeTime) {
        LocalDateTime beginTime = null;
        LocalDateTime endTime = executeTime.withSecond(0);
        List<SceneModelTagCycleVO> list = JSON.parseArray(cycle, SceneModelTagCycleVO.class);
        SceneModelTagCycleVO sceneModelTagCycleVO = list.get(0);
        switch (cycleType) {
            // 周期循环
            case 1:
                // 几分钟运算一次
                if (null != sceneModelTagCycleVO.getInterval()) {
                    int min = sceneModelTagCycleVO.getInterval() / MagicValueConstants.VALUE_60;
                    beginTime = endTime.minusMinutes(min);
                }
                // 每小时运算一次
                if (null != sceneModelTagCycleVO.getType()
                        && SceneModelConstants.CYCLE_HOUR.equals(sceneModelTagCycleVO.getType())) {
                    beginTime = endTime.minusHours(1);
                }
                // 每天几时运算一次
                if (SceneModelConstants.CYCLE_DAY.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getTime()) {
                    beginTime = endTime.minusDays(1);
                }
                // 每周周几几时运算一次
                if (SceneModelConstants.CYCLE_WEEK.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getWeek() && null != sceneModelTagCycleVO.getTime()) {
                    beginTime = endTime.minusWeeks(1);
                }
                // 每月第几日几时运算一次
                if (SceneModelConstants.CYCLE_MONTH.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getDay() && null != sceneModelTagCycleVO.getTime()) {
                    beginTime = endTime.minusMonths(1);
                }
                break;
            // 自定义时间段，取开始时间
            case 2:
                // 每日几时
                if (SceneModelConstants.CYCLE_DAY.equals(sceneModelTagCycleVO.getType())) {
                    if (SceneModelConstants.CYCLE_TO_TYPE_NOW_DAY.equals(sceneModelTagCycleVO.getToType())) {
                        beginTime = endTime.withHour(sceneModelTagCycleVO.getTime());
                    } else if (SceneModelConstants.CYCLE_TO_TYPE_SECOND_DAY.equals(sceneModelTagCycleVO.getToType())) {
                        beginTime = endTime.minusDays(1).withHour(sceneModelTagCycleVO.getTime());
                    }
                }
                // 每周周几运算一次
                if (SceneModelConstants.CYCLE_WEEK.equals(sceneModelTagCycleVO.getType())) {
                    int weekDay = sceneModelTagCycleVO.getToWeek() - sceneModelTagCycleVO.getWeek();
                    beginTime = endTime.minusDays(weekDay).withHour(sceneModelTagCycleVO.getTime());
                }
                // 每月几号几时运算一次
                if (SceneModelConstants.CYCLE_MONTH.equals(sceneModelTagCycleVO.getType())
                        && null != sceneModelTagCycleVO.getToDay() && null != sceneModelTagCycleVO.getToTime()) {
                    beginTime = endTime.withDayOfMonth(sceneModelTagCycleVO.getDay()).withHour(sceneModelTagCycleVO.getTime());
                }
                break;
            default:
                break;
        }
        sceneModelTagCycleVO.setBeginTime(beginTime);
        sceneModelTagCycleVO.setEndTime(endTime);
        return sceneModelTagCycleVO;
    }

    /**
     * 创建场景运算变量定时任务
     *
     * @param sceneModelTagId 运算变量id
     * @param sceneModelTagCycleVO cron
     */
    private void createSceneTagTask(Long sceneModelTagId, SceneModelTagCycleVO sceneModelTagCycleVO) {
        // 创建定时任务
        try {
            if (!CronUtils.isValid(sceneModelTagCycleVO.getCron())) {
                log.error("新增场景运算变量定时任务失败，Cron表达式不正确");
                throw new Exception("新增场景运算变量定时任务失败，Cron表达式不正确");
            }
            DeviceJob deviceJob = new DeviceJob();
            deviceJob.setJobName("场景运算变量定时触发");
            deviceJob.setJobType(4);
            deviceJob.setJobGroup("SCENE");
            deviceJob.setConcurrent("1");
            deviceJob.setMisfirePolicy("2");
            deviceJob.setStatus(1 == sceneModelTagCycleVO.getStatus() ? "0" : "1");
            deviceJob.setCronExpression(sceneModelTagCycleVO.getCron());
            deviceJob.setIsAdvance(1);
            deviceJob.setDeviceName("场景运算变量定时触发");
            deviceJob.setDatasourceId(sceneModelTagId);
            deviceJobService.insertJob(deviceJob);
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (TaskException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
