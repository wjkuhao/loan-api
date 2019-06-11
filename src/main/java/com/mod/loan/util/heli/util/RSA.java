package com.mod.loan.util.heli.util;

import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

/**
 * 私钥签名，私钥签名（只有私钥能签），公钥验证签名，确认发起人是私钥持有人
 * 公钥加密，公钥加密只有私钥能解密
 *
 * @author datou
 */
public class RSA {

    /**
     * String to hold name of the encryption padding.
     */
    public static final String PROVIDER = "BC";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * 验证签名
     *
     * @param data      数据
     * @param sign      签名
     * @param publicKey 公钥
     * @return
     */
    public static boolean verifySign(byte[] data, byte[] sign,
                                     PublicKey publicKey) {
        try {
            Signature signature = Signature
                    .getInstance("MD5withRSA");
            signature.initVerify(publicKey);
            signature.update(data);
            boolean result = signature.verify(sign);
            return result;
        } catch (Exception e) {

            throw new RuntimeException("verifySign fail!", e);
        }
    }

    /**
     * 验证签名
     *
     * @param data     数据
     * @param sign     签名
     * @param pubicKey 公钥
     * @return
     */
    public static boolean verifySign(String data, String sign,
                                     PublicKey pubicKey) {
        try {
            byte[] dataByte = data
                    .getBytes("UTF-8");
            byte[] signByte = Base64.decode(sign
                    .getBytes("UTF-8"));
            return verifySign(dataByte, signByte, pubicKey);
        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException("verifySign fail! data[" + data + "] sign[" + sign + "]", e);
        }
    }

    /**
     * 签名
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sign(byte[] data, PrivateKey key) {
        try {
            Signature signature = Signature
                    .getInstance("MD5withRSA");
            signature.initSign(key);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("sign fail!", e);
        }
    }

    /**
     * 签名
     *
     * @param data
     * @param key
     * @return
     */
    public static String sign(String data, PrivateKey key) {
        try {
            byte[] dataByte = data.getBytes("UTF-8");
            return new String(Base64.encode(sign(dataByte, key)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("sign fail!", e);
        }
    }

    public static byte[] encode(String encodeString, Key key, String padding) throws Exception {
        final Cipher cipher = Cipher.getInstance(padding, PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = encodeString.getBytes("UTF-8");
        byte[] encodedByteArray = new byte[]{};
        for (int i = 0; i < bytes.length; i += 117) {
            byte[] subarray = ArrayUtils.subarray(bytes, i, i + 117);
            byte[] doFinal = cipher.doFinal(subarray);
            encodedByteArray = ArrayUtils.addAll(encodedByteArray, doFinal);
        }
        return encodedByteArray;
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @return
     */
    public static String encodeToBase64(String data, Key key, String padding) {
        try {
            return new String(Base64.encode(encode(data,
                    key, padding)));
        } catch (Exception e) {
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    public static PublicKey getPublicKeyByCert(String path) throws Exception {
        CertificateFactory cff = CertificateFactory.getInstance("X.509");
//        Resource res2 = new ClassPathResource("conf/helipay.cer");
//        InputStream fis1 = res2.getInputStream();
        FileInputStream fis1 = new FileInputStream(path);
        Certificate cf = cff.generateCertificate(fis1);
        PublicKey publicKey = cf.getPublicKey();
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String pfxPath, String pfxPassword) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(pfxPath);
//            Resource res = new ClassPathResource(pfxPath);
//            InputStream fis = res.getInputStream();
            char[] nPassword = null;
            if ((pfxPassword == null) || pfxPassword.trim().equals("")) {
                nPassword = null;
            } else {
                nPassword = pfxPassword.toCharArray();
            }
            ks.load(fis, nPassword);
            fis.close();
            Enumeration enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements())
            {
                keyAlias = (String) enumas.nextElement();
            }
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            return prikey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
