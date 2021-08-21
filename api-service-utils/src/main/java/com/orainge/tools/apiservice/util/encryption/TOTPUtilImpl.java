package com.orainge.tools.apiservice.util.encryption;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * 动态验证码生成工具类
 */
@Component
@ConditionalOnMissingBean(TOTPUtil.class)
public class TOTPUtilImpl implements TOTPUtil {
    /**
     * 时间步长 单位:毫秒 作为口令变化的时间周期
     */
    static final long STEP = 30000;

    /**
     * 转码位数 [1-8]
     */
    static final int CODE_DIGITS = 8;

    /**
     * 初始化时间
     */
    static final long INITIAL_TIME = 0;

    /**
     * 数子量级
     */
    static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * 生成动态验证码
     *
     * @param id        用户标识
     * @param secretKey 预共享密钥
     * @return String 动态验证码
     */
    public String generate(String id, String secretKey) {
        long now = new Date().getTime();
        String time = Long.toHexString(timeFactor(now)).toUpperCase();
        return generateTOTP(id + secretKey, time);
    }

    /**
     * 柔性验证码验证
     *
     * @param id        用户标识
     * @param secretKey 预共享密钥
     * @param code      待验证的验证码
     * @return boolean
     */
    public boolean verify(String id, String secretKey, String code) {
        return generate(id, secretKey).equals(code);
    }

    /**
     * 获取动态因子
     *
     * @param targetTime 指定时间
     * @return long
     */
    private long timeFactor(long targetTime) {
        return (targetTime - INITIAL_TIME) / STEP;
    }

    private String generateTOTP(String key, String time) {
        return generateTOTP(key, time, "HmacSHA1");
    }

    private String generateTOTP256(String key, String time) {
        return generateTOTP(key, time, "HmacSHA256");
    }

    private String generateTOTP512(String key, String time) {
        return generateTOTP(key, time, "HmacSHA512");
    }

    private String generateTOTP(String key, String time, String crypto) {
        StringBuilder timeBuilder = new StringBuilder(time);
        while (timeBuilder.length() < 16)
            timeBuilder.insert(0, "0");
        time = timeBuilder.toString();

        byte[] bArray = new BigInteger("10" + time, 16).toByteArray();
        byte[] msg = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, msg, 0, msg.length);

        byte[] k = key.getBytes();
        byte[] hash = hmac_sha(crypto, k, msg); // 20字节的字符串

        // 截断函数
        StringBuilder result;
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[CODE_DIGITS];
        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < CODE_DIGITS) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    /**
     * 哈希加密
     *
     * @param crypto   加密算法
     * @param keyBytes 密钥数组
     * @param text     加密内容
     * @return byte[]
     */
    private byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "AES");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}