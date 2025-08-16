package com.fastbee.framework.web.service;

import com.fastbee.common.constant.CacheConstants;
import com.fastbee.common.constant.Constants;
import com.fastbee.common.core.domain.entity.SysDept;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.enums.UserStatus;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.exception.user.CaptchaException;
import com.fastbee.common.exception.user.CaptchaExpireException;
import com.fastbee.common.exception.user.UserPasswordNotMatchException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.ServletUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.ip.IpUtils;
import com.fastbee.framework.manager.AsyncManager;
import com.fastbee.framework.manager.factory.AsyncFactory;
import com.fastbee.framework.security.context.AuthenticationContextHolder;
import com.fastbee.system.service.ISysConfigService;
import com.fastbee.system.service.ISysDeptService;
import com.fastbee.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Resource
    private SysPasswordService passwordService;
    @Resource
    private ISysDeptService sysDeptService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid, Integer sourceType, String language) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 移动端、小程序限制终端用户登录
        Long deptId = loginUser.getDeptId();
        if (null != sourceType && 1 == sourceType && null == deptId) {
            throw new ServiceException("web端只允许租户登录！");
        }
//        if (!"admin".equals(loginUser.getUsername()) && null != sourceType) {
//            Long deptId = loginUser.getDeptId();
//            if (1 == sourceType && null == deptId) {
//                throw new ServiceException("web端只允许租户登录！");
//            }
//            if (1 != sourceType && null != deptId) {
//                throw new ServiceException("只允许终端用户登录！");
//            }
//        }
        SysDept sysDept = sysDeptService.selectDeptById(deptId);
        recordLoginInfo(loginUser.getUserId());
        loginUser.setLanguage(language);
        if (null != sysDept) {
            loginUser.setDeptUserId(sysDept.getDeptUserId());
        }
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 第三方验证后，调用登录方法
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    public String socialLogin(String username, String password, String language) {
        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        loginUser.setLanguage(language);
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 三方跳转登录认证方法
     *
     * @param username  系统用户名
     * @param encodePwd 系统用户密码
     * @return
     */
    public String redirectLogin(String username, String encodePwd, String language) {
//        UserDetails userDetails=userDetailsServiceImpl.loadUserByUsername(username);
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            throw new ServiceException("登录用户：" + username + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        // 重写验证方法
        passwordService.socialValidate(user, encodePwd);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        UserDetails userDetails = userDetailsServiceImpl.createLoginUser(user);
        LoginUser loginUser = (LoginUser) userDetails;
        recordLoginInfo(loginUser.getUserId());
        loginUser.setLanguage(language);
        // 生成token
        return tokenService.createToken(loginUser);

    }

    /**
     * 组态分享页面生成token
     *
     * @param username 系统用户名
     * @return
     */
    public String shareUserLogin(String username, String language) {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            throw new ServiceException("分享用户：" + username + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的分享账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的分享账号：" + username + " 已停用");
        }
        // 重写验证方法
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        UserDetails userDetails = userDetailsServiceImpl.createLoginUser(user);
        LoginUser loginUser = (LoginUser) userDetails;
        recordLoginInfo(loginUser.getUserId());
        loginUser.setLanguage(language);
        loginUser.setNeverExpire(Boolean.TRUE);
        // 生成token
        return tokenService.createToken(loginUser);

    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }

    public String ssoLogin(String username, String password, String language) {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            throw new ServiceException("登录用户：" + username + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        // 重写验证方法
        passwordService.ssoValidate(user, password);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        UserDetails userDetails = userDetailsServiceImpl.createLoginUser(user);
        LoginUser loginUser = (LoginUser) userDetails;
        recordLoginInfo(loginUser.getUserId());
        loginUser.setLanguage(language);
        // 生成token
        return tokenService.createToken(loginUser);
    }
}
