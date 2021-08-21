package com.orainge.tools.apiservice.util.encryption;

/**
 * 加解密工具接口
 *
 * @author orainge
 * @date 2021/2/7
 */
public interface EncryptionUtil {
    /**
     * 加密文本
     *
     * @param str 待加密的文本
     * @param key 加密密钥
     * @return null: 加密失败; 非 null: 密文
     */
    String encrypt(String str, String key);

    /**
     * 解密文本
     *
     * @param str 待解密的文本
     * @param key 解密密钥
     * @return null: 解密失败; 非 null: 原文
     */
    String decrypt(String str, String key);
}
