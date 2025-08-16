package com.fastbee.common.constant;

/**
 * 常量
 *
 * @author bill
 */
public interface FastBeeConstant {

    interface SERVER {
        String UFT8 = "UTF-8";
        String GB2312 = "GB2312";


        String MQTT = "mqtt";
        String PORT = "port";
        String ADAPTER = "adapter";
        String FRAMEDECODER = "frameDecoder";
        String DISPATCHER = "dispatcher";
        String DECODER = "decoder";
        String ENCODER = "encoder";
        String MAXFRAMELENGTH = "maxFrameLength";
        String SLICER = "slicer";
        String DELIMITERS = "delimiters";
        String IDLE = "idle";
        String WS_PREFIX = "web-";
        String WM_PREFIX = "server-";
        String FAST_PHONE = "phone-";

        /*MQTT平台判定离线时间 keepAlive*1.5 */
        Long DEVICE_PING_EXPIRED = 90000L;
    }

    interface CLIENT {
        //加盐
        String TOKEN = "fastbee-smart!@#$123";
    }

    /*webSocket配置*/
    interface WS {
        String HEART_BEAT = "heartbeat";
        String HTTP_SERVER_CODEC = "httpServerCodec";
        String AGGREGATOR = "aggregator";
        String COMPRESSOR = "compressor";
        String PROTOCOL = "protocol";
        String MQTT_WEBSOCKET = "mqttWebsocket";
        String DECODER = "decoder";
        String ENCODER = "encoder";
        String BROKER_HANDLER = "brokerHandler";

    }

    interface TASK {
        /**
         * 设备上下线任务
         */
        String DEVICE_STATUS_TASK = "deviceStatusTask";
        /**
         * 设备主动上报任务
         */
        String DEVICE_UP_MESSAGE_TASK = "deviceUpMessageTask";
        /**
         * 设备回调任务
         */
        String DEVICE_REPLY_MESSAGE_TASK = "deviceReplyMessageTask";
        /**
         * 设备下行任务
         */
        String DEVICE_DOWN_MESSAGE_TASK = "deviceDownMessageTask";
        /**
         * 服务调用(指令下发)任务
         */
        String FUNCTION_INVOKE_TASK = "functionInvokeTask";
        /**
         * 属性读取任务,区分服务调用
         */
        String DEVICE_FETCH_PROP_TASK = "deviceFetchPropTask";
        /**
         * 设备其他消息处理
         */
        String DEVICE_OTHER_TASK = "deviceOtherMsgTask";
        /**
         * 数据调试任务
         */
        String DEVICE_TEST_TASK = "deviceTestMsgTask";
        /**
         * 消息消费线程
         */
        String MESSAGE_CONSUME_TASK = "messageConsumeTask";
        /*内部消费线程publish*/
        String MESSAGE_CONSUME_TASK_PUB = "messageConsumeTaskPub";
        /*内部消费线程Fetch*/
        String MESSAGE_CONSUME_TASK_FETCH = "messageConsumeTaskFetch";
        /*OTA升级延迟队列*/
        String DELAY_UPGRADE_TASK = "delayUpgradeTask";
        /*OTA升级线程池*/
        String OTA_THREAD_POOL = "otaThreadPoolTaskExecutor";

    }

    interface MQTT {

        String PREDIX = "/+/+";

        /*OTA消息回复*/
        String OTA_REPLY = "upgrade/reply";

    }

    /*集群，全局发布的消息类型*/
    interface CHANNEL {
        /*设备状态*/
        String DEVICE_STATUS = "device_status";
        /*平台读取属性*/
        String PROP_READ = "prop_read";
        /*推送消息*/
        String PUBLISH = "publish";
        /*服务下发*/
        String FUNCTION_INVOKE = "function_invoke";
        /*事件*/
        String EVENT = "event";
        /*other*/
        String OTHER = "other";
        /*Qos1 推送应答*/
        String PUBLISH_ACK = "publish_ack";
        /*Qos2 发布消息收到*/
        String PUB_REC = "pub_rec";
        /*Qos 发布消息释放*/
        String PUB_REL = "pub_rel";
        /*Qos2 发布消息完成*/
        String PUB_COMP = "pub_comp";

