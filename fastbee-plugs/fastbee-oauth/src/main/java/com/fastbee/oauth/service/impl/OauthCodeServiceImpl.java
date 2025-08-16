package com.fastbee.oauth.service.impl;

import com.fastbee.common.exception.ServiceException;
import com.fastbee.oauth.domain.OauthCode;
import com.fastbee.oauth.mapper.OauthCodeMapper;
import com.fastbee.oauth.service.IOauthCodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author kerwincui
 * @date 2024-03-20
 */
@Service
public class OauthCodeServiceImpl implements IOauthCodeService
{
    @Resource
    private OauthCodeMapper oauthCodeMapper;

    /**
     * 查询【请填写功能名称】
     *
     * @param code 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public OauthCode selectOauthCodeByCode(String code)
    {
        return oauthCodeMapper.selectOauthCodeByCode(code);
    }

    /**
     * 查询【请填写功能名称】列表
     *
     * @param oauthCode 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<OauthCode> selectOauthCodeList(OauthCode oauthCode)
    {
        return oauthCodeMapper.selectOauthCodeList(oauthCode);
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param oauthCode 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertOauthCode(OauthCode oauthCode)
    {
        return oauthCodeMapper.insertOauthCode(oauthCode);
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param oauthCode 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateOauthCode(OauthCode oauthCode)
    {
        return oauthCodeMapper.updateOauthCode(oauthCode);
    }

    /**
     * 批量删除【请填写功能名称】
     *
     * @param codes 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteOauthCodeByCodes(String[] codes)
    {
        return oauthCodeMapper.deleteOauthCodeByCodes(codes);
    }

    /**
     * 删除【请填写功能名称】信息
     *
     * @param code 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteOauthCodeByCode(String code)
    {
        return oauthCodeMapper.deleteOauthCodeByCode(code);
    }

    @Override
    public OauthCode consumeAuthorizationCode(String code) {
        OauthCode oauthCode = this.selectOauthCodeByCode(code);
        if (oauthCode == null) {
            throw new ServiceException("code 不存在");
        }
//        if (DateUtils.isExpired(codeDO.getExpiresTime())) {
//            throw exception(OAUTH2_CODE_EXPIRE);
//        }
        this.deleteOauthCodeByCode(code);
        return oauthCode;
    }
}
