package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.mapper.DeviceMapper;
import com.fastbee.iot.mapper.SceneModelDataMapper;
import com.fastbee.iot.mapper.SceneModelDeviceMapper;
import com.fastbee.iot.mapper.ThingsModelMapper;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.service.IDeviceJobService;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.ISceneModelDeviceService;
import com.fastbee.iot.service.IThingsModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum.OPERATION_VARIABLE;
import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 场景管理关联设备Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Service
public class SceneModelDeviceServiceImpl implements ISceneModelDeviceService
{
    @Resource
    private SceneModelDeviceMapper sceneModelDeviceMapper;

    @Resource
    private SceneModelDataMapper sceneModelDataMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private IThingsModelService thingsModelService;

    @Resource
    private IDeviceJobService jobService;


    /**
     * 查询场景管理关联设备
     *
     * @param id 场景管理关联设备主键
     * @return 场景管理关联设备
     */
    @Override
    public SceneModelDevice selectSceneModelDeviceById(Long id)
    {
        return sceneModelDeviceMapper.selectSceneModelDeviceById(id);
    }

    /**
     * 查询场景管理关联设备列表
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 场景管理关联设备
     */
    @Override
    public List<SceneModelDevice> selectSceneModelDeviceList(SceneModelDevice sceneModelDevice)
    {
        return sceneModelDeviceMapper.selectSceneModelDeviceList(sceneModelDevice);
    }

    /**
     * 新增场景管理关联设备
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 结果
     */
    @Override
    public int insertSceneModelDevice(SceneModelDevice sceneModelDevice)
    {
        SysUser user = getLoginUser().getUser();
        if (null != sceneModelDevice.getCusDeviceId()) {
            SceneModelDevice sceneModelDevice1 = new SceneModelDevice();
            sceneModelDevice1.setSceneModelId(sceneModelDevice.getSceneModelId());
            sceneModelDevice1.setCusDeviceId(sceneModelDevice.getCusDeviceId());
            List<SceneModelDevice> sceneModelDeviceList = this.selectSceneModelDeviceList(sceneModelDevice1);
            if (CollectionUtils.isNotEmpty(sceneModelDeviceList)) {
                throw new ServiceException("场景已绑定该设备，请重新选择！");
            }
        }
        sceneModelDevice.setCreateTime(DateUtils.getNowDate());
        sceneModelDevice.setVariableType(SceneModelVariableTypeEnum.THINGS_MODEL.getType());
        sceneModelDevice.setAllEnable(1);
        sceneModelDevice.setSort(1);
        if (StringUtils.isEmpty(sceneModelDevice.getName())) {
            sceneModelDevice.setName(SceneModelVariableTypeEnum.THINGS_MODEL.getName());
        }
        sceneModelDevice.setCreateBy(user.getUserName());
        sceneModelDevice.setUpdateBy(user.getUserName());
        int result = sceneModelDeviceMapper.insertSceneModelDevice(sceneModelDevice);
        if (result <= 0) {
            return 0;
        }
        if (null != sceneModelDevice.getCusDeviceId()) {
            this.insertSceneModelData(sceneModelDevice);
        }
        return result;
    }

    /**
     * 修改场景管理关联设备
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 结果
     */
    @Override
    public int updateSceneModelDevice(SceneModelDevice sceneModelDevice)
    {
        // 校验是否有使用计算公式
        int count = sceneModelDeviceMapper.checkContainAliasFormule(sceneModelDevice.getId());
        if (count > 0) {
            throw new ServiceException("当前设备下存在变量被引用到运算型变量计算公式中，无法修改，请删除引用关系后再执行修改操作！");
        }
        SysUser user = getLoginUser().getUser();
        SceneModelDevice oldSceneModelDevice = this.selectSceneModelDeviceById(sceneModelDevice.getId());
        if (sceneModelDevice.getCusDeviceId().equals(oldSceneModelDevice.getCusDeviceId())) {
            sceneModelDevice.setUpdateTime(DateUtils.getNowDate());
            sceneModelDevice.setUpdateBy(user.getUserName());
            return sceneModelDeviceMapper.updateSceneModelDevice(sceneModelDevice);
        }
        SceneModelDevice sceneModelDevice1 = new SceneModelDevice();
        sceneModelDevice1.setSceneModelId(sceneModelDevice.getSceneModelId());
        sceneModelDevice1.setCusDeviceId(sceneModelDevice.getCusDeviceId());
        List<SceneModelDevice> sceneModelDeviceList = this.selectSceneModelDeviceList(sceneModelDevice1);
        if (CollectionUtils.isNotEmpty(sceneModelDeviceList)) {
            throw new ServiceException("场景已绑定该设备，请重新选择！");
        }
        sceneModelDevice.setUpdateTime(DateUtils.getNowDate());
        int i = sceneModelDeviceMapper.updateSceneModelDevice(sceneModelDevice);
        if (i <= 0) {
            return 0;
        }
        sceneModelDataMapper.deleteBySceneModelDeviceIds(new Long[]{sceneModelDevice.getId()});
        this.insertSceneModelData(sceneModelDevice);
        return i;
    }

