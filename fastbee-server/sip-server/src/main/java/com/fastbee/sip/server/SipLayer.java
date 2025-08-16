package com.fastbee.sip.server;

import com.fastbee.sip.conf.SipProperties;
import com.fastbee.sip.conf.SysSipConfig;
import com.fastbee.sip.domain.MediaServer;
import com.fastbee.sip.model.ZlmMediaServer;
import com.fastbee.sip.service.IMediaServerService;
import com.fastbee.sip.service.ISipCacheService;
import com.fastbee.sip.util.ZlmApiUtils;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@Order(2)
public class SipLayer implements ApplicationRunner {
    @Autowired
    private SysSipConfig sipConfig;

    @Autowired
    private IGBListener gbListener;

    @Autowired
    private ZlmApiUtils zlmApiUtils;

    @Autowired
    private ISipCacheService sipCacheService;

    @Autowired
    private IMediaServerService mediaServerService;

    private final Map<String, SipProviderImpl> tcpSipProviderMap = new ConcurrentHashMap<>();
    private final Map<String, SipProviderImpl> udpSipProviderMap = new ConcurrentHashMap<>();
    private final List<String> monitorIps = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.warn("SipLayer run");
        if (ObjectUtils.isEmpty(sipConfig.getIp())) {
            try {
                // 获得本机的所有网络接口
                Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
                while (nifs.hasMoreElements()) {
                    NetworkInterface nif = nifs.nextElement();
                    // 获得与该网络接口绑定的 IP 地址，一般只有一个
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            if (addr.getHostAddress().equals("127.0.0.1")){
                                continue;
                            }
                            if (nif.getName().startsWith("docker")) {
                                continue;
                            }
                            log.info("[自动配置SIP监听网卡] 网卡接口地址： {}", addr.getHostAddress());// 只关心 IPv4 地址
                            monitorIps.add(addr.getHostAddress());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[读取网卡信息失败]", e);
            }
            if (monitorIps.isEmpty()) {
                log.error("[自动配置SIP监听网卡信息失败]， 请手动配置SIP.IP后重新启动");
                System.exit(1);
            }
        } else {
            // 使用逗号分割多个ip
            String separator = ",";
            if (sipConfig.getIp().indexOf(separator) > 0) {
                String[] split = sipConfig.getIp().split(separator);
                monitorIps.addAll(Arrays.asList(split));
            }else {
                monitorIps.add(sipConfig.getIp());
            }
        }
        if (monitorIps.size() > 0 && sipConfig.isEnabled()) {
            SipFactory.getInstance().setPathName("gov.nist");
            for (String monitorIp : monitorIps) {
                addListeningPoint(monitorIp, sipConfig.getPort());
            }
            if (udpSipProviderMap.size() + tcpSipProviderMap.size() == 0) {
                System.exit(1);
            }
            //缓存zlm服务器配置
            MediaServer media = mediaServerService.selectMediaServerBytenantId(1L);
            if (media != null) {
                ZlmMediaServer mediaServer = zlmApiUtils.getMediaServerConfig(media);
                if (mediaServer != null) {
                    log.info("zlm配置获取成功成功...");
                    sipCacheService.updateMediaInfo(mediaServer);
                    mediaServerService.syncMediaServer(media, media.getSecret());
                }
            }
        }
    }

    private void addListeningPoint(String monitorIp, int port){
        SipStackImpl sipStack;
        try {
            sipStack = (SipStackImpl)SipFactory.getInstance().createSipStack(SipProperties.getProperties("GB28181_SIP",sipConfig.isLog()));
            sipStack.setMessageParserFactory(new GbMsgParserFactory());
        } catch (PeerUnavailableException e) {
            log.error("[SIP SERVER] SIP服务启动失败， 监听地址{}失败,请检查ip是否正确", monitorIp);
            return;
        }

        try {
            ListeningPoint tcpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "TCP");
            SipProviderImpl tcpSipProvider = (SipProviderImpl)sipStack.createSipProvider(tcpListeningPoint);

            tcpSipProvider.setDialogErrorsAutomaticallyHandled();
            tcpSipProvider.addSipListener(gbListener);
            tcpSipProviderMap.put(monitorIp, tcpSipProvider);
            log.info("[SIP SERVER] tcp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[SIP SERVER] tcp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }

        try {
            ListeningPoint udpListeningPoint = sipStack.createListeningPoint(monitorIp, port, "UDP");

            SipProviderImpl udpSipProvider = (SipProviderImpl)sipStack.createSipProvider(udpListeningPoint);
            udpSipProvider.addSipListener(gbListener);

            udpSipProviderMap.put(monitorIp, udpSipProvider);

            log.info("[SIP SERVER] udp://{}:{} 启动成功", monitorIp, port);
        } catch (TransportNotSupportedException
                 | TooManyListenersException
                 | ObjectInUseException
                 | InvalidArgumentException e) {
            log.error("[SIP SERVER] udp://{}:{} SIP服务启动失败,请检查端口是否被占用或者ip是否正确"
                    , monitorIp, port);
        }
    }

    public SipProviderImpl getUdpSipProvider(String ip) {
        if (udpSipProviderMap.size() == 1) {
            return udpSipProviderMap.values().stream().findFirst().get();
        }
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return udpSipProviderMap.get(ip);
    }

    public SipProviderImpl getUdpSipProvider() {
        if (udpSipProviderMap.size() != 1) {
            return udpSipProviderMap.get(sipConfig.getIp());
        }
        return udpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider() {
        if (tcpSipProviderMap.size() != 1) {
            return null;
        }
        return tcpSipProviderMap.values().stream().findFirst().get();
    }

    public SipProviderImpl getTcpSipProvider(String ip) {
        if (tcpSipProviderMap.size() == 1) {
            return tcpSipProviderMap.values().stream().findFirst().get();
        }
        if (ObjectUtils.isEmpty(ip)) {
            return null;
        }
        return tcpSipProviderMap.get(ip);
    }

    public String getLocalIp(String deviceLocalIp) {
		if (monitorIps.size() == 1) {
			return monitorIps.get(0);
		}
		if (!ObjectUtils.isEmpty(deviceLocalIp)) {
			return deviceLocalIp;
		}
		return getUdpSipProvider().getListeningPoint().getIPAddress();
    }
}
