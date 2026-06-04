package com.jhl.silver.union;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author: qingren
 * @create_time: 2025/9/23
 */
public class TestMain {
    private static final String KEY_ALGORITHM = "AES";

    /**
     * 生成加密秘钥
     */
    private static SecretKeySpec getSecretKey(final String key) {
        // 返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg;
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes());
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            // AES 要求密钥长度为 128
            kg.init(128, random);
            // 生成一个密钥
            SecretKey secretKey = kg.generateKey();
            // 转换为AES专用密钥
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("AESUtil.getSecretKey-error:" + e);
        }
        return null;
    }

    public static void main(String[] args) {
        String key = "7398b9ed-6e25-4ce0-a76e-01178fa8ad9a";
        SecretKeySpec sks = getSecretKey(key);
        System.out.println(Base64.encodeBase64String(sks.getEncoded()));

    }
}
