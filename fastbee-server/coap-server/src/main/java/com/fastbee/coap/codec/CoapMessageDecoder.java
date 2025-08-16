package com.fastbee.coap.codec;

import com.fastbee.coap.model.*;
import com.fastbee.coap.model.options.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.fastbee.coap.model.MessageCode.EMPTY;
import static com.fastbee.coap.model.MessageType.*;

@ChannelHandler.Sharable
@Slf4j
public class CoapMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        ByteBuf buffer = datagramPacket.content();
        log.debug("Incoming message to be decoded (length: {})", buffer.readableBytes());
        //Decode the header values
        int encodedHeader = buffer.readInt();
        int version =     (encodedHeader >>> 30) & 0x03;
        int messageType = (encodedHeader >>> 28) & 0x03;
        int tokenLength = (encodedHeader >>> 24) & 0x0F;
        int messageCode = (encodedHeader >>> 16) & 0xFF;
        int messageID =   (encodedHeader)        & 0xFFFF;


        log.debug("Decoded Header: (T) {}, (TKL) {}, (C) {}, (ID) {}",
                messageType, tokenLength, messageCode, messageID);

        //Check whether the protocol version is supported (=1)
        if (version != CoapMessage.PROTOCOL_VERSION) {
            String message = "CoAP version (" + version + ") is other than \"1\"!";
            throw new HeaderDecodingException(messageID, datagramPacket.sender(), message);
        }

        //Check whether TKL indicates a not allowed token length
        if (tokenLength > CoapMessage.MAX_TOKEN_LENGTH) {
            String message = "TKL value (" + tokenLength + ") is larger than 8!";
            throw new HeaderDecodingException(messageID, datagramPacket.sender(), message);
        }

        //Check whether there are enough unread bytes left to read the token
        if (buffer.readableBytes() < tokenLength) {
            String message = "TKL value is " + tokenLength + " but only " + buffer.readableBytes() + " bytes left!";
            throw new HeaderDecodingException(messageID, datagramPacket.sender(), message);
        }

        //Handle empty message (ignore everything but the first 4 bytes)
        if (messageCode == EMPTY) {
            if (messageType == ACK) {
                log.info("ACK Message");
                list.add(CoapMessage.createEmptyAcknowledgement(messageID));
                return ;
            } else if (messageType == RST) {
                log.info("RST Message");
                list.add(CoapMessage.createEmptyReset(messageID));
                return;
            } else if (messageType == CON) {
                log.info("CON Message");
                list.add(CoapMessage.createPing(messageID));
                return;
            } else {
                //There is no empty NON message defined, so send a RST
                throw new HeaderDecodingException(messageID, datagramPacket.sender(), "Empty NON messages are invalid!");
            }
        }

        //Read the token
        byte[] token = new byte[tokenLength];
        buffer.readBytes(token);

        //Handle non-empty messages (CON, NON or ACK)
        CoapMessage coapMessage;

        if (MessageCode.isRequest(messageCode)) {
            coapMessage = new CoapRequest(messageType, messageCode);
        } else {
            coapMessage = new CoapResponse(messageType, messageCode);
            coapMessage.setMessageType(messageType);
        }

        coapMessage.setMessageID(messageID);
        coapMessage.setToken(new Token(token));

        //Decode and set the options
        if (buffer.readableBytes() > 0) {
            try {
                setOptions(coapMessage, buffer);
            } catch (OptionCodecException ex) {
                ex.setMessageID(messageID);
                ex.setToken(new Token(token));
                //ex.setRemoteSocket(remoteSocket);
                ex.setMessageType(messageType);
                throw ex;
            }
        }

        //The remaining bytes (if any) are the messages payload. If there is no payload, reader and writer index are
        //at the same position (buf.readableBytes() == 0).
        buffer.discardReadBytes();

        try {
            coapMessage.setContent(buffer);
        } catch (IllegalArgumentException e) {
            String warning = "Message code {} does not allow content. Ignore {} bytes.";
            log.warn(warning, coapMessage.getMessageCode(), buffer.readableBytes());
        }
        log.info("Decoded Message: {}", coapMessage);
        coapMessage.setSender(datagramPacket.sender());
        list.add(coapMessage);
    }

    private void setOptions(CoapMessage coapMessage, ByteBuf buffer) throws OptionCodecException {

        //Decode the options
        int previousOptionNumber = 0;
        int firstByte = buffer.readByte() & 0xFF;

        while(firstByte != 0xFF && buffer.readableBytes() >= 0) {
            log.debug("First byte: {} ({})", toBinaryString(firstByte), firstByte);
            int optionDelta =   (firstByte & 0xF0) >>> 4;
            int optionLength =   firstByte & 0x0F;
            log.debug("temp. delta: {}, temp. length {}", optionDelta, optionLength);

            if (optionDelta == 13) {
                optionDelta += buffer.readByte() & 0xFF;
            } else if (optionDelta == 14) {
                optionDelta = 269 + ((buffer.readByte() & 0xFF) << 8) + (buffer.readByte() & 0xFF);
            }

            if (optionLength == 13) {
                optionLength += buffer.readByte() & 0xFF;
            } else if (optionLength == 14) {
                optionLength = 269 + ((buffer.readByte() & 0xFF) << 8) + (buffer.readByte() & 0xFF);
            }


            log.info("Previous option: {}, Option delta: {}", previousOptionNumber, optionDelta);

            int actualOptionNumber = previousOptionNumber + optionDelta;
            log.info("Decode option no. {} with length of {} bytes.", actualOptionNumber, optionLength);

            try {
                byte[] optionValue = new byte[optionLength];
                buffer.readBytes(optionValue);

                switch(OptionValue.getType(actualOptionNumber)) {
                    case EMPTY: {
                        EmptyOptionValue value = new EmptyOptionValue(actualOptionNumber);
                        coapMessage.addOption(actualOptionNumber, value);
                        break;
                    }
                    case OPAQUE: {
                        OpaqueOptionValue value = new OpaqueOptionValue(actualOptionNumber, optionValue);
                        coapMessage.addOption(actualOptionNumber, value);
                        break;
                    }
                    case STRING: {
                        StringOptionValue value = new StringOptionValue(actualOptionNumber, optionValue, true);
                        coapMessage.addOption(actualOptionNumber, value);
                        break;
                    }
                    case UINT: {
                        UintOptionValue value = new UintOptionValue(actualOptionNumber, optionValue, true);
                        coapMessage.addOption(actualOptionNumber, value);
                        break;
                    }
                    default: {
                        log.error("This should never happen!");
                        throw new RuntimeException("This should never happen!");
                    }
                }
            } catch (IllegalArgumentException e) {
                //failed option creation leads to an illegal argument exception
                log.warn("Exception while decoding option!", e);

                if (MessageCode.isResponse(coapMessage.getMessageCode())) {
                    //Malformed options in responses are silently ignored...
                    log.warn("Silently ignore malformed option no. {} in inbound response.", actualOptionNumber);
                } else if (Option.isCritical(actualOptionNumber)) {
                    //Critical malformed options in requests cause an exception
                    throw new OptionCodecException(actualOptionNumber);
                } else {
                    //Not critical malformed options in requests are silently ignored...
                    log.warn("Silently ignore elective option no. {} in inbound request.", actualOptionNumber);
                }
            }

            previousOptionNumber = actualOptionNumber;

            if (buffer.readableBytes() > 0) {
                firstByte = buffer.readByte() & 0xFF;
            } else {
                // this is necessary if there is no payload and the last option is empty (e.g. UintOption with value 0)
                firstByte = 0xFF;
            }

            log.debug("{} readable bytes remaining.", buffer.readableBytes());
        }
    }

    private static String toBinaryString(int byteValue) {
        StringBuilder buffer = new StringBuilder(8);

        for(int i = 7; i >= 0; i--) {
            if ((byteValue & (int) Math.pow(2, i)) > 0) {
                buffer.append("1");
            } else {
                buffer.append("0");
            }
        }

        return buffer.toString();
    }

}
