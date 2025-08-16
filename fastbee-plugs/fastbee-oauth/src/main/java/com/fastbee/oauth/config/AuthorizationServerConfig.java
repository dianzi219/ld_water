package com.fastbee.oauth.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 授权服务器配置，配置客户端id，密钥和令牌的过期时间
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Resource
    private DataSource dataSource;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserDetailsService userDetailsService;

    /**
     * 用来配置令牌端点(Token Endpoint)的安全约束
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
                .authenticationEntryPoint(new OAuth2AuthenticationEntryPoint());
//        security.allowFormAuthenticationForClients()
//                .tokenKeyAccess("permitAll()")
//                // 允许 /oauth/token_check端点的访问
//                .checkTokenAccess("permitAll()")
//                .passwordEncoder(new PasswordEncoder() {
//                    @Override
//                    public String encode(CharSequence charSequence) {
//                        // 密码加密
//                        return null;
//                    }
//
//                    @Override
//                    public boolean matches(CharSequence charSequence, String s) {
//                        // 密码校验
//                        // return false;
//                        return true;
//                    }
//                })
//                .allowFormAuthenticationForClients();
    }

    /**
     * 用来配置客户端详情服务
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.withClientDetails(getClientDetailsService());
    }

    /**
     * 用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)。
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 查询用户、授权、分组，可以被重写
        endpoints.userDetailsService(userDetailsService)
                // 审批客户端的授权
                .userApprovalHandler(userApprovalHandler())
                // 授权审批
                .approvalStore(approvalStore())
                // 获取授权码
                .authorizationCodeServices(new JdbcAuthorizationCodeServices(dataSource))
                // 验证token
                .authenticationManager(authenticationManager)
                // 查询、保存、刷新token
                .tokenStore(this.getJdbcTokenStore());
    }

    @Bean
    public ApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource);
    }

    @Bean
    public UserApprovalHandler userApprovalHandler() {
        return new SpeakerApprovalHandler(getClientDetailsService(), approvalStore(), oAuth2RequestFactory());
    }

    @Bean
    public JdbcClientDetailsService getClientDetailsService() {
        JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        jdbcClientDetailsService.setPasswordEncoder(passwordEncoder());
        return jdbcClientDetailsService;
    }

    @Bean
    public OAuth2RequestFactory oAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(getClientDetailsService());
    }
    @Bean
    public TokenStore getJdbcTokenStore(){
        TokenStore tokenStore = new JdbcTokenStore(dataSource);
        return tokenStore;
    }



    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
