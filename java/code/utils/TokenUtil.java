package com.technology.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * openApi-token样例
 */
@Slf4j
public class TokenUtil {

    /**
     * 根据请求参数和timestamp计算签名哈希值
     *
     * @param apiSecret
     * @param nonce     调用方生成的随机数
     * @param timestamp
     * @return
     */
    public static String getSignHash(String apiSecret, String nonce, long timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(nonce);
        sb.append(timestamp);
        return safeB64encode(hmacSha1(sb.toString(), apiSecret));
    }

    private static String safeB64encode(byte[] str) {
        String encode = Base64.getEncoder().encodeToString(str);
        return encode.replace('+', '-').replace('/', '_').replace('=', '.');
    }

    private static byte[] hmacSha1(String value, String key) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return rawHmac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
