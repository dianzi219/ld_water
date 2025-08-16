
package com.fastbee.coap.model.options;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Slf4j
public final class EmptyOptionValue extends OptionValue<Void> {

    /**
     * @param optionNumber the option number of the {@link EmptyOptionValue} to be created
     *
     * @throws java.lang.IllegalArgumentException if the given option number does not refer to an empty option
     */
    public EmptyOptionValue(int optionNumber) throws IllegalArgumentException {
       super(optionNumber, new byte[0], false);
        log.debug("Empty Option (#{}) created.", optionNumber);
    }

    /**
     * Returns <code>null</code>
     * @return <code>null</code>
     */
    @Override
    public Void getDecodedValue() {
        return null;
    }


    /**
     * Returns <code>0</code>
     * @return <code>0</code>
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Checks if a given {@link Object} equals this {@link EmptyOptionValue} instance. A given {@link Object} equals
     * this {@link EmptyOptionValue} if and only if the {@link Object} is an instance of {@link EmptyOptionValue}.
     *
     * @param object the object to check for equality with this instance of {@link EmptyOptionValue}
     *
     * @return <code>true</code> if the given {@link Object} is an instance of {@link EmptyOptionValue} and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EmptyOptionValue))
            return false;

        EmptyOptionValue other = (EmptyOptionValue) object;
        return Arrays.equals(this.getValue(), other.getValue());
    }
}
