package com.mod.loan.util.heli.util;

import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mod.loan.util.heli.annotation.FieldEncrypt;
import com.mod.loan.util.heli.annotation.SignExclude;

public class MessageHandle {

	private static final String CERT_PATH = "/data/conf/mx.cer"; // 合利宝cert
	private static final String ENCRYPTION_KEY = "encryptionKey";

	/**
	 * 获取map
	 */
	public static Map getReqestMap(Object bean, String pfxPath, String pfxPwd) throws Exception {
		Map retMap = new HashMap();
		boolean isEncrypt = false;
		String aesKey = AES.generateString(16);
		StringBuilder sb = new StringBuilder();
		Class clazz = bean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			String key = field.toString().substring(field.toString().lastIndexOf(".") + 1);
			String value = (String) field.get(bean);
			if (value == null) {
				value = "";
			}
			// 查看是否有需要加密字段的注解,有则加密
			// 这部分是将需要加密的字段先进行加密
			if (field.isAnnotationPresent(FieldEncrypt.class) && StringUtils.isNotEmpty(value)) {
				isEncrypt = true;
				value = AES.encryptToBase64(value, aesKey);
			}
			// 字段没有@SignExclude注解的拼签名串
			// 这部分是把需要参与签名的字段拼成一个待签名的字符串
			if (!field.isAnnotationPresent(SignExclude.class)) {
				sb.append("&");
				sb.append(value);
			}
			retMap.put(key, value);
		}
		// 如果有加密的，需要用合利宝的公钥将AES加密的KEY进行加密使用BASE64编码上送
		if (isEncrypt) {
			PublicKey publicKey = RSA.getPublicKeyByCert(CERT_PATH);
			String encrytionKey = RSA.encodeToBase64(aesKey, publicKey, "RSA");
			retMap.put(ENCRYPTION_KEY, encrytionKey);
		}

		// 使用商户的私钥进行签名
		PrivateKey privateKey = RSA.getPrivateKey(pfxPath, pfxPwd);
		String sign = RSA.sign(sb.toString(), privateKey);
		retMap.put("sign", sign);
		return retMap;
	}

}
