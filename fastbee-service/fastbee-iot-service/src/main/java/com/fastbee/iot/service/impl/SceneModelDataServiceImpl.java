package com.fastbee.iot.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fastbee.common.constant.HttpStatus;
import com.fastbee.common.core.page.TableDataExtendInfo;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.bean.BeanUtils;
import com.fastbee.common.utils.date.LocalDateTimeUtils;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.mapper.*;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.ThingsModels.ValueItem;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.model.scenemodel.SceneModelDataDTO;
import com.fastbee.iot.model.scenemodel.SceneModelTagCacheVO;
import com.fastbee.iot.service.IDeviceJobService;
import com.fastbee.iot.cache.ITSLValueCache;
import com.fastbee.iot.cache.SceneModelTagCache;
import com.fastbee.iot.service.IThingsModelService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import com.fastbee.iot.service.ISceneModelDataService;

import javax.annotation.Resource;

import static com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum.*;
import static com.fastbee.common.utils.PageUtils.startPage;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Service
public class SceneModelDataServiceImpl implements ISceneModelDataService
{
    @Resource
    private SceneModelDataMapper sceneModelDataMapper;
    @Resource
    private SceneModelTagMapper sceneModelTagMapper;
    @Resource
    private SceneModelDeviceMapper sceneModelDeviceMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private ITSLValueCache itslValueCache;
    @Resource
    private SceneModelTagCache sceneModelTagCache;
    @Resource
    private IDeviceJobService jobService;
    @Resource
    private IThingsModelService thingsModelService;

    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public SceneModelData selectSceneModelDataById(Long id)
    {
        return sceneModelDataMapper.selectSceneModelDataById(id);
    }

