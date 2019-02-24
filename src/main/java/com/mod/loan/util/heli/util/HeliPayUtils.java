package com.mod.loan.util.heli.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.MD5;
import com.mod.loan.util.heli.HttpClientService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeliPayUtils {

	public static JSONObject requestMD5(String requrl,LinkedHashMap<String, String> params,String md5_key) {
		String sign = getSignMD5(params, md5_key);
		params.put("sign", sign);
		String resp = HttpClientService.getHttpResp(params, requrl);
		JSONObject result = JSON.parseObject(resp);
		return result;
	}

	public static JSONObject requestMD5(String requrl,LinkedHashMap<String, String> params,String md5_key,LinkedHashMap<String, String> noSignParams) {
		String sign = getSignMD5(params, md5_key);
		params.put("sign", sign);
		if (noSignParams!=null) {
			params.putAll(noSignParams);
		}
		String resp = HttpClientService.getHttpResp(params, requrl);
		JSONObject result = JSON.parseObject(resp);
		return result;
	}


	private static String getSignMD5(Map<String, String> params,String md5_key) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(params.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			content.append("&"+ value);
		}
		content.append("&"+ md5_key);
		return MD5.toMD5(content.toString());
	}

}
