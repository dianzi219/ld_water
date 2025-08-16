package com.fastbee.sip.server;

import gov.nist.javax.sip.parser.MessageParser;
import gov.nist.javax.sip.parser.MessageParserFactory;
import gov.nist.javax.sip.stack.SIPTransactionStack;

public class GbMsgParserFactory implements MessageParserFactory {


    private static GBMsgParser msgParser = new GBMsgParser();

    public MessageParser createMessageParser(SIPTransactionStack stack) {
        return msgParser;
    }
}
