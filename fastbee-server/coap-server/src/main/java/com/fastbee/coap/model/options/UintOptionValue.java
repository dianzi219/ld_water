
package com.fastbee.coap.model.options;

import lombok.extern.slf4j.Slf4j;
import java.math.BigInteger;
import java.util.Arrays;

@Slf4j
public class UintOptionValue extends OptionValue<Long> {

    /**
     * Corresponds to a value of <code>-1</code> to indicate that there is no value for that option set.
     */
    public static final long UNDEFINED = -1;

    public UintOptionValue(int optionNumber, byte[] value) throws IllegalArgumentException {
        this(optionNumber, shortenValue(value), false);
    }

    public UintOptionValue(int optionNumber, byte[] value, boolean allowDefault) throws IllegalArgumentException {
        super(optionNumber, shortenValue(value), allowDefault);
        log.debug("Uint Option (#{}) created with value: {}", optionNumber, this.getDecodedValue());
    }

    @Override
    public Long getDecodedValue() {
        return new BigInteger(1, value).longValue();
    }

    @Override
    public int hashCode() {
        return getDecodedValue().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UintOptionValue))
            return false;

        UintOptionValue other = (UintOptionValue) object;
        return Arrays.equals(this.getValue(), other.getValue());
    }

    public static byte[] shortenValue(byte[] value) {
        int index = 0;
        while(index < value.length - 1 && value[index] == 0)
            index++;
        return Arrays.copyOfRange(value, index, value.length);
    }

}
