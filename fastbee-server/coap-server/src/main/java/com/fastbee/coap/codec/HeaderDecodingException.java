package com.fastbee.coap.codec;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Getter
@Setter
public class HeaderDecodingException extends Exception{

    private int messageID;
    private InetSocketAddress remoteSocket;

    public HeaderDecodingException(int messageID, InetSocketAddress remoteSocket, String message) {
        super(message);
        this.messageID = messageID;
        this.remoteSocket = remoteSocket;
    }

}
