package com.fastbee.iot.mapper;

import java.util.List;
import java.util.stream.Stream;

import com.fastbee.iot.domain.SceneModelTag;
import org.apache.ibatis.annotations.Param;

/**
 * 场景录入型变量Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface SceneModelTagMapper
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
     * 删除场景录入型变量
     *
     * @param id 场景录入型变量主键
     * @return 结果
     */
    public int deleteSceneModelTagById(Long id);

    /**
     * 批量删除场景录入型变量
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSceneModelTagByIds(Long[] ids);

    /**
     * 查询场景联动相关变量
     * @param idList id
     * @return java.util.List<com.fastbee.iot.domain.SceneModelTag>
     */
    List<SceneModelTag> listSceneModelDataByIds(@Param("idList") List<Long> idList);

    /**
     * 批量删除场景变量
     * @param sceneModelIds
     * @return void
     */
    void deleteBySceneModelIds(Long[] sceneModelIds);

    /**
     * 校验变量名称唯一
     * @param sceneModelTag 场景变量
     * @return com.fastbee.iot.domain.SceneModelTag
     */
    SceneModelTag checkName(SceneModelTag sceneModelTag);
}
