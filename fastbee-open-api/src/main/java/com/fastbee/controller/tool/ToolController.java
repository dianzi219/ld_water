package com.fastbee.controller.tool;

import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestRequestType;
import com.dtflys.forest.http.ForestResponse;
import com.fastbee.common.ProtocolDeCodeService;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.config.RuoYiConfig;
import com.fastbee.common.constant.CacheConstants;
import com.fastbee.common.constant.Constants;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.iot.response.DeCodeBo;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.exception.file.FileNameLengthLimitExceededException;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.file.FileUploadUtils;
import com.fastbee.common.utils.file.FileUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.iot.data.service.IRuleEngine;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.model.*;
import com.fastbee.iot.model.ThingsModels.ThingsModelShadow;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IToolService;
import com.fastbee.iot.util.VelocityInitializer;
import com.fastbee.iot.util.VelocityUtils;
import com.fastbee.common.core.mq.message.ReportDataBo;
import com.fastbee.mqttclient.MqttClientConfig;
import com.fastbee.mqttclient.PubMqttClient;
import com.fastbee.system.service.ISysConfigService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.fastbee.common.utils.file.FileUploadUtils.getExtension;

/**
 * 产品分类Controller
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Api(tags = "工具相关")
@RestController
@RequestMapping("/iot/tool")
public class ToolController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ToolController.class);

    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private IMqttMessagePublish messagePublish;
    @Autowired
    private MqttClientConfig mqttConfig;
    @Autowired
    private IToolService toolService;
    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;
    @Resource
    private IRuleEngine ruleEngine;
    @Resource
    private ProtocolDeCodeService deCodeService;
    @Resource
    private PubMqttClient mqttClient;

    @Autowired
    private EmqxApiConfig emqxApiConfig;

    @Resource
    private ISysConfigService configService;
    @Value("${server.broker.enabled}")
    private boolean brokerEnabled;

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterUserInput user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = toolService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 获取用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/userList")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = toolService.selectUserList(user);
        return getDataTable(list);
    }

    @ApiOperation("mqtt认证")
    @PostMapping("/mqtt/auth")
    public ResponseEntity mqttAuth(@RequestParam String clientid, @RequestParam String username, @RequestParam String password) throws Exception {
        if (clientid.startsWith("server")) {
            // 服务端认证：配置的账号密码认证
            if (mqttConfig.getUsername().equals(username) && mqttConfig.getPassword().equals(password)) {
                log.info("-----------服务端mqtt认证成功,clientId:" + clientid + "---------------");
                return ResponseEntity.ok().body("ok");
            } else {
                return toolService.returnUnauthorized(new MqttAuthenticationModel(clientid, username, password), MessageUtils.message("mqtt.unauthorized"));
            }
        } else if (clientid.startsWith("web") || clientid.startsWith("phone")) {
            // web端和移动端认证：token认证
            String token = password;
            if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
                token = token.replace(Constants.TOKEN_PREFIX, "");
            }
            try {
                Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
                log.info("-----------移动端/Web端mqtt认证成功,clientId:" + clientid + "---------------");
                return ResponseEntity.ok().body("ok");
            } catch (Exception ex) {
                return toolService.returnUnauthorized(new MqttAuthenticationModel(clientid, username, password), ex.getMessage());
            }
        } else {
           // 替换为整合之后的接口
           return toolService.clientAuth(clientid, username, password);
        }
    }
    @ApiOperation("mqtt认证")
    @PostMapping("/mqtt/authv5")
    public ResponseEntity mqttAuthv5(@RequestBody JSONObject json) throws Exception {
        log.info("-----------auth-json:" + json + "---------------");
        String clientid = json.getString("clientid");
        String username = json.getString("username");
        String password = json.getString("password");
        String peerhost = json.getString("peerhost");  // 源IP地址
        JSONObject ret = new JSONObject();
        ret.put("is_superuser", false);
        if (clientid.startsWith("server")) {
            // 服务端认证：配置的账号密码认证
            if (mqttConfig.getUsername().equals(username) && mqttConfig.getPassword().equals(password)) {
                log.info("-----------服务端mqtt认证成功,clientId:" + clientid + "---------------");
                ret.put("result", "allow");
                return ResponseEntity.ok().body(ret);
            } else {
                ret.put("result", "deny");
                return ResponseEntity.ok().body(ret);
            }
        } else if (clientid.startsWith("web") || clientid.startsWith("phone")) {
            // web端和移动端认证：token认证
            String token = password;
            if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
                token = token.replace(Constants.TOKEN_PREFIX, "");
            }
            try {
                Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
                log.info("-----------移动端/Web端mqtt认证成功,clientId:" + clientid + "---------------");
                ret.put("result", "allow");
                return ResponseEntity.ok().body(ret);
            } catch (Exception ex) {
                ret.put("result", "deny");
                return ResponseEntity.ok().body(ret);
            }
        } else {
            // 替换为整合之后的接口
            return toolService.clientAuthv5(clientid, username, password);
        }
    }

    @ApiOperation("mqtt钩子处理")
    @PostMapping("/mqtt/webhook")
    public void webHookProcess(@RequestBody MqttClientConnectModel model) {
        try {
            System.out.println("webhook:" + model.getAction());
            // 过滤服务端、web端和手机端
            if (model.getClientid().startsWith("server") || model.getClientid().startsWith("web") || model.getClientid().startsWith("phone")) {
                return;
            }
            // 设备端认证：加密认证（E）和简单认证（S，配置的账号密码认证）
            String[] clientArray = model.getClientid().split("&");
            String authType = clientArray[0];
            String deviceNumber = clientArray[1];
            Long productId = Long.valueOf(clientArray[2]);
            Long userId = Long.valueOf(clientArray[3]);

            Device device = deviceService.selectShortDeviceBySerialNumber(deviceNumber);
            ReportDataBo ruleBo = new ReportDataBo();
            ruleBo.setProductId(device.getProductId());
            ruleBo.setSerialNumber(device.getSerialNumber());
            // 设备状态（1-未激活，2-禁用，3-在线，4-离线）
            if (model.getAction().equals("client_disconnected")) {
                device.setStatus(4);
                deviceService.updateDeviceStatusAndLocation(device, "");
                // 设备掉线后发布设备状态
                messagePublish.publishStatus(device.getProductId(), device.getSerialNumber(), 4, device.getIsShadow(),device.getRssi());
                // 清空保留消息，上线后发布新的属性功能保留消息
                messagePublish.publishProperty(device.getProductId(), device.getSerialNumber(), null,0);
                messagePublish.publishFunction(device.getProductId(), device.getSerialNumber(), null,0);
                // 规则匹配处理（告警和场景联动）
                ruleBo.setType(6);
                ruleEngine.ruleMatch(ruleBo);
            } else if (model.getAction().equals("client_connected")) {
                device.setStatus(3);
                deviceService.updateDeviceStatusAndLocation(device, model.getIpaddress());
                // 设备上线后发布设备状态
                messagePublish.publishStatus(device.getProductId(), device.getSerialNumber(), 3, device.getIsShadow(),device.getRssi());
                // 影子模式，发布属性和功能
                if (device.getIsShadow() == 1) {
                    ThingsModelShadow shadow = deviceService.getDeviceShadowThingsModel(device);
                    if (shadow.getProperties().size() > 0) {
                        messagePublish.publishProperty(device.getProductId(), device.getSerialNumber(), shadow.getProperties(),3);
                    }
                    if (shadow.getFunctions().size() > 0) {
                        messagePublish.publishFunction(device.getProductId(), device.getSerialNumber(), shadow.getFunctions(),3);
                    }
                }
                // 规则匹配处理（告警和场景联动）
                ruleBo.setType(5);
                ruleEngine.ruleMatch(ruleBo);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("发生错误：" + ex.getMessage());
        }
    }

    @ApiOperation("mqtt钩子处理")
    @PostMapping("/mqtt/webhookv5")
    public void webHookProcessv5(@RequestBody JSONObject json) {
        try {
            String clientid = json.getString("clientid");
            String event = json.getString("event");
            String peername = json.getString("peername");  // 类似 127.0.0.1:8812
            String ipAddress = peername;
            if(peername.indexOf(":")!=-1){
                ipAddress = peername.substring(0,peername.indexOf(":"));
            }
            log.info("webhook:" + event + ",clientid:" + clientid);
            // 过滤服务端、web端和手机端
            if (clientid.startsWith("server") || clientid.startsWith("web") || clientid.startsWith("phone")) {
                return;
            }
            // 设备端认证：加密认证（E）和简单认证（S，配置的账号密码认证）
            String[] clientArray = clientid.split("&");
            String deviceNumber = clientArray[1];

            Device device = deviceService.selectShortDeviceBySerialNumber(deviceNumber);
            ReportDataBo ruleBo = new ReportDataBo();
            ruleBo.setProductId(device.getProductId());
            ruleBo.setSerialNumber(device.getSerialNumber());

            // 查询emqx中设备连接状态
            String emqxConnected = getEmqxConnected(clientid);
            // 设备状态（1-未激活，2-禁用，3-在线，4-离线）
            if ("client.disconnected".equals(event) && "false".equals(emqxConnected)) {
                device.setStatus(DeviceStatus.OFFLINE.getType());
                deviceService.updateDeviceStatusAndLocation(device, "");
                // 设备掉线后发布设备状态
                messagePublish.publishStatus(device.getProductId(), device.getSerialNumber(), DeviceStatus.OFFLINE.getType(), device.getIsShadow(),device.getRssi());
                // 清空保留消息，上线后发布新的属性功能保留消息
                messagePublish.publishProperty(device.getProductId(), device.getSerialNumber(), null,0);
                messagePublish.publishFunction(device.getProductId(), device.getSerialNumber(), null,0);
                // 规则匹配处理（告警和场景联动）
                ruleBo.setType(6);
                ruleEngine.ruleMatch(ruleBo);
            } else if ("client.connected".equals(event) && device.getStatus() != DeviceStatus.ONLINE.getType()) {
                device.setStatus(DeviceStatus.ONLINE.getType());
                deviceService.updateDeviceStatusAndLocation(device, ipAddress);
                // 设备掉线后发布设备状态
                messagePublish.publishStatus(device.getProductId(), device.getSerialNumber(), DeviceStatus.ONLINE.getType(), device.getIsShadow(),device.getRssi());
                // 规则匹配处理（告警和场景联动,上线放到设备订阅主题之后，保证设备执行动作能订阅和处理）
                ruleBo.setType(5);
                ruleEngine.ruleMatch(ruleBo);
            } else if("session.subscribed".equals(event)){
                // 影子模式，发布属性和功能
                if (device.getIsShadow() == 1) {
                    ThingsModelShadow shadow = deviceService.getDeviceShadowThingsModel(device);
                    if (shadow.getProperties().size() > 0) {
                        messagePublish.publishProperty(device.getProductId(), device.getSerialNumber(), shadow.getProperties(),3);
                    }
                    if (shadow.getFunctions().size() > 0) {
                        messagePublish.publishFunction(device.getProductId(), device.getSerialNumber(), shadow.getFunctions(),3);
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("发生错误：" + ex.getMessage());
        }
    }

    /**
     * 获取emqx连接状态
     * @param clientid
     * @return
     */
    public String getEmqxConnected(String clientid) {
        String apiKey = emqxApiConfig.getApiKey();
        String apiSecret = emqxApiConfig.getApiSecret();
        if ("ApiKey".equals(apiKey) || StringUtils.isEmpty(apiKey) || "ApiSecret".equals(apiSecret) || StringUtils.isEmpty(apiSecret)){
            return "false";
        }
        String url = "http://" + emqxApiConfig.getHost() + ":" + emqxApiConfig.getPort() + "/api/v5/clients/" + clientid;
        ForestRequest<?> request = Forest.request();
        request.setUrl(url);
        request.setType(ForestRequestType.GET);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", Credentials.basic(emqxApiConfig.getApiKey(), emqxApiConfig.getApiSecret()));
        ForestResponse response = request.execute(ForestResponse.class);;
        String execute = response.getResult().toString();
        if(response.statusCode() == 200) {
            JSONObject jsonObject = JSONObject.parseObject(execute);
            return jsonObject.getString("connected");
        }else if(response.statusCode() == 404) { // 404表示设备断开连接，设备断开连接后，emqx会删除该客户端，所以连接也会删除
            return "false";
        }else {
            throw new RuntimeException("emqx连接状态查询失败：" + execute + ",响应码:" + response.statusCode());
        }
    }

    @ApiOperation("获取NTP时间")
    @GetMapping("/ntp")
    public JSONObject ntp(@RequestParam Long deviceSendTime) {
        JSONObject ntpJson = new JSONObject();
        ntpJson.put("deviceSendTime", deviceSendTime);
        ntpJson.put("serverRecvTime", System.currentTimeMillis());
        ntpJson.put("serverSendTime", System.currentTimeMillis());
        return ntpJson;
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public AjaxResult uploadFile(MultipartFile file) throws Exception {
        try {
            String filePath = RuoYiConfig.getProfile();
            // 文件名长度限制
            int fileNamelength = file.getOriginalFilename().length();
            if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
                throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
            }
            // 文件类型限制
            // assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

            // 获取文件名和文件类型
            String fileName = file.getOriginalFilename();
            String extension = getExtension(file);
            //设置日期格式
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MMdd-HHmmss");
            fileName = "/iot/" + getLoginUser().getUserId().toString() + "/" + df.format(new Date()) + "." + extension;
            //创建目录
            File desc = new File(filePath + File.separator + fileName);
            if (!desc.exists()) {
                if (!desc.getParentFile().exists()) {
                    desc.getParentFile().mkdirs();
                }
            }
            // 存储文件
            file.transferTo(desc);

            String url = "/profile" + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", url);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 批量生成代码
     */
    @Log(title = "SDK生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genSdk")
    @ApiOperation("生成SDK")
    public void genSdk(HttpServletResponse response, int deviceChip) throws IOException {
        byte[] data = downloadCode(deviceChip);
        genSdk(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genSdk(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"fastbee.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 批量生成代码（下载方式）
     *
     * @param deviceChip
     * @return 数据
     */
    public byte[] downloadCode(int deviceChip) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
//        generatorCode(deviceChip, zip);
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(int deviceChip, ZipOutputStream zip) {
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(deviceChip);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList("");
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template)));
                IOUtils.write(sw.toString(), zip, Constants.UTF8);
                IOUtils.closeQuietly(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                System.out.println("渲染模板失败");
            }
        }
    }

    @GetMapping("/getTopics")
    @ApiOperation("获取所有下发的topic")
    public AjaxResult getTopics(Boolean isSimulate){
        return AjaxResult.success(TopicsUtils.getAllGet(isSimulate));
    }

    @GetMapping("/decode")
    @ApiOperation("指令编码")
    public AjaxResult decode(DeCodeBo bo){
        return AjaxResult.success(deCodeService.protocolDeCode(bo));
    }


}
