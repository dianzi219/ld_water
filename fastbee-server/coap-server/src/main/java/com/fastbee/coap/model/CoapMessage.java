package com.fastbee.coap.model;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Longs;
import com.fastbee.coap.model.options.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.fastbee.coap.model.MessageCode.EMPTY;
import static com.fastbee.coap.model.MessageType.*;

@Slf4j
@Data
public abstract class CoapMessage {

    /**
     * The CoAP protocol version (1)
     */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * The default character set for {@link CoapMessage}s (UTF-8)
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Internal constant to indicate that the message ID was not yet set (-1)
     */
    public static final int UNDEFINED_MESSAGE_ID = -1;

    /**
     * The maximum length of the byte array that backs the {@link Token} of {@link CoapMessage} (8)
     */
    public static final int MAX_TOKEN_LENGTH = 8;

    private static final String WRONG_OPTION_TYPE = "Option no. %d is no option of type %s";
    private static final String OPTION_NOT_ALLOWED_WITH_MESSAGE_TYPE = "Option no. %d (%s) is not allowed with " +
            "message type %s";
    private static final String OPTION_ALREADY_SET = "Option no. %d is already set and is only allowed once per " +
            "message";

    private static final String DOES_NOT_ALLOW_CONTENT = "CoAP messages with code %s do not allow payload.";
    private static final String EXCLUDES = "Already contained option no. %d excludes option no. %d";


    private int messageType;
    private int messageCode;
    private int messageID;
    private Token token;
    InetSocketAddress sender;

    protected SetMultimap<Integer, OptionValue> options;
    private ByteBuf content;

    protected CoapMessage(int messageType, int messageCode, int messageID, Token token)
            throws IllegalArgumentException {

        if (!MessageType.isMessageType(messageType))
            throw new IllegalArgumentException("No. " + messageType + " is not corresponding to any message type.");

        if (!MessageCode.isMessageCode(messageCode))
            throw new IllegalArgumentException("No. " + messageCode + " is not corresponding to any message code.");

        this.setMessageType(messageType);
        this.setMessageCode(messageCode);

        log.debug("Set Message Code to {} ({}).", MessageCode.asString(messageCode), messageCode);

        this.setMessageID(messageID);
        this.setToken(token);

        this.options = Multimaps.newSetMultimap(new TreeMap<>(),
                LinkedHashSetSupplier.getInstance());

        this.content = ByteBufAllocator.DEFAULT.buffer();

        log.debug("Created CoAP message: {}", this);
    }

    protected CoapMessage(int messageType, int messageCode) throws IllegalArgumentException {
        this(messageType, messageCode, UNDEFINED_MESSAGE_ID, new Token(new byte[0]));
    }

    public static CoapMessage createEmptyReset(int messageID) throws IllegalArgumentException {
        return new CoapMessage(RST, EMPTY, messageID, new Token(new byte[0])) {};
    }

    public static CoapMessage createEmptyAcknowledgement(int messageID) throws IllegalArgumentException {
        return new CoapMessage(ACK, EMPTY, messageID, new Token(new byte[0])) {};
    }

    public static CoapMessage createPing(int messageID) throws IllegalArgumentException{
        return new CoapMessage(CON, EMPTY, messageID, new Token(new byte[0])) {};
    }

    public void setMessageType(int messageType) throws IllegalArgumentException {
        if (!MessageType.isMessageType(messageType))
            throw new IllegalArgumentException("Invalid message type (" + messageType +
                    "). Only numbers 0-3 are allowed.");

        this.messageType = messageType;
    }

    public boolean isPing() {
        return this.messageCode == EMPTY && this.messageType == CON;
    }

    public boolean isRequest() {
        return MessageCode.isRequest(this.getMessageCode());
    }

    public boolean isResponse() {
        return MessageCode.isResponse(this.getMessageCode());
    }

