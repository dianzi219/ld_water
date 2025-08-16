package com.fastbee.oauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fastbee.oauth.domain.OauthAccessToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OauthAccessTokenMapper extends BaseMapper<OauthAccessToken> {

    String selectUserNameByTokenId(String tokenId);

    OauthAccessToken selectByTokenId(String tokenId);

    void updateOpenIdByTokenId(@Param("tokenId") String tokenId,@Param("openUid")  String openUid);

    OauthAccessToken selectByUserName(String userName);

    void insertOauthAccessToken(OauthAccessToken oauthAccessToken);

    void deleteByUserId(Long userId);
}
