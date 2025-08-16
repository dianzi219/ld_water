package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysDept;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.Product;
import com.fastbee.iot.domain.SceneModel;
import com.fastbee.iot.domain.SceneModelDevice;
import com.fastbee.iot.domain.SipRelation;
import com.fastbee.iot.mapper.*;
import com.fastbee.iot.model.scenemodel.CusDeviceVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.ISceneModelService;
import com.fastbee.iot.service.ISipRelationService;
import com.fastbee.system.mapper.SysDeptMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 场景管理Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-20
 */
@Service
public class SceneModelServiceImpl implements ISceneModelService
{
    @Resource
    private SceneModelMapper sceneModelMapper;

    @Resource
    private SceneModelDeviceMapper sceneModelDeviceMapper;


    @Resource
    private SceneModelDataMapper sceneModelDataMapper;

    @Resource
    private SceneModelTagMapper sceneModelTagMapper;

    @Resource
    private SceneTagPointsMapper sceneTagPointsMapper;
    @Resource
    private ISipRelationService sipRelationService;
    @Resource
    private SysDeptMapper sysDeptMapper;

    /**
     * 查询场景管理
     *
     * @param sceneModelId 场景管理主键
     * @return 场景管理
     */
    @Override
    public SceneModel selectSceneModelBySceneModelId(Long sceneModelId)
    {
        SceneModel sceneModel = sceneModelMapper.selectSceneModelBySceneModelId(sceneModelId);
        if (null == sceneModel) {
            return null;
        }
        //查询关联的监控设备
        SipRelation sipRelation = new SipRelation();
        sipRelation.setReSceneModelId(sceneModelId);
        List<SipRelation> sipRelationList = sipRelationService.selectSipRelationList(sipRelation);
        sceneModel.setSipRelationList(sipRelationList);
        SceneModelDevice sceneModelDevice = new SceneModelDevice();
        sceneModelDevice.setSceneModelId(sceneModelId);
        List<SceneModelDevice> sceneModelDeviceList = sceneModelDeviceMapper.selectSceneModelDeviceList(sceneModelDevice);
        if (CollectionUtils.isEmpty(sceneModelDeviceList)) {
            return sceneModel;
        }
        List<CusDeviceVO> cusDeviceVOList = new ArrayList<>();
        for (SceneModelDevice modelDevice : sceneModelDeviceList) {
            if (SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(modelDevice.getVariableType())) {
                CusDeviceVO cusDeviceVO = new CusDeviceVO();
                cusDeviceVO.setName(modelDevice.getName());
                cusDeviceVO.setProductId(modelDevice.getProductId());
                cusDeviceVO.setSerialNumber(modelDevice.getSerialNumber());
                cusDeviceVOList.add(cusDeviceVO);
            }
        }
        sceneModel.setCusDeviceList(cusDeviceVOList);
        sceneModel.setSceneModelDeviceList(CollectionUtils.isNotEmpty(sceneModelDeviceList) ? sceneModelDeviceList : new ArrayList<>());
        return sceneModel;
    }