    public void addOption(int optionNumber, OptionValue optionValue) throws IllegalArgumentException {
        this.checkOptionPermission(optionNumber);

        for(int containedOption : options.keySet()) {
            if (Option.mutuallyExcludes(containedOption, optionNumber))
                throw new IllegalArgumentException(String.format(EXCLUDES, containedOption, optionNumber));
        }

        options.put(optionNumber, optionValue);

        log.debug("Added option (number: {}, value: {})", optionNumber, optionValue.toString());

    }

    protected void addStringOption(int optionNumber, String value) throws IllegalArgumentException {

        if (!(OptionValue.getType(optionNumber) == OptionValue.Type.STRING))
            throw new IllegalArgumentException(String.format(WRONG_OPTION_TYPE, optionNumber, OptionValue.Type.STRING));

        //Add new option to option list
        StringOptionValue option = new StringOptionValue(optionNumber, value);
        addOption(optionNumber, option);
    }

    protected void addUintOption(int optionNumber, long value) throws IllegalArgumentException {

        if (!(OptionValue.getType(optionNumber) == OptionValue.Type.UINT))
            throw new IllegalArgumentException(String.format(WRONG_OPTION_TYPE, optionNumber, OptionValue.Type.STRING));

        //Add new option to option list
        byte[] byteValue = Longs.toByteArray(value);
        int index = 0;
        while(index < byteValue.length && byteValue[index] == 0) {
            index++;
        }
        UintOptionValue option = new UintOptionValue(optionNumber, Arrays.copyOfRange(byteValue, index, byteValue.length));
        addOption(optionNumber, option);

    }

    protected void addOpaqueOption(int optionNumber, byte[] value) throws IllegalArgumentException {

        if (!(OptionValue.getType(optionNumber) == OptionValue.Type.OPAQUE))
            throw new IllegalArgumentException(String.format(WRONG_OPTION_TYPE, optionNumber, OptionValue.Type.OPAQUE));

        //Add new option to option list
        OpaqueOptionValue option = new OpaqueOptionValue(optionNumber, value);
        addOption(optionNumber, option);

    }

    protected void addEmptyOption(int optionNumber) throws IllegalArgumentException {

        if (!(OptionValue.getType(optionNumber) == OptionValue.Type.EMPTY))
            throw new IllegalArgumentException(String.format(WRONG_OPTION_TYPE, optionNumber, OptionValue.Type.EMPTY));

        //Add new option to option list
        options.put(optionNumber, new EmptyOptionValue(optionNumber));

        log.debug("Added empty option (number: {})", optionNumber);
    }

    public void removeOptions(int optionNumber) {
        int result = options.removeAll(optionNumber).size();
        log.debug("Removed {} options with number {}.", result, optionNumber);
    }

    private void checkOptionPermission(int optionNumber) throws IllegalArgumentException {
        Option.Occurence permittedOccurence = Option.getPermittedOccurrence(optionNumber, this.messageCode);
        if (permittedOccurence == Option.Occurence.NONE) {
            throw new IllegalArgumentException(String.format(OPTION_NOT_ALLOWED_WITH_MESSAGE_TYPE,
                    optionNumber, Option.asString(optionNumber), this.getMessageCodeName()));
        } else if (options.containsKey(optionNumber) && permittedOccurence == Option.Occurence.ONCE) {
                throw new IllegalArgumentException(String.format(OPTION_ALREADY_SET, optionNumber));
        }
    }

    private static long extractBits(final long value, final int bits, final int offset) {
        final long shifted = value >>> offset;
        final long masked = (1L << bits) - 1L;
        return shifted & masked;
    }


    public int getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    public String getMessageTypeName() {
        return MessageType.asString(this.messageType);
    }

    public String getMessageCodeName() {
        return MessageCode.asString(this.messageCode);
    }

    public long getContentFormat() {
        if (options.containsKey(Option.CONTENT_FORMAT)) {
            return ((UintOptionValue) options.get(Option.CONTENT_FORMAT).iterator().next()).getDecodedValue();
        } else {
            return ContentFormat.UNDEFINED;
        }
    }

