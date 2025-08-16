package com.fastbee.iot.service;

import java.time.LocalDateTime;
import java.util.List;
import com.fastbee.iot.domain.SceneModelTag;
import com.fastbee.iot.domain.SceneTagPoints;
import com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO;

/**
 * 场景录入型变量Service接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface ISceneModelTagService
{
    /**
     * 查询场景录入型变量
     *
     * @param id 场景录入型变量主键
     * @return 场景录入型变量
     */
    public SceneModelTag selectSceneModelTagById(Long id);

    /**
     * 查询场景录入型变量列表
     *
     * @param sceneModelTag 场景录入型变量
     * @return 场景录入型变量集合
     */
    public List<SceneModelTag> selectSceneModelTagList(SceneModelTag sceneModelTag);

    /**
     * 新增场景录入型变量
     *
     * @param sceneModelTag 场景录入型变量
     * @return 结果
     */
    public int insertSceneModelTag(SceneModelTag sceneModelTag);

    /**
     * 修改场景录入型变量
     *
     * @param sceneModelTag 场景录入型变量
     * @return 结果
     */
    public int updateSceneModelTag(SceneModelTag sceneModelTag);

    /**
     * 批量删除场景录入型变量
     *
     * @param ids 需要删除的场景录入型变量主键集合
     * @return 结果
     */
    public int deleteSceneModelTagByIds(Long[] ids);

    /**
     * 删除场景录入型变量信息
     *
     * @param id 场景录入型变量主键
     * @return 结果
     */
    public int deleteSceneModelTagById(Long id);

    /**
     * 校验变量是否被运用到计算公式
     * @param sceneModelTag 场景变量类
     * @return java.lang.String
     */
    String checkAliasFormule(SceneModelTag sceneModelTag);

    /**
     * 处理变量运算时间周期
     * @param cycleType 周期类型
     * @param: cycle 格式
     * @param: executeTime 执行时间
     * @return com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO
     */
    SceneModelTagCycleVO handleTimeCycle(Integer cycleType, String cycle, LocalDateTime executeTime);

    /**
     * 获取场景变量实时值
     * @param sceneTagPoints 场景变量类
     * @param: sceneModelTagCycleVO 时间周期类
     * @return java.lang.String
     */
    String getSceneModelDataValue(SceneTagPoints sceneTagPoints, SceneModelTagCycleVO sceneModelTagCycleVO);
}
