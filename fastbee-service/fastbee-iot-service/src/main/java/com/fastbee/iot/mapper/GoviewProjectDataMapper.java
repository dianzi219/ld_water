package com.fastbee.iot.mapper;

import com.fastbee.iot.domain.GoviewProjectData;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 项目数据关联Mapper接口
 *
 * @author kami
 * @date 2022-10-27
 */
@Repository
public interface GoviewProjectDataMapper
{
    /**
     * 查询项目数据关联
     *
     * @param id 项目数据关联主键
     * @return 项目数据关联
     */
    public GoviewProjectData selectGoviewProjectDataById(String id);

    /**
     * 查询项目数据关联列表
     *
     * @param goviewProjectData 项目数据关联
     * @return 项目数据关联集合
     */
    public List<GoviewProjectData> selectGoviewProjectDataList(GoviewProjectData goviewProjectData);

    /**
     * 新增项目数据关联
     *
     * @param goviewProjectData 项目数据关联
     * @return 结果
     */
    public int insertGoviewProjectData(GoviewProjectData goviewProjectData);

    /**
     * 修改项目数据关联
     *
     * @param goviewProjectData 项目数据关联
     * @return 结果
     */
    public int updateGoviewProjectData(GoviewProjectData goviewProjectData);

    /**
     * 删除项目数据关联
     *
     * @param id 项目数据关联主键
     * @return 结果
     */
    public int deleteGoviewProjectDataById(String id);

    /**
     * 批量删除项目数据关联
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGoviewProjectDataByIds(String[] ids);

	GoviewProjectData selectGoviewProjectDataByProjectId(String projectId);

    /**
     * 根据sql获取组件数据接口
     * @param sql sql
     * @return 组件数据
     */
    List<LinkedHashMap> executeSql(String sql);
}
