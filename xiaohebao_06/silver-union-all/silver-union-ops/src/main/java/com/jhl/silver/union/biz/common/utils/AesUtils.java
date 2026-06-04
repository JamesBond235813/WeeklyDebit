package com.jhl.silver.union.biz.common.utils;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.commons.exception.BizException;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import java.util.Base64;

/**
 * @author: qingren
 * @create_time: 2025/12/5
 */
@Slf4j
public class AesUtils {
    private static final String AES_KEY_ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encryptWithAesECB(String content, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_KEY_ALGORITHM);
            byte[] toEncryptContent = content.getBytes(StandardCharsets.UTF_8);
            // 取加密器
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedData = cipher.doFinal(toEncryptContent);
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            log.error("加密数据失败", e);
            throw new BizException(ResultCode.DECRYPT_FAILED, content);
        }
    }

    public static String decryptWithAesECB(String encContent, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_KEY_ALGORITHM);
            byte[] encryptedData = Base64.getDecoder().decode(encContent);
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密数据失败", e);
            throw new BizException(ResultCode.DECRYPT_FAILED, encContent);
        }
    }

}
