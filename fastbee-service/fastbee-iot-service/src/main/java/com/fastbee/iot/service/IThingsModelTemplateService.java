package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.domain.ThingsModelTemplate;

/**
 * 通用物模型Service接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
public interface IThingsModelTemplateService
{
    /**
     * 查询通用物模型
     *
     * @param templateId 通用物模型主键
     * @return 通用物模型
     */
    public ThingsModelTemplate selectThingsModelTemplateByTemplateId(Long templateId);

    /**
     * 查询通用物模型列表
     *
     * @param thingsModelTemplate 通用物模型
     * @return 通用物模型集合
     */
    public List<ThingsModelTemplate> selectThingsModelTemplateList(ThingsModelTemplate thingsModelTemplate);

    /**
     * 新增通用物模型
     *
     * @param thingsModelTemplate 通用物模型
     * @return 结果
     */
    public int insertThingsModelTemplate(ThingsModelTemplate thingsModelTemplate);

    /**
     * 修改通用物模型
     *
     * @param thingsModelTemplate 通用物模型
     * @return 结果
     */
    public int updateThingsModelTemplate(ThingsModelTemplate thingsModelTemplate);


    /**
     * 批量删除通用物模型
     *
     * @param templateIds 需要删除的通用物模型主键集合
     * @return 结果
     */
    public int deleteThingsModelTemplateByTemplateIds(Long[] templateIds);

    /**
     * 删除通用物模型信息
     *
     * @param templateId 通用物模型主键
     * @return 结果
     */
    public int deleteThingsModelTemplateByTemplateId(Long templateId);

    /**
     * 导入采集点数据
     * @param lists 数据列表
     * @param tempSlaveId 从机编码
     * @return 结果
     */
    public String importData(List<ThingsModelTemplate> lists,String tempSlaveId);

    /**
     * 导出采集点数据
     *
     * @param thingsModelTemplate 通用物模型
     * @return 通用物模型集合
     */
    public List<ThingsModelTemplate> selectThingsModelTemplateExport(ThingsModelTemplate thingsModelTemplate);

}