    /**
     * 查询场景变量列表
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<SceneModelDataDTO> selectSceneModelDataDTOList(SceneModelData sceneModelData)
    {
        List<SceneModelDataDTO> sceneModelDataDTOList = sceneModelDataMapper.selectSceneModelDataDTOList(sceneModelData);
        if (CollectionUtils.isEmpty(sceneModelDataDTOList)) {
            return new ArrayList<>();
        }
        // 取设备物模型value值
        List<SceneModelDataDTO> dtoList = sceneModelDataDTOList.stream().filter(s -> StringUtils.isNotEmpty(s.getSerialNumber())).collect(Collectors.toList());
        Map<String, List<ThingsModelDTO>> thingsModelMap = new HashMap<>(5);
        for (SceneModelDataDTO sceneModelDataDTO : dtoList) {
            List<ThingsModelDTO> thingsModelDtoList = thingsModelService.getCacheAndHandleArrayByProductId(sceneModelDataDTO.getProductId(), sceneModelDataDTO.getSerialNumber());
            if (CollectionUtils.isNotEmpty(thingsModelDtoList)) {
                thingsModelMap.put(sceneModelDataDTO.getSerialNumber(), thingsModelDtoList);
            }
        }
        // 取场景录入、运算型变量
        List<SceneModelDataDTO> modelDataList = sceneModelDataDTOList.stream()
                .filter(s -> INPUT_VARIABLE.getType().equals(s.getVariableType()) || OPERATION_VARIABLE.getType().equals(s.getVariableType())).collect(Collectors.toList());
        Map<Long, SceneModelTag> sceneModelTagMap = new HashMap<>(2);
        if (CollectionUtils.isNotEmpty(modelDataList)) {
            List<SceneModelTag> sceneModelTagList = sceneModelTagMapper.listSceneModelDataByIds(modelDataList.stream().map(SceneModelDataDTO::getDatasourceId).collect(Collectors.toList()));
            sceneModelTagMap = sceneModelTagList.stream().collect(Collectors.toMap(SceneModelTag::getId, Function.identity()));
        }
        for (SceneModelDataDTO sceneModelDataDTO : sceneModelDataDTOList) {
            if (StringUtils.isNotEmpty(sceneModelDataDTO.getSerialNumber())) {
                List<ThingsModelDTO> thingsModelDTOList = thingsModelMap.get(sceneModelDataDTO.getSerialNumber());
                String identifier = sceneModelDataDTO.getIdentifier();
                ThingsModelDTO thingsModelDTO = thingsModelDTOList.stream().filter(t -> identifier.equals(t.getIdentifier())).findFirst().orElse(null);
                if (null != thingsModelDTO) {
                    sceneModelDataDTO.setValue(thingsModelDTO.getValue());
                    sceneModelDataDTO.setType(thingsModelDTO.getType());
                    sceneModelDataDTO.setUpdateTime(thingsModelDTO.getTs());
                    sceneModelDataDTO.setUnit(thingsModelDTO.getUnit());
                    sceneModelDataDTO.setIsReadonly(thingsModelDTO.getIsReadonly());
                }
            }
            if (!THINGS_MODEL.getType().equals(sceneModelDataDTO.getVariableType())) {
                SceneModelTag sceneModelTag = sceneModelTagMap.get(sceneModelDataDTO.getDatasourceId());
                if (null != sceneModelTag) {
                    sceneModelDataDTO.setUnit(sceneModelTag.getUnit());
                    sceneModelDataDTO.setIsReadonly(sceneModelTag.getIsReadonly());
                }
                SceneModelTagCacheVO sceneModelTagCacheVO = sceneModelTagCache.getSceneModelTagValue(sceneModelData.getSceneModelId(), sceneModelDataDTO.getDatasourceId());
                if (null != sceneModelTagCacheVO) {
                    sceneModelDataDTO.setValue(sceneModelTagCacheVO.getValue());
                    sceneModelDataDTO.setUpdateTime(sceneModelTagCacheVO.getTs());
                }
            }
        }
        return sceneModelDataDTOList;
    }

    @Override
    public TableDataExtendInfo listByType(SceneModelData sceneModelData) {
        // 查询全部启用
        SceneModelDevice sceneModelDevice;
        if (THINGS_MODEL.getType().equals(sceneModelData.getVariableType())) {
            sceneModelDevice = sceneModelDeviceMapper.selectSceneModelDeviceById(sceneModelData.getSceneModelDeviceId());
        } else {
            sceneModelDevice = sceneModelDeviceMapper.selectOneNoDeviceBySceneModelId(sceneModelData.getSceneModelId(), sceneModelData.getVariableType());
        }
        TableDataExtendInfo rspData = new TableDataExtendInfo();
        startPage();
        List<SceneModelDataDTO> sceneModelDataDTOList = sceneModelDataMapper.selectSceneModelDataDTOList(sceneModelData);
        rspData.setAllEnable(null != sceneModelDevice ? sceneModelDevice.getAllEnable() : 0);
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("query.success");
        rspData.setTotal(new PageInfo(sceneModelDataDTOList).getTotal());
        rspData.setRows(sceneModelDataDTOList);
        return rspData;
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertSceneModelData(SceneModelData sceneModelData)
    {
        return sceneModelDataMapper.insertSceneModelData(sceneModelData);
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateSceneModelData(SceneModelData sceneModelData)
    {
        return sceneModelDataMapper.updateSceneModelData(sceneModelData);
    }

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelDataByIds(Long[] ids)
    {
        return sceneModelDataMapper.deleteSceneModelDataByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     *
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelDataById(Long id)
    {
        return sceneModelDataMapper.deleteSceneModelDataById(id);
    }

    @Override
    public int editEnable(SceneModelData sceneModelData) {
        Long sceneModelTagId = sceneModelData.getId();
        if (!THINGS_MODEL.getType().equals(sceneModelData.getVariableType())) {
            SceneModelData sceneModelData1 = sceneModelDataMapper.selectNoDeviceBySourceIdAndVariableType(sceneModelData.getId(), sceneModelData.getVariableType());
            sceneModelData.setId(sceneModelData1.getId());
            sceneModelData.setSceneModelDeviceId(sceneModelData1.getSceneModelDeviceId());
        }
        SceneModelData updateSceneModelData = new SceneModelData();
        updateSceneModelData.setId(sceneModelData.getId());
        updateSceneModelData.setEnable(sceneModelData.getEnable());
        int i = sceneModelDataMapper.updateSceneModelData(updateSceneModelData);
        if (i > 0) {
            SceneModelDevice sceneModelDevice = new SceneModelDevice();
            sceneModelDevice.setId(sceneModelData.getSceneModelDeviceId());
            if (0 == sceneModelData.getEnable()) {
                sceneModelDevice.setAllEnable(0);
                sceneModelDeviceMapper.updateSceneModelDevice(sceneModelDevice);
            } else {
                int noEnable = sceneModelDataMapper.countNoEnableBySceneModelDeviceId(sceneModelData.getSceneModelDeviceId());
                if (noEnable <= 0) {
                    sceneModelDevice.setAllEnable(1);
                    sceneModelDeviceMapper.updateSceneModelDevice(sceneModelDevice);
                }
            }
            // 更新定时任务
            if (OPERATION_VARIABLE.getType().equals(sceneModelData.getVariableType())) {
                List<DeviceJob> deviceJobList = jobService.selectListByJobTypeAndDatasourceIds(new Long[]{sceneModelTagId}, 4);
                try {
                    for (DeviceJob job : deviceJobList) {
                        DeviceJob deviceJob = new DeviceJob();
                        deviceJob.setJobId(job.getJobId());
                        deviceJob.setJobGroup(job.getJobGroup());
                        deviceJob.setStatus(1 == sceneModelData.getEnable() ? "0" : "1");
                        jobService.changeStatus(deviceJob);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return i;
    }

    @Override
    public List<SceneModelData> selectSceneModelDataListByIds(List<Long> ids) {
        return sceneModelDataMapper.selectSceneModelDataListByIds(ids);
    }
}