    public void setObserve(long value) {
        try {
            this.removeOptions(Option.OBSERVE);
            value = value & 0xFFFFFF;
            this.addUintOption(Option.OBSERVE, value);
        }
        catch (IllegalArgumentException e) {
            this.removeOptions(Option.OBSERVE);
            log.error("This should never happen.", e);
        }
    }

    public long getObserve() {
        if (!options.containsKey(Option.OBSERVE)) {
            return UintOptionValue.UNDEFINED;
        } else {
            return (long) options.get(Option.OBSERVE).iterator().next().getDecodedValue();
        }
    }

    public void setSize2(long size2) throws IllegalArgumentException{
        this.options.removeAll(Option.SIZE_2);
        this.addUintOption(Option.SIZE_2, size2);
    }

    public long getSize2() {
        if (options.containsKey(Option.SIZE_2)) {
            return ((UintOptionValue) options.get(Option.SIZE_2).iterator().next()).getDecodedValue();
        } else {
            return UintOptionValue.UNDEFINED;
        }
    }

    public void setSize1(long size1) throws IllegalArgumentException{
        this.options.removeAll(Option.SIZE_1);
        this.addUintOption(Option.SIZE_1, size1);
    }

    public long getSize1() {
        if (options.containsKey(Option.SIZE_1)) {
            return ((UintOptionValue) options.get(Option.SIZE_1).iterator().next()).getDecodedValue();
        } else {
            return UintOptionValue.UNDEFINED;
        }
    }

    public byte[] getEndpointID1() {
        Set<OptionValue> values = getOptions(Option.ENDPOINT_ID_1);
        if (values.isEmpty()) {
            return null;
        } else {
            return values.iterator().next().getValue();
        }
    }

    public void setEndpointID1() {
        this.setEndpointID1(new byte[0]);
    }

    public void setEndpointID1(byte[] value) {
        try {
            this.removeOptions(Option.ENDPOINT_ID_1);
            this.addOpaqueOption(Option.ENDPOINT_ID_1, value);
        } catch (IllegalArgumentException e) {
            this.removeOptions(Option.ENDPOINT_ID_1);
            log.error("This should never happen.", e);
        }
    }

    public byte[] getEndpointID2() {
        Set<OptionValue> values = getOptions(Option.ENDPOINT_ID_2);
        if (values.isEmpty()) {
            return null;
        } else {
            return values.iterator().next().getValue();
        }
    }

    public void setEndpointID2(byte[] value) {
        try {
            this.removeOptions(Option.ENDPOINT_ID_2);
            this.addOpaqueOption(Option.ENDPOINT_ID_2, value);
        } catch (IllegalArgumentException e) {
            this.removeOptions(Option.ENDPOINT_ID_2);
            log.error("This should never happen.", e);
        }
    }

    public void setContent(ByteBuf content) throws IllegalArgumentException {

        if (!(MessageCode.allowsContent(this.messageCode)) && content.readableBytes() > 0) {
            throw new IllegalArgumentException(String.format(DOES_NOT_ALLOW_CONTENT, this.getMessageCodeName()));
        }

        this.content = content;
    }

    public void setContent(ByteBuf content, long contentFormat) throws IllegalArgumentException {

        try {
            this.addUintOption(Option.CONTENT_FORMAT, contentFormat);
            setContent(content);
        } catch (IllegalArgumentException e) {
            this.content = ByteBufAllocator.DEFAULT.buffer();
            this.removeOptions(Option.CONTENT_FORMAT);
            throw e;
        }
    }

    public void setContent(byte[] content) throws IllegalArgumentException {
        setContent(Unpooled.wrappedBuffer(content));
    }

    public void setContent(byte[] content, long contentFormat) throws IllegalArgumentException {
        setContent(Unpooled.wrappedBuffer(content), contentFormat);
    }

    public ByteBuf getContent() {
        return this.content;
    }

    public byte[] getContentAsByteArray() {
        byte[] result = new byte[this.getContentLength()];
        this.getContent().readBytes(result, 0, this.getContentLength());
        return result;
    }

