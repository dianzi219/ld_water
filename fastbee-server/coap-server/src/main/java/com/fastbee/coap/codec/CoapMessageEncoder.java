package com.fastbee.coap.codec;

import com.google.common.primitives.Ints;
import com.fastbee.coap.model.CoapMessage;
import com.fastbee.coap.model.MessageCode;
import com.fastbee.coap.model.options.OptionValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@ChannelHandler.Sharable
@Slf4j
public class CoapMessageEncoder extends MessageToMessageEncoder<CoapMessage> {
    /**
     * The maximum option delta (65804)
     */
    public static final int MAX_OPTION_DELTA = 65804;

    /**
     * The maximum option length (65804)
     */
    public static final int MAX_OPTION_LENGTH = 65804;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, CoapMessage coapMessage, List<Object> list) throws Exception {
        log.info("CoapMessage to be encoded: {}", coapMessage);
        // start encoding
        ByteBuf encodedMessage = ByteBufAllocator.DEFAULT.buffer();
        // encode HEADER and TOKEN
        encodeHeader(encodedMessage, coapMessage);
        log.debug("Encoded length of message (after HEADER + TOKEN): {}", encodedMessage.readableBytes());

        if (coapMessage.getMessageCode() == MessageCode.EMPTY) {
            encodedMessage = Unpooled.wrappedBuffer(Ints.toByteArray(encodedMessage.getInt(0) & 0xF0FFFFFF));
            DatagramPacket datagramPacket = new DatagramPacket(encodedMessage, coapMessage.getSender());
            list.add(datagramPacket);
            return;
        }

        if (coapMessage.getAllOptions().size() == 0 && coapMessage.getContent().readableBytes() == 0) {
            DatagramPacket datagramPacket = new DatagramPacket(encodedMessage, coapMessage.getSender());
            list.add(datagramPacket);
            return;
        }

        // encode OPTIONS (if any)
        encodeOptions(encodedMessage, coapMessage);
        log.debug("Encoded length of message (after OPTIONS): {}", encodedMessage.readableBytes());

        // encode payload (if any)
        if (coapMessage.getContent().readableBytes() > 0) {
            // add END-OF-OPTIONS marker only if there is payload
            encodedMessage.writeByte(255);

            // add payload
            encodedMessage = Unpooled.wrappedBuffer(encodedMessage, coapMessage.getContent());
            log.debug("Encoded length of message (after CONTENT): {}", encodedMessage.readableBytes());
        }

        //发送响应
        DatagramPacket datagramPacket = new DatagramPacket(encodedMessage, coapMessage.getSender());
        list.add(datagramPacket);
    }
    protected void encodeHeader(ByteBuf buffer, CoapMessage coapMessage) {

        byte[] token = coapMessage.getToken().getBytes();

        int encodedHeader = ((coapMessage.getProtocolVersion()  & 0x03)     << 30)
                | ((coapMessage.getMessageType()      & 0x03)     << 28)
                | ((token.length                      & 0x0F)     << 24)
                | ((coapMessage.getMessageCode()      & 0xFF)     << 16)
                | ((coapMessage.getMessageID()        & 0xFFFF));

        buffer.writeInt(encodedHeader);

        if (log.isDebugEnabled()) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(encodedHeader));
            while(binary.length() < 32) {
                binary.insert(0, "0");
            }
            log.debug("Encoded Header: {}", binary.toString());
        }

        //Write token
        if (token.length > 0) {
            buffer.writeBytes(token);
        }
    }


    protected void encodeOptions(ByteBuf buffer, CoapMessage coapMessage) throws OptionCodecException {

        //Encode options one after the other and append buf option to the buf
        int previousOptionNumber = 0;

        for(int optionNumber : coapMessage.getAllOptions().keySet()) {
            for(OptionValue optionValue : coapMessage.getOptions(optionNumber)) {
                encodeOption(buffer, optionNumber, optionValue, previousOptionNumber);
                previousOptionNumber = optionNumber;
            }
        }
    }


    protected void encodeOption(ByteBuf buffer, int optionNumber, OptionValue optionValue, int prevNumber)
            throws OptionCodecException {

        //The previous option number must be smaller or equal to the actual one
        if (prevNumber > optionNumber) {
            log.error("Previous option no. ({}) must not be larger then current option no ({})",
                    prevNumber, optionNumber);
            throw new OptionCodecException(optionNumber);
        }

        int optionDelta = optionNumber - prevNumber;
        int optionLength = optionValue.getValue().length;

        if (optionLength > MAX_OPTION_LENGTH) {
            log.error("Option no. {} exceeds maximum option length (actual: {}, max: {}).",
                    optionNumber, optionLength, MAX_OPTION_LENGTH);

            throw new OptionCodecException(optionNumber);
        }

        if (optionDelta > MAX_OPTION_DELTA) {
            log.error("Option delta exceeds maximum option delta (actual: {}, max: {})", optionDelta, MAX_OPTION_DELTA);
            throw new OptionCodecException(optionNumber);
        }

        if (optionDelta < 13) {
            //option delta < 13
            if (optionLength < 13) {
                buffer.writeByte(((optionDelta & 0xFF) << 4) | (optionLength & 0xFF));
            } else if (optionLength < 269) {
                buffer.writeByte(((optionDelta << 4) & 0xFF) | (13 & 0xFF));
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte(((optionDelta << 4) & 0xFF) | (14 & 0xFF));
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        } else if (optionDelta < 269) {
            //13 <= option delta < 269
            if (optionLength < 13) {
                buffer.writeByte(((13 & 0xFF) << 4) | (optionLength & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
            } else if (optionLength < 269) {
                buffer.writeByte(((13 & 0xFF) << 4) | (13 & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte((13 & 0xFF) << 4 | (14 & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        } else {
            //269 <= option delta < 65805
            if (optionLength < 13) {
                buffer.writeByte(((14 & 0xFF) << 4) | (optionLength & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
            } else if (optionLength < 269) {
                buffer.writeByte(((14 & 0xFF) << 4) | (13 & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte(((14 & 0xFF) << 4) | (14 & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        }

        //Write option value
        buffer.writeBytes(optionValue.getValue());
        log.debug("Encoded option no {} with value {}", optionNumber, optionValue.getDecodedValue());
        log.debug("Encoded message length is now: {}", buffer.readableBytes());
    }
}
