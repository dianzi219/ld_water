package com.fastbee.system.mapper;

import java.util.List;
import com.fastbee.system.domain.SysClient;

/**
 * 系统授权Mapper接口
 *
 * @author zhuangpeng.li
 * @date 2024-07-26
 */
public interface SysClientMapper
{
    /**
     * 查询系统授权
     *
     * @param id 系统授权主键
     * @return 系统授权
     */
    public SysClient selectSysClientById(Long id);

    /**
     * 查询系统授权列表
     *
     * @param sysClient 系统授权
     * @return 系统授权集合
     */
    public List<SysClient> selectSysClientList(SysClient sysClient);

    /**
     * 新增系统授权
     *
     * @param sysClient 系统授权
     * @return 结果
     */
    public int insertSysClient(SysClient sysClient);

    /**
     * 修改系统授权
     *
     * @param sysClient 系统授权
     * @return 结果
     */
    public int updateSysClient(SysClient sysClient);

    /**
     * 删除系统授权
     *
     * @param id 系统授权主键
     * @return 结果
     */
    public int deleteSysClientById(Long id);

    /**
     * 批量删除系统授权
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysClientByIds(Long[] ids);
}
