package com.fastbee.coap.model.linkformat;

import com.fastbee.coap.model.CoapMessage;
import com.fastbee.coap.model.options.StringOptionValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class LinkParam {

    /**
     * The enumeration {@link Key} contains all link-param-keys that are supported
     *
     * @author Oliver Kleine
     */
    public enum Key {
        /**
         * Corresponds to link-param-key "rel"
         */
        REL("rel", ValueType.RELATION_TYPE, ValueType.DQUOTED_RELATION_TYPES),

        /**
         * Corresponds to link-param-key "anchor"
         */
        ANCHOR("anchor", ValueType.DQUOTED_URI_REFERENCE),

        /**
         * Corresponds to link-param-key "rev"
         */
        REV("rev", ValueType.RELATION_TYPE),

        /**
         * Corresponds to link-param-key "hreflang"
         */
        HREFLANG("hreflang", ValueType.LANGUAGE_TAG),

        /**
         * Corresponds to link-param-key "media"
         */
        MEDIA("media", ValueType.MEDIA_DESC, ValueType.DQUOTED_MEDIA_DESC),

        /**
         * Corresponds to link-param-key "title"
         */
        TITLE("title", ValueType.DQUOTED_STRING),

        /**
         * Corresponds to link-param-key "title*"
         */
        TITLE_STAR("title*", ValueType.EXT_VALUE),

        /**
         * Corresponds to link-param-key "type"
         */
        TYPE("type", ValueType.MEDIA_TYPE, ValueType.DQUOTED_MEDIA_TYPE),

        /**
         * Corresponds to link-param-key "rt"
         */
        RT("rt", ValueType.RELATION_TYPE),

        /**
         * Corresponds to link-param-key "if"
         */
        IF("if", ValueType.RELATION_TYPE),

        /**
         * Corresponds to link-param-key "sz"
         */
        SZ("sz", ValueType.CARDINAL),

        /**
         * Corresponds to link-param-key "ct"
         */
        CT("ct", ValueType.CARDINAL, ValueType.DQUOTED_CARDINALS),

        /**
         * Corresponds to link-param-key "obs"
         */
        OBS("obs", ValueType.EMPTY),

        /**
         * Used internally for unknown link-param-keys
         */
        UNKNOWN(null, ValueType.UNKNOWN);

        private final String keyName;
        private final Set<ValueType> valueTypes;

        Key(String keyName, ValueType... valueType) {
            this.keyName = keyName;
            this.valueTypes = new HashSet<>(valueType.length);
            this.valueTypes.addAll(Arrays.asList(valueType));
        }

        /**
         * Returns the name of this link-param-key (i.e. "ct")
         * @return the name of this link-param-key (i.e. "ct")
         */
        public String getKeyName() {
            return this.keyName;
        }

        /**
         * Returns the {@link ValueType}s that are allowed for values of this key
         * @return the {@link ValueType}s that are allowed for values of this key
         */
        public Set<ValueType> getValueTypes() {
            return this.valueTypes;
        }
    }


    /**
     * The enumeration {@link ValueType} contains all value types that are supported
     *
     * @author Oliver Kleine
     */
    public enum ValueType {

        /**
         * Corresponds to the empty type, i.e. no value
         */
        EMPTY (false, false),

        /**
         * Corresponds to a single value of type "relation-types"
         */
        RELATION_TYPE (false, false),

        /**
         * Corresponds to one or more values of type "relation-types" enclosed in double-quotes (<code>DQUOTE</code>)
         */
        DQUOTED_RELATION_TYPES(true, true),

        /**
         * Corresponds to a single value of type "URI reference"
         */
        DQUOTED_URI_REFERENCE(true, false),

        /**
         * Corresponds to a single value of type "Language-Tag"
         */
        LANGUAGE_TAG (false, false),

        /**
         * Corresponds to a single value of type "Media Desc"
         */
        MEDIA_DESC (false, false),

        /**
         * Corresponds to a single value of type "Media Desc" enclosed in double-quotes (<code>DQUOTE</code>)
         */
        DQUOTED_MEDIA_DESC(true, false),

        /**
         * Corresponds to a single value of type "quoted-string", i.e. a string value enclosed in double-quotes
         * (<code>DQUOTE</code>)
         */
        DQUOTED_STRING(true, false),

        /**
         * Corresponds to a single value of type "ext-value"
         */
        EXT_VALUE (false, false),

        /**
         * Corresponds to a single value of type "media-type"
         */
        MEDIA_TYPE (false, false),

        /**
         * Corresponds to a single value of type "media-type" enclosed in double-quotes (<code>DQUOTE</code>)
         */
        DQUOTED_MEDIA_TYPE(true, false),

        /**
         * Corresponds to a single value of type "cardinal", i.e. digits
         */
        CARDINAL (false, false),

        /**
         * Values of this type consist of multiple cardinal values, divided by white spaces and enclosed in
         * double-quotes (<code>DQUOTE</code>)
         */
        DQUOTED_CARDINALS(true, true),

        /**
         * Internally used to represent all other types
         */
        UNKNOWN(false, false);

        private final boolean doubleQuoted;
        private final boolean multipleValues;

        ValueType(boolean doubleQuoted, boolean multipleValues) {
            this.doubleQuoted = doubleQuoted;
            this.multipleValues = multipleValues;
        }

        /**
         * Returns <code>true</code> if this {@link ValueType} allows multiple values divided by white spaces and
         * <code>false</code> otherwise
         *
         * @return <code>true</code> if this {@link ValueType} allows multiple values divided by white spaces and
         * <code>false</code> otherwise
         */
        public boolean isMultipleValues() {
            return this.multipleValues;
        }

        /**
         * Returns <code>true</code> if values of this {@link ValueType} are enclosed in double-quotes
         * (<code>DQUOTE</code>) and <code>false</code> otherwise
         *
         * @return <code>true</code> if values of this {@link ValueType} are enclosed in double-quotes
         * (<code>DQUOTE</code>) and <code>false</code> otherwise
         */
        public boolean isDoubleQuoted() {
            return this.doubleQuoted;
        }
    }


    /**
     * Returns the {@link Key} corresponding to the given name or <code>null</code> if no such {@link Key} exists
     *
     * @param keyName the name of the {@link Key} to lookup
     * @return the {@link Key} corresponding to the given name or <code>null</code> if no such {@link Key} exists
     */
    public static Key getKey(String keyName) {
        for(Key key : Key.values()) {
            if (key.getKeyName().equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Returns the {@link ValueType} that corresponds to the given key-value-pair or <code>null</code> if no such
     * {@link ValueType} exists.
     *
     * @param key the key
     * @param value the value
     *
     * @return the {@link ValueType} that corresponds to the given key-value-pair or <code>null</code> if no such
     * {@link ValueType} exists.
     */
    public static ValueType getValueType(Key key, String value) {
        // determine possible value types
        Set<ValueType> valueTypes = key.getValueTypes();

        // check if link param value is quoted and if there is quoted type
        if (valueTypes.size() == 1) {
            return valueTypes.iterator().next();
        } else if (value.startsWith("\"") && value.endsWith("\"")) {
            for (ValueType valueType : valueTypes) {
                if(valueType.isDoubleQuoted()) {
                    return valueType;
                }
            }
        } else {
            for (ValueType valueType : valueTypes) {
                if(!valueType.isDoubleQuoted()) {
                    return valueType;
                }
            }
        }

        return null;
    }

    /**
     * Decodes the given (serialized) link param (e.g. <code>ct=40</code>)
     * @param linkParam the serialized link param
     * @return an instance of {@link LinkParam} according to the given parameter
     */
    public static LinkParam decode(String linkParam) {
        // remove percent encoding
        byte[] tmp = StringOptionValue.convertToByteArrayWithoutPercentEncoding(linkParam);
        linkParam = new String(tmp, CoapMessage.CHARSET);

        // determine the key of this link param
        String keyName = !linkParam.contains("=") ? linkParam : linkParam.substring(0, linkParam.indexOf("="));
        LinkParam.Key key = LinkParam.getKey(keyName);

        if(key == null) {
            log.warn("Unsupported key name for link param: {}", keyName);
            return null;
        } else if (keyName.equals(linkParam)) {
            // empty attribute
            if(!key.getValueTypes().contains(ValueType.EMPTY)) {
                log.debug("Key {} does not support empty values!", key.getKeyName());
                return null;
            } else {
                return new LinkParam(key, ValueType.EMPTY, null);
            }
        } else {
            // link param has non-empty value
            String value = linkParam.substring(linkParam.indexOf("=") + 1, linkParam.length());
            LinkParam.ValueType valueType = LinkParam.getValueType(key, value);

            if(valueType == null) {
                log.warn("Could not determine value type for key \"{}\" and value\"{}\".", keyName, value);
                return null;
            } else {
                log.debug("Value: {}, Type: {}", value, valueType);
                return new LinkParam(key, valueType, value);
            }
        }
    }

    /**
     * <p>Creates a new instance of {@link LinkParam}</p>
     *
     * <p><b>Note:</b>For some kinds of link params the enclosing double quotes are part of the value (e.g. value "0 41"
     * for {@link Key#CT} or "Some title" for {@link Key#TITLE}). Thus, the latter is created using
     * <code>createLinkParam(Key.TITLE, "\"Some title\"")</code>
     * </p>
     *
     * @param key the {@link Key} of the link param to be created
     * @param value the value of the link param to be created (see note above)
     *
     * @return a new instance of {@link LinkParam} according to the given parameters (key and value)
     */
    public static LinkParam createLinkParam(Key key, String value) {
        ValueType valueType = getValueType(key, value);
        if (valueType == null) {
            log.warn("Could not determine value type for key \"{}\" and value\"{}\".", key.getKeyName(), value);
            return null;
        } else {
            return new LinkParam(key, valueType, value);
        }
    }

    //******************************************************************************************
    // instance related fields and methods
    //******************************************************************************************

    private final Key key;
    private final ValueType valueType;
    private final String value;


    private LinkParam(Key key, ValueType valueType, String value) {
        this.key = key;
        this.valueType = valueType;
        // remove double quotes if existing
        this.value = valueType.isDoubleQuoted() ? value.substring(1, value.length() - 1) : value;
        log.debug("LinkParam created: {}", this.toString());
    }


    /**
     * Returns the {@link Key} of this {@link LinkParam}
     * @return the {@link Key} of this {@link LinkParam}
     */
    public Key getKey() {
        return key;
    }

    /**
     * Shortcut for {@link #getKey()#getKeyName()}
     * @return the name of the {@link Key} of this {@link LinkParam} (e.g. "ct" or "rt")
     */
    public String getKeyName() {
        return this.key.getKeyName();
    }

    /**
     * Returns the {@link ValueType} of the value returned by {@link #getValue()}
     * @return the {@link ValueType} of the value returned by {@link #getValue()}
     */
    public ValueType getValueType() {
        return this.valueType;
    }

    /**
     * Returns the value of this {@link LinkParam}
     * @return the value of this {@link LinkParam}
     */
    public String getValue() {
        if (this.valueType.isDoubleQuoted()) {
            return "\"" + this.value + "\"";
        } else {
            return this.value;
        }
    }

    /**
     * <p>Returns <code>true</code> if the given value is contained in the value returned by {@link #getValue()} and
     * <code>false</code> otherwise. The exact behaviour depends on whether there are multiple values allowed in a
     * single param (see: {@link ValueType#isMultipleValues()}).</p>
     *
     * <p>Example: If the {@link LinkParam} corresponds to <code>ct="0 41"</code> then both, <code>contains("0")</code>
     * and <code>contains("41")</code> return <code>true</code> but <code>contains("0 41")</code> returns
     * <code>false</code>.</p>
     *
     * @param value the value to check
     *
     * @return <code>true</code> if the given value is contained in the value returned by {@link #getValue()} and
     * <code>false</code> otherwise.
     */
    public boolean contains(String value) {
        if (this.valueType.isMultipleValues()){
            return Arrays.asList(this.value.split(" ")).contains(value);
        } else {
            return this.value.equals(value);
        }
    }

    /**
     * Returns a string representation of this {@link LinkParam}
     * @return a string representation of this {@link LinkParam}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.key.getKeyName());
        if (this.valueType != ValueType.EMPTY) {
            builder.append("=");
            if (this.valueType.doubleQuoted) {
                builder.append("\"");
            }
            builder.append(this.value);
            if (this.valueType.doubleQuoted) {
                builder.append("\"");
            }
        }
        return builder.toString();
    }
}
