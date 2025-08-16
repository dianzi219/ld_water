package com.fastbee.common.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_USERID_KEY = "login_userId:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 数据库查询 cache key
     */
    public static final String SQL_CACHE_KEY = "sql_cache:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 短信登录验证码 redis key
     */
    public static final String LOGIN_SMS_CAPTCHA_PHONE = "login_sms_captcha_phone:";

    /**
     * 微信获取accessToken redis key
     */
    public static final String WECHAT_GET_ACCESS_TOKEN_APPID = "wechat_get_accessToken:";

    /**
     * 短信注册验证码 redis key
     */
    public static final String REGISTER_SMS_CAPTCHA_PHONE = "register_sms_captcha_phone:";

    /**设备OTA升级实时数据*/
    public static final String DEVICE_OTA_DATA = "device:ota:";
}
