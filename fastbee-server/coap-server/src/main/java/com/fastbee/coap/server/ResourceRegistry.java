package com.fastbee.coap.server;

import com.fastbee.coap.handler.ResourceHandler;
import com.fastbee.coap.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.fastbee.coap.model.MessageCode.BAD_REQUEST_400;

@Slf4j
@Component
public class ResourceRegistry{
    private final Map<String, ResourceHandler> handlers = new HashMap<String, ResourceHandler>();

    public void register(ResourceHandler handler) {
        handlers.put(handler.getPath(), handler);
    }

    public CoapMessage respond(CoapRequest request) {
        // find the URI
        String url = request.getUriPath();
        log.debug("requested URL : {}", url);

        if (url.length() < 1) {
            // 4.00 !
            return CoapResponse.createErrorResponse(request.getMessageType(), BAD_REQUEST_400,"no URL !");
        }
        if (".well-known/core".equalsIgnoreCase(url)) {
            // discovery !
            CoapResponse coapResponse = CoapResponse.createErrorResponse(MessageType.ACK, MessageCode.CONTENT_205,"");
            coapResponse.setContent(getDiscovery());
            coapResponse.setSender(request.getSender());
            coapResponse.setMessageID(request.getMessageID());
            coapResponse.setToken(request.getToken());
            return coapResponse;
        } else {
            ResourceHandler handler = handlers.get(url);
            if (handler == null) {
                // 4.04 !
                CoapResponse coapResponse = CoapResponse.createErrorResponse(MessageType.ACK, MessageCode.NOT_FOUND_404,"");
                coapResponse.setContent("not found !".getBytes());
                coapResponse.setSender(request.getSender());
                return coapResponse;
            } else {
                return handler.handle(request);
            }
        }
    }

    private byte[] getDiscovery() {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, ResourceHandler> e : handlers.entrySet()) {
            // ex :</link1>;if="If1";rt="Type1 Type2";title="Link test resource",
            if (first) {
                first = false;
            } else {
                b.append(",");
            }
            ResourceHandler h = e.getValue();
            b.append("</").append(h.getPath()).append(">");
            if (h.getInterface() != null) {
                b.append(";if=\"").append(h.getInterface()).append("\"");
            }
            if (h.getResourceType() != null) {
                b.append(";rt=\"").append(h.getResourceType()).append("\"");
            }
            if (h.getTitle() != null) {
                b.append(";title=\"").append(h.getTitle()).append("\"");
            }
        }
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }
}
