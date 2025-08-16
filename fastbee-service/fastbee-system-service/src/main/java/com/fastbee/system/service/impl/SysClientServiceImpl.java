package com.fastbee.system.service.impl;

import java.util.List;
import com.fastbee.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.system.mapper.SysClientMapper;
import com.fastbee.system.domain.SysClient;
import com.fastbee.system.service.ISysClientService;

/**
 * 系统授权Service业务层处理
 * 
 * @author zhuangpeng.li
 * @date 2024-07-26
 */
@Service
public class SysClientServiceImpl implements ISysClientService 
{
    @Autowired
    private SysClientMapper sysClientMapper;

    /**
     * 查询系统授权
     * 
     * @param id 系统授权主键
     * @return 系统授权
     */
    @Override
    public SysClient selectSysClientById(Long id)
    {
        return sysClientMapper.selectSysClientById(id);
    }

    /**
     * 查询系统授权列表
     * 
     * @param sysClient 系统授权
     * @return 系统授权
     */
    @Override
    public List<SysClient> selectSysClientList(SysClient sysClient)
    {
        return sysClientMapper.selectSysClientList(sysClient);
    }

    /**
     * 新增系统授权
     * 
     * @param sysClient 系统授权
     * @return 结果
     */
    @Override
    public int insertSysClient(SysClient sysClient)
    {
        sysClient.setCreateTime(DateUtils.getNowDate());
        return sysClientMapper.insertSysClient(sysClient);
    }

    /**
     * 修改系统授权
     * 
     * @param sysClient 系统授权
     * @return 结果
     */
    @Override
    public int updateSysClient(SysClient sysClient)
    {
        sysClient.setUpdateTime(DateUtils.getNowDate());
        return sysClientMapper.updateSysClient(sysClient);
    }

    /**
     * 批量删除系统授权
     * 
     * @param ids 需要删除的系统授权主键
     * @return 结果
     */
    @Override
    public int deleteSysClientByIds(Long[] ids)
    {
        return sysClientMapper.deleteSysClientByIds(ids);
    }

    /**
     * 删除系统授权信息
     * 
     * @param id 系统授权主键
     * @return 结果
     */
    @Override
    public int deleteSysClientById(Long id)
    {
        return sysClientMapper.deleteSysClientById(id);
    }
}
