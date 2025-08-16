package com.fastbee.coap.model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceStatusAge {

    public static final long MODULUS = (long) Math.pow(2, 24);
    private static final long THRESHOLD = (long) Math.pow(2, 23);

    private final long sequenceNo;
    private final long timestamp;

    public ResourceStatusAge(long sequenceNo, long timestamp) {
        this.sequenceNo = sequenceNo;
        this.timestamp = timestamp;
    }

    public static boolean isReceivedStatusNewer(ResourceStatusAge latest, ResourceStatusAge received) {
        if (latest.sequenceNo < received.sequenceNo && received.sequenceNo - latest.sequenceNo < THRESHOLD) {
            log.debug("Criterion 1 matches: received ({}) is newer than latest ({}).", received, latest);
            return true;
        }

        if (latest.sequenceNo > received.sequenceNo && latest.sequenceNo - received.sequenceNo > THRESHOLD) {
            log.debug("Criterion 2 matches: received  ({}) is newer than latest ({}).", received, latest);
            return true;
        }

        if (received.timestamp > latest.timestamp + 128000L) {
            log.debug("Criterion 3 matches: received ({}) is newer than latest ({}).", received, latest);
            return true;
        }

        log.debug("No criterion matches: received({}) is older than latest ({}).", received, latest);
        return false;
    }

    @Override
    public String toString() {
        return "STATUS AGE (Sequence No: " + this.sequenceNo + ", Reception Timestamp: " + this.timestamp + ")";
    }
}
