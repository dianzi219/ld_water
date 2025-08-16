package com.fastbee.http.manager;

import com.fastbee.common.core.redis.RedisCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class HttpSessionManager {
    @Autowired
    private RedisCache sessionStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpSessionManager() {
    }

    public String createSession() {
        String sessionId = generateSessionId();
        saveSession(sessionId, new NettyHttpSession(sessionId));
        return sessionId;
    }

    public HttpSession getSession(String sessionId) {
        String sessionData = sessionStore.getCacheObject("session:" + sessionId);
        if (sessionData != null) {
            try {
                return new NettyHttpSession(sessionId, objectMapper.readValue(sessionData, Map.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void saveSession(String sessionId, HttpSession session) {
        try {
            String sessionData = objectMapper.writeValueAsString(((NettyHttpSession) session).getAttributeMap());
            sessionStore.setCacheObject("session:" + sessionId, sessionData);
            sessionStore.expire("session:" + sessionId, 30 * 60);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSessionId() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
