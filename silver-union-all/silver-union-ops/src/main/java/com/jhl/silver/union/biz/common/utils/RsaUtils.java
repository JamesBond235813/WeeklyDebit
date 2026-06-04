package com.jhl.silver.union.biz.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加解密与签名验签工具
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
public class RsaUtils {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private RsaUtils() {
    }

    /**
     * 加载 RSA 公钥
     *
     * @param keyBase64 base64 公钥字符串
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKey(String keyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 加载 RSA 私钥
     *
     * @param keyBase64 base64 私钥字符串
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String keyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用 RSA 私钥解密 aes 密钥
     *
     * @param encryptedSecretKey RSA 加密后的密钥
     * @param privateKeyBase64   RSA 私钥
     * @return
     * @throws Exception
     */
    public static String decryptSecretKeyWithRsa(String encryptedSecretKey, String privateKeyBase64)
            throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyBase64);
        javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedSecretKey);
        byte[] decryptedKeyBytes = rsaCipher.doFinal(encryptedKeyBytes);
        return new String(decryptedKeyBytes, StandardCharsets.UTF_8);
    }

    /**
     * RSA + SHA256 验签
     *
     * @param signBase64  签名字符串(base64)
     * @param dataToVerify 待验签内容
     * @param publicKeyBase64 RSA 公钥
     * @return
     * @throws Exception
     */
    public static boolean verifySign(String signBase64, String dataToVerify, String publicKeyBase64)
            throws Exception {
        PublicKey publicKey = loadPublicKey(publicKeyBase64);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(dataToVerify.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.getDecoder().decode(signBase64));
    }
}
