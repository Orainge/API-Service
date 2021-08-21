package com.orainge.tools.apiservice.util.encryption;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES加密工具 (默认加密工具)
 *
 * @date 2020/10/22
 */
@Component
@ConditionalOnMissingBean(EncryptionUtil.class)
@Slf4j
public class AESEncryptionUtil implements EncryptionUtil {
    @Override
    public String encrypt(String str, String key) {
        try {
            return aesEncrypt(str, key);
        } catch (Exception e) {
            log.error("[AES加密工具] - 加密错误", e);
            return null;
        }
    }

    @Override
    public String decrypt(String str, String key) {
        try {
            return aesDecrypt(str, key);
        } catch (Exception e) {
            log.error("[AES加密工具] - 解密密错误", e);
            return null;
        }
    }

    /**
     * AES 加密
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] encryptBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encryptBytes);
    }

    /**
     * AES 解密
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes());
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(Base64.decodeBase64(encryptStr));
        return new String(decryptBytes, StandardCharsets.UTF_8);
    }
}