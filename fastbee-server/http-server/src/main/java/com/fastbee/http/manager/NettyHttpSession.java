package com.fastbee.http.manager;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class NettyHttpSession implements HttpSession {
    private final String id;
    private final Map<String, Object> attributes;

    public NettyHttpSession(String id) {
        this(id, new HashMap<>());
    }

    public NettyHttpSession(String id, Map<String, Object> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    @Override
    public long getCreationTime() {
        // Implement as needed
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        // Implement as needed
        return 0;
    }

    @Override
    public javax.servlet.ServletContext getServletContext() {
        // Implement as needed
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        // Implement as needed
    }

    @Override
    public int getMaxInactiveInterval() {
        // Implement as needed
        return 0;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public void invalidate() {
        // Implement as needed
    }

    @Override
    public boolean isNew() {
        // Implement as needed
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        // Implement as needed
        return null;
    }

    @Override
    public String[] getValueNames() {
        // Implement as needed
        return new String[0];
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void removeValue(String name) {
        attributes.remove(name);
    }

    public Map<String, Object> getAttributeMap() {
        return attributes;
    }
}