    /**
     * 查询场景管理列表
     *
     * @param sceneModel 场景管理
     * @return 场景管理
     */
    @Override
    public List<SceneModel> selectSceneModelList(SceneModel sceneModel)
    {
        SysUser user = getLoginUser().getUser();
        if (null == sceneModel.getTenantId()) {
            sceneModel.setTenantId(user.getDept().getDeptUserId());
        }
        if (null != sceneModel.getDeptId()) {
            SysDept sysDept = sysDeptMapper.selectDeptById(sceneModel.getDeptId());
            sceneModel.setTenantId(sysDept.getDeptUserId());
        }
        List<SceneModel> sceneModelList = sceneModelMapper.selectSceneModelList(sceneModel);
        // 组态不是所有人都买了，故单独查询组态信息
        List<String> guidList = sceneModelList.stream().map(SceneModel::getGuid).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(guidList)) {
            List<SceneModel> scadaList = sceneModelMapper.selectListScadaIdByGuidS(guidList);
            Map<String, Long> map = scadaList.stream().collect(Collectors.toMap(SceneModel::getGuid, SceneModel::getScadaId));
            for (SceneModel sceneModel1 : sceneModelList) {
                Long scadaId = map.get(sceneModel1.getGuid());
                sceneModel1.setScadaId(scadaId);
            }
        }
        return sceneModelList;
    }

    /**
     * 新增场景管理
     *
     * @param sceneModel 场景管理
     * @return 结果
     */
    @Override
    public int insertSceneModel(SceneModel sceneModel)
    {
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            sceneModel.setTenantId(user.getDept().getDeptUserId());
        } else {
            sceneModel.setTenantId(user.getUserId());
        }
        sceneModel.setCreateBy(user.getUserName());
        sceneModel.setUpdateBy(user.getUserName());
        int result = sceneModelMapper.insertSceneModel(sceneModel);
        if (result > 0) {
            for (SceneModelVariableTypeEnum sceneModelVariableTypeEnum : SceneModelVariableTypeEnum.ADD_LIST) {
                SceneModelDevice sceneModelDevice = new SceneModelDevice();
                sceneModelDevice.setSceneModelId(sceneModel.getSceneModelId());
                sceneModelDevice.setVariableType(sceneModelVariableTypeEnum.getType());
                sceneModelDevice.setName(sceneModelVariableTypeEnum.getName());
                sceneModelDevice.setAllEnable(1);
                sceneModelDevice.setSort(sceneModelVariableTypeEnum.getType());
                sceneModelDevice.setCreateBy(user.getUserName());
                sceneModelDevice.setCreateBy(user.getUserName());
                sceneModelDeviceMapper.insertSceneModelDevice(sceneModelDevice);
            }
        }
        return result;
    }

    /**
     * 修改场景管理
     *
     * @param sceneModel 场景管理
     * @return 结果
     */
    @Override
    public int updateSceneModel(SceneModel sceneModel)
    {
        SysUser user = getLoginUser().getUser();
        sceneModel.setUpdateBy(user.getUserName());
        SceneModel oldSceneModel = sceneModelMapper.selectBySceneModelId(sceneModel.getSceneModelId());
        SysDept sysDept = sysDeptMapper.selectDeptById(sceneModel.getDeptId());
        if (!oldSceneModel.getTenantId().equals(sysDept.getDeptUserId())) {
            sceneModel.setTenantId(sysDept.getDeptUserId());
        }
        return sceneModelMapper.updateSceneModel(sceneModel);
    }

    /**
     * 批量删除场景管理
     *
     * @param sceneModelIds 需要删除的场景管理主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelBySceneModelIds(Long[] sceneModelIds)
    {
        int i = sceneModelMapper.deleteSceneModelBySceneModelIds(sceneModelIds);
        if (i > 0) {
            sceneModelDeviceMapper.deleteBySceneModelIds(sceneModelIds);
            sceneModelDataMapper.deleteBySceneModelIds(sceneModelIds);
            sceneTagPointsMapper.deleteBySceneModelIds(sceneModelIds);
            sceneModelTagMapper.deleteBySceneModelIds(sceneModelIds);
        }
        return i;
    }

    /**
     * 删除场景管理信息
     *
     * @param sceneModelId 场景管理主键
     * @return 结果
     */
    @Override
    public int deleteSceneModelBySceneModelId(Long sceneModelId)
    {
        return sceneModelMapper.deleteSceneModelBySceneModelId(sceneModelId);
    }

    /**
     * 根据监控设备id获取场景
     * @param channelId
     * @return
     */
    @Override
    public SceneModel selectSceneModelByChannelId(String channelId){
        return sceneModelMapper.selectSceneModelByChannelId(channelId);
    }
}