        String UPGRADE = "upgrade";

        /*-------------------------ROCKETMQ-------------------------*/
        String SUFFIX = "group";
        /*设备状态*/
        String DEVICE_STATUS_GROUP = DEVICE_STATUS + SUFFIX;
        String PROP_READ_GROUP = PROP_READ + SUFFIX;
        /*服务下发*/
        String FUNCTION_INVOKE_GROUP = FUNCTION_INVOKE + SUFFIX;
        /*推送消息*/
        String PUBLISH_GROUP = PUBLISH + SUFFIX;
        /*Qos1 推送应答*/
        String PUBLISH_ACK_GROUP = PUBLISH_ACK + SUFFIX;
        /*Qos2 发布消息收到*/
        String PUB_REC_GROUP = PUB_REC + SUFFIX;
        /*Qos 发布消息释放*/
        String PUB_REL_GROUP = PUB_REL + SUFFIX;
        /*Qos2 发布消息完成*/
        String PUB_COMP_GROUP = PUB_COMP + SUFFIX;
        /*OTA升级*/
        String UPGRADE_GROUP = UPGRADE + SUFFIX;
    }


    /**
     * redisKey 定义
     */
    interface REDIS {
        /*在线设备列表*/
        String DEVICE_ONLINE_LIST = "device:online:list";
        /*设备实时状态key*/
        String DEVICE_RUNTIME_DATA = "device:runtime:";
        /*通讯协议参数*/
        String DEVICE_PROTOCOL_PARAM = "device:param:";
        /**
         * 设备消息id缓存key
         */
        String DEVICE_MESSAGE_ID = "device:messageId:";
        /**
         * 固件版本key
         */
        String FIRMWARE_VERSION = "device:firmware:";
        /**
         * 设备信息
         */
        String DEVICE_MSG = "device:msg:";
        /**
         * 属性下发回调
         */
        String PROP_READ_STORE = "prop:read:store:";
        /**
         * sip
         */
        String RECORDINFO_KEY = "sip:recordinfo:";
        String DEVICEID_KEY = "sip:deviceid:";
        String STREAM_KEY = "sip:stream:";
        String INVITE_KEY = "sip:invite:";
        String SIP_CSEQ_PREFIX = "sip:CSEQ:";
        String DEFAULT_SIP_CONFIG = "sip:config";
        String DEFAULT_MEDIA_CONFIG = "sip:mediaconfig";

        /**
         * rule
         */
        String RULE_SILENT_TIME = "rule:SilentTime";

        /**
         * 总保留消息
         */
        String MESSAGE_RETAIN_TOTAL = "message:retain:total";

        /*发送消息数*/
        String MESSAGE_SEND_TOTAL = "message:send:total";
        /*接收消息数*/
        String MESSAGE_RECEIVE_TOTAL = "message:receive:total";
        /*连接次数*/
        String MESSAGE_CONNECT_TOTAL = "message:connect:total";
        /**
         * 认证次数
         */
        String MESSAGE_AUTH_TOTAL = "message:auth:total";
        /**
         * 订阅次数
         */
        String MESSAGE_SUBSCRIBE_TOTAL = "message:subscribe:total";

        /**
         * 今日接收消息
         */
        String MESSAGE_RECEIVE_TODAY = "message:receive:today";
        /**
         * 今日发送消息
         */
        String MESSAGE_SEND_TODAY = "message:send:today";

        /**
         * 物模型值缓存
         */
        String DEVICE_PRE_KEY = "TSLV:";

        /**
         * 物模型缓存
         */
        String TSL_PRE_KEY = "TSL:";