    private void insertSceneModelData(SceneModelDevice sceneModelDevice) {
        Device device = deviceMapper.selectDeviceByDeviceId(sceneModelDevice.getCusDeviceId());
        List<ThingsModelDTO> thingsModelList = thingsModelService.changeObjectOrArrayByProductId(device.getProductId());
        List<SceneModelData> sceneModelDataList = new ArrayList<>();
        for (ThingsModelDTO model : thingsModelList) {
            SceneModelData sceneModelData = new SceneModelData();
            sceneModelData.setEnable(1);
            sceneModelData.setSceneModelDeviceId(sceneModelDevice.getId());
            sceneModelData.setDatasourceId(model.getModelId());
            sceneModelData.setVariableType(SceneModelVariableTypeEnum.THINGS_MODEL.getType());
            sceneModelData.setSceneModelId(sceneModelDevice.getSceneModelId());
            sceneModelData.setSourceName(model.getModelName());
            sceneModelData.setIdentifier(model.getIdentifier());
            sceneModelData.setType(model.getType());
            sceneModelDataList.add(sceneModelData);
        }
        if (CollectionUtils.isNotEmpty(sceneModelDataList)) {
            sceneModelDataMapper.insertBatchSceneModelData(sceneModelDataList);
        }
    }

    /**
     * 批量删除场景管理关联设备
     *
     * @param ids 需要删除的场景管理关联设备主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelDeviceByIds(Long[] ids)
    {
        // 校验是否有使用计算公式
        int count = sceneModelDeviceMapper.checkContainAliasFormule(ids[0]);
        if (count > 0) {
            throw new ServiceException("当前设备下存在变量被引用到运算型变量计算公式中，无法删除，请删除引用关系后再执行删除操作！");
        }
        int i = sceneModelDeviceMapper.deleteSceneModelDeviceByIds(ids);
        if (i > 0) {
            sceneModelDataMapper.deleteBySceneModelDeviceIds(ids);
        }
        return i;
    }

    /**
     * 删除场景管理关联设备信息
     *
     * @param id 场景管理关联设备主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelDeviceById(Long id)
    {
        return sceneModelDeviceMapper.deleteSceneModelDeviceById(id);
    }

    @Override
    public int editEnable(SceneModelDevice sceneModelDevice) {
        int i = sceneModelDeviceMapper.updateAllEnable(sceneModelDevice);
        if (i > 0) {
            SceneModelData sceneModelData = new SceneModelData();
            sceneModelData.setSceneModelDeviceId(sceneModelDevice.getId());
            sceneModelData.setEnable(sceneModelDevice.getAllEnable());
            sceneModelData.setVariableType(sceneModelDevice.getVariableType());
            sceneModelData.setSceneModelId(sceneModelDevice.getSceneModelId());
            sceneModelDataMapper.editAllEnable(sceneModelData);
            if (SceneModelVariableTypeEnum.OPERATION_VARIABLE.getType().equals(sceneModelDevice.getVariableType())) {
                SceneModelData sceneModelData1 = new SceneModelData();
                sceneModelData1.setSceneModelId(sceneModelDevice.getSceneModelId());
                sceneModelData1.setVariableType(sceneModelDevice.getVariableType());
                List<SceneModelData> sceneModelDataList = sceneModelDataMapper.selectSceneModelDataList(sceneModelData1);
                Long[] datasourceId = sceneModelDataList.stream().map(SceneModelData::getDatasourceId).toArray(s -> new Long[s]);
                List<DeviceJob> deviceJobList = jobService.selectListByJobTypeAndDatasourceIds(datasourceId, 4);
                try {
                    for (DeviceJob job : deviceJobList) {
                        DeviceJob deviceJob = new DeviceJob();
                        deviceJob.setJobId(job.getJobId());
                        deviceJob.setJobGroup(job.getJobGroup());
                        deviceJob.setStatus(1 == sceneModelDevice.getAllEnable() ? "0" : "1");
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
    public SceneModelDevice selectOneBySceneModelIdAndVariableType(Long sceneModelId, Integer variableType) {
        return sceneModelDeviceMapper.selectOneNoDeviceBySceneModelId(sceneModelId, variableType);
    }
}
