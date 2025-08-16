package com.fastbee.coap.codec;

import com.fastbee.coap.model.Token;
import com.fastbee.coap.model.options.Option;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Getter
@Setter
public class OptionCodecException extends Exception {

    private static final String message = "Unsupported or misplaced critical option %s";
    private int optionNumber;
    private int messageID;
    private Token token;
    private InetSocketAddress remoteSocket;
    private int messageType;

    public OptionCodecException(int optionNumber) {
        super();
        this.optionNumber = optionNumber;
    }
    @Override
    public String getMessage() {
        return String.format(message, Option.asString(this.optionNumber));
    }
}