        String MODBUS_PRE_KEY = "MODBUS:";

        /**
         * modbus缓存指令
         */
        String POLL_MODBUS_KEY = "MODBUS:POLL:";
        /**
         * modbus运行时数据
         */
        String MODBUS_RUNTIME = "MODBUS:RUNTIME:";

        String MODBUS_LOCK = "MODBUS:LOCK:";

        /**
         * 通知企业微信应用消息accessToken缓存key
         */
        String NOTIFY_WECOM_APPLY_ACCESSTOKEN = "notify:wecom:apply:";

        // 场景变量命名空间：Key:SMTV:{sceneModelTagId}
        String SCENE_MODEL_TAG_ID = "SMTV:";

        /**
         * modbus运行时数据
         */
        String MODBUS_TCP = "MODBUS:TCP:";

        /**
         * modbus运行时数据
         */
        String MODBUS_TCP_RUNTIME = "MODBUS:TCP:RUNTIME:";
        /**
         * OTA实时数据
         */
        String DEVICE_OTA_DATA = "device:ota:";

    }

    interface PROTOCOL {
        String ModbusRtu = "MODBUS-RTU";
        String YinErDa = "YinErDa";
        String JsonObject = "JSONOBJECT";
        String JsonArray = "JSON";
        String ModbusRtuPak = "MODBUS-RTU-PAK";
        String NetOTA = "OTA-NET";
        String FlowMeter = "FlowMeter";
        String RJ45 = "RJ45";
        String ModbusToJson = "MODBUS-JSON";
        String ModbusToJsonHP = "MODBUS-JSON-HP";
        String ModbusToJsonZQWL = "MODBUS-JSON-ZQWL";
        String JsonObject_ChenYi = "JSONOBJECT-CHENYI";
        String GEC6100D = "MODBUS-JSON-GEC6100D";
        String SGZ = "SGZ";
        String CH = "CH";
        String ModbusTcp = "MODBUS-TCP";
        String SL247="SL-247";

    }

    interface URL {
        /**
         * 微信小程序订阅消息推送url前缀
         */
        String WX_MINI_PROGRAM_PUSH_URL_PREFIX = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
        /**
         * 微信网站、移动应用登录获取用户access_token
         */
        String WX_GET_ACCESS_TOKEN_URL_PREFIX = "https://api.weixin.qq.com/sns/oauth2/access_token";
        /**
         * 微信小程序登录获取用户会话参数
         */
        String WX_MINI_PROGRAM_GET_USER_SESSION_URL_PREFIX = "https://api.weixin.qq.com/sns/jscode2session";
        /**
         * 微信小程序、公众号获取access_token
         */
        String WX_MINI_PROGRAM_GET_ACCESS_TOKEN_URL_PREFIX = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
        /**
         * 微信获取用户信息
         */
        String WX_GET_USER_INFO_URL_PREFIX = "https://api.weixin.qq.com/sns/userinfo";
        /**
         * 获取用户手机号信息
         */
        String WX_GET_USER_PHONE_URL_PREFIX = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";
        /**
         * 企业微信获取accessToken
         */
        String WECOM_GET_ACCESSTOKEN = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
        /**
         * 企业微信发送应用消息
         */
        String WECOM_APPLY_SEND = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
        /**
         * 微信公众号获取用户信息
         */
        String WX_PUBLIC_ACCOUNT_GET_USER_INFO_URL_PREFIX = "https://api.weixin.qq.com/cgi-bin/user/info";
        /**
         * 微信公众号发送模版消息
         */
        String WX_PUBLIC_ACCOUNT_TEMPLATE_SEND_URL_PREFIX = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    }

    interface TRANSPORT {
        String MQTT = "MQTT";
        String TCP = "TCP";
        String COAP = "COAP";
        String UDP = "UDP";
        String GB28181 = "GB28181";
    }

}
