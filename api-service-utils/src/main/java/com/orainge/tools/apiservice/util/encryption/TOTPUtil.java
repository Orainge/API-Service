package com.orainge.tools.apiservice.util.encryption;

/**
 * 动态验证码生成工具类
 *
 * @author orainge
 * @date 2021/2/22
 */
public interface TOTPUtil {
    /**
     * 生成动态验证码
     *
     * @param id        用户标识
     * @param secretKey 预共享密钥
     * @return String 动态验证码
     */
    String generate(String id, String secretKey);

    /**
     * 验证码验证
     *
     * @param id        用户标识
     * @param secretKey 预共享密钥
     * @param code      待验证的验证码
     * @return boolean
     */
    boolean verify(String id, String secretKey, String code);
}