    public int getContentLength() {
        return this.content.readableBytes();
    }

    public SetMultimap<Integer, OptionValue> getAllOptions() {
        return this.options;
    }

    public void setAllOptions (SetMultimap<Integer, OptionValue> options) {
        this.options = options;
    }

    public Set<OptionValue> getOptions(int optionNumber) {
        return this.options.get(optionNumber);
    }

    public boolean containsOption(int optionNumber) {
        return !getOptions(optionNumber).isEmpty();
    }

    @Override
    public int hashCode() {
        return toString().hashCode() + content.hashCode();
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof CoapMessage)) {
            log.error("Different type");
            return false;
        }

        CoapMessage other = (CoapMessage) object;

        //Check header fields
        if (this.getProtocolVersion() != other.getProtocolVersion())
            return false;

        if (this.getMessageType() != other.getMessageType())
            return false;

        if (this.getMessageCode() != other.getMessageCode())
            return false;

        if (this.getMessageID() != other.getMessageID())
            return false;

        if (!this.getToken().equals(other.getToken()))
            return false;


        //Iterators iterate over the contained options
        Iterator<Map.Entry<Integer, OptionValue>> iterator1 = this.getAllOptions().entries().iterator();
        Iterator<Map.Entry<Integer, OptionValue>> iterator2 = other.getAllOptions().entries().iterator();

        //Check if both CoAP Messages contain the same options in the same order
        while(iterator1.hasNext()) {

            //Check if iterator2 has no more options while iterator1 has at least one more
            if (!iterator2.hasNext())
                return false;

            Map.Entry<Integer, OptionValue> entry1 = iterator1.next();
            Map.Entry<Integer, OptionValue> entry2 = iterator2.next();

            if (!entry1.getKey().equals(entry2.getKey()))
                return false;

            if (!entry1.getValue().equals(entry2.getValue()))
                return false;
        }

        //Check if iterator2 has at least one more option while iterator1 has no more
        if (iterator2.hasNext())
            return false;

        //Check content
        return this.getContent().equals(other.getContent());
    }


    @Override
    public String toString() {

        StringBuilder result =  new StringBuilder();
        //Header + Token
        result.append("[Header: (V) ").append(getProtocolVersion())
                .append(", (T) ").append(getMessageTypeName())
                .append(", (TKL) ").append(token.getBytes().length)
                .append(", (C) ").append(getMessageCodeName())
                .append(", (ID) ").append(getMessageID())
                .append(" | (Token) ").append(token).append(" | ");
        //Options
        result.append("Options:");
        for(int optionNumber : getAllOptions().keySet()) {
            result.append(" (No. ").append(optionNumber).append(") ");
            Iterator<OptionValue> iterator = this.getOptions(optionNumber).iterator();
            OptionValue<?> optionValue = iterator.next();
            result.append(optionValue.toString());
            while(iterator.hasNext())
                result.append(" / ").append(iterator.next().toString());
        }
        result.append(" | ");

        //Content
        result.append("Content: ");
        long payloadLength = getContent().readableBytes();
        if (payloadLength == 0)
            result.append("<no content>]");
        else
            result.append(getContent().toString(0, Math.min(getContent().readableBytes(), 20), CoapMessage.CHARSET)).append("... ( ").append(payloadLength).append(" bytes)]");
        return result.toString();
    }

    public void setMessageCode(int messageCode) throws IllegalArgumentException {
        if (!MessageCode.isMessageCode(messageCode))
            throw new IllegalArgumentException("Invalid message code no. " + messageCode);

        this.messageCode = messageCode;
    }

    private final static class LinkedHashSetSupplier implements Supplier<LinkedHashSet<OptionValue>> {

        public static LinkedHashSetSupplier instance = new LinkedHashSetSupplier();

        private LinkedHashSetSupplier() {}

        public static LinkedHashSetSupplier getInstance() {
            return instance;
        }

        @Override
        public LinkedHashSet<OptionValue> get() {
            return new LinkedHashSet<>();
        }
    }
}
