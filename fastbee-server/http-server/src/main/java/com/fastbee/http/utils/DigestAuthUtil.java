package com.fastbee.http.utils;

import gov.nist.core.InternalErrorHandler;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the HTTP digest authentication method server side functionality.
 */

@Slf4j
public class DigestAuthUtil {

    private final MessageDigest messageDigest;
    public static final String DEFAULT_ALGORITHM = "MD5";
    public static final String DEFAULT_SCHEME = "Digest";
    /** to hex converter */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Default constructor.
     * @throws NoSuchAlgorithmException
     */
    public DigestAuthUtil()
            throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }

    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length * 2];
        for (byte value : b) {
            c[pos++] = toHex[(value >> 4) & 0x0F];
            c[pos++] = toHex[value & 0x0f];
        }
        return new String(c);
    }

    /**
     * Generate the challenge string.
     *
     * @return a generated nonce.
     */
    private String generateNonce() {
        // Get the time of day and run MD5 over it.
        Date date = new Date();
        long time = date.getTime();
        Random rand = new Random();
        long pad = rand.nextLong();
        String nonceString = (new Long(time)).toString()
                + (new Long(pad)).toString();
        byte[] mdbytes = messageDigest.digest(nonceString.getBytes());
        // Convert the mdbytes array into a hex string.
        return toHexString(mdbytes);
    }

    public FullHttpResponse generateChallenge(String realm) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED,
                    Unpooled.copiedBuffer("Unauthorized\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE,
                    "Digest realm=\"" + realm + "\", nonce=\"" + generateNonce()
                            + "\", opaque=\"\", stale=\"FALSE\", algorithm=\"" + DEFAULT_ALGORITHM + "\"");
            return response;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
        return null;
    }
    /**
     * Authenticate the inbound request.
     *
     * @param request - the request to authenticate.
     * @param hashedPassword -- the MD5 hashed string of username:realm:plaintext password.
     *
     * @return true if authentication succeded and false otherwise.
     */
    public boolean doAuthenticateHashedPassword(FullHttpRequest request, String hashedPassword) {
        String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if ( authHeader == null ) return false;
        Map<String, String> params = parseDigestParameters(authHeader);
        String realm = params.get("realm");
        String username = params.get("username");

        if ( username == null || realm == null ) {
            return false;
        }

        String nonce = params.get("nonce");
        String uri = params.get("uri");
        if (uri == null) {
            return false;
        }

        String A2 = request.method() + ":" + uri;

        byte[] mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);

        String cnonce = params.get("cnonce");
        String KD = hashedPassword + ":" + nonce;
        if (cnonce != null) {
            KD += ":" + cnonce;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = params.get("response");


        return mdString.equals(response);
    }

    /**
     * Authenticate the inbound request given plain text password.
     *
     * @param request - the request to authenticate.
     * @param pass -- the plain text password.
     *
     * @return true if authentication succeded and false otherwise.
     */
    public boolean doAuthenticatePlainTextPassword(FullHttpRequest request, String pass) {

        String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if ( authHeader == null ) return false;
        Map<String, String> params = parseDigestParameters(authHeader);
        String realm = params.get("realm");
        String username = params.get("username");

        String nonce = params.get("nonce");
        String uri = params.get("uri");
        if (uri == null) {
            return false;
        }
        // qop 保护质量 包含auth（默认的）和auth-int（增加了报文完整性检测）两种策略
        String qop = params.get("qop");

        // nonce计数器，是一个16进制的数值，表示同一nonce下客户端发送出请求的数量
        int nc = Integer.parseInt(params.get("nc"));
        String ncStr = new DecimalFormat("00000000").format(nc);

        String A1 = username + ":" + realm + ":" + pass;
        String A2 = request.method() + ":" + uri;
        byte[] mdbytes = messageDigest.digest(A1.getBytes());
        String HA1 = toHexString(mdbytes);


        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);
        String cnonce = params.get("cnonce");
        String KD = HA1 + ":" + nonce;

        if (qop != null && qop.equals("auth") ) {
            if (nc != -1) {
                KD += ":" + ncStr;
            }
            if (cnonce != null) {
                KD += ":" + cnonce;
            }
            KD += ":" + qop;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = params.get("response");
        return mdString.equals(response);

    }

    private Map<String, String> parseDigestParameters(String authHeader) {
        Map<String, String> params = new HashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^,]+))");
        Matcher matcher = pattern.matcher(authHeader);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            params.put(key, value);
        }

        return params;
    }
}

