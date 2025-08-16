package com.fastbee.system.service.impl;



import com.fastbee.common.utils.DateUtils;
import com.fastbee.system.domain.AppLanguage;
import com.fastbee.system.mapper.AppLanguageMapper;
import com.fastbee.system.service.IAppLanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * app语言Service业务层处理
 */
@Service
public class AppLanguageServiceImpl implements IAppLanguageService
{
    @Autowired
    private AppLanguageMapper appLanguageMapper;

    /**
     * 查询app语言
     *
     * @param id app语言主键
     * @return app语言
     */
    @Override
    public AppLanguage selectAppLanguageById(Long id)
    {
        return appLanguageMapper.selectAppLanguageById(id);
    }

    /**
     * 查询app语言列表
     *
     * @param appLanguage app语言
     * @return app语言
     */
    @Override
    public List<AppLanguage> selectAppLanguageList(AppLanguage appLanguage)
    {
        return appLanguageMapper.selectAppLanguageList(appLanguage);
    }

    /**
     * 新增app语言
     *
     * @param appLanguage app语言
     * @return 结果
     */
    @Override
    public int insertAppLanguage(AppLanguage appLanguage)
    {
        appLanguage.setCreateTime(DateUtils.getNowDate());
        return appLanguageMapper.insertAppLanguage(appLanguage);
    }

    /**
     * 修改app语言
     *
     * @param appLanguage app语言
     * @return 结果
     */
    @Override
    public int updateAppLanguage(AppLanguage appLanguage)
    {
        return appLanguageMapper.updateAppLanguage(appLanguage);
    }

    /**
     * 批量删除app语言
     *
     * @param ids 需要删除的app语言主键
     * @return 结果
     */
    @Override
    public int deleteAppLanguageByIds(Long[] ids)
    {
        return appLanguageMapper.deleteAppLanguageByIds(ids);
    }

    /**
     * 删除app语言信息
     *
     * @param id app语言主键
     * @return 结果
     */
    @Override
    public int deleteAppLanguageById(Long id)
    {
        return appLanguageMapper.deleteAppLanguageById(id);
    }
}
