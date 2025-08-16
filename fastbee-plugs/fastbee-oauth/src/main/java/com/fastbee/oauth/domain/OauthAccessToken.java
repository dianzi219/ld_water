package com.fastbee.oauth.domain;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author fastb
 * @date 2023-09-01 17:00
 */
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OauthAccessToken {

    private String tokenId;

    private String token;

    private String authenticationId;

    private String userName;

    private String clientId;

    private String authentication;

    private String refreshToken;

    private String openId;

    private Long userId;

    private LocalDateTime expiresTime;
}
