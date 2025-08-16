package com.fastbee.coap.handler;

import com.fastbee.coap.model.CoapMessage;
import com.fastbee.coap.model.CoapResponse;
import com.fastbee.coap.model.MessageCode;
import com.fastbee.coap.model.options.ContentFormat;

public class TimeResourceHandler extends AbstractResourceHandler{
    private final String path;

    public TimeResourceHandler(String path) {
        this.path = path;
    }
    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public CoapResponse handle(CoapMessage request) {
        if (request.getMessageCode() == MessageCode.GET) {
            CoapResponse coapResponse = new CoapResponse(request.getMessageType(),
                    MessageCode.CONTENT_205);
            long time = System.currentTimeMillis() % 86400000;
            long hours = time / 3600000;
            long remainder = time % 3600000;
            long minutes = remainder / 60000;
            long seconds = (remainder % 60000) / 1000;
            coapResponse.setContent(String.format("The current time is %02d:%02d:%02d", hours, minutes, seconds).getBytes(CoapMessage.CHARSET), ContentFormat.TEXT_PLAIN_UTF8);
            coapResponse.setSender(request.getSender());
            coapResponse.setMessageID(request.getMessageID());
            coapResponse.setToken(request.getToken());
            return coapResponse;
        } else {
            CoapResponse coapResponse = new CoapResponse(request.getMessageType(),
                    MessageCode.METHOD_NOT_ALLOWED_405);
            String message = "Service does not allow " + request.getMessageCodeName() + " requests.";
            coapResponse.setContent(message.getBytes(CoapMessage.CHARSET), ContentFormat.TEXT_PLAIN_UTF8);
            coapResponse.setSender(request.getSender());
            coapResponse.setMessageID(request.getMessageID());
            coapResponse.setToken(request.getToken());
            return coapResponse;

        }
    }
}
