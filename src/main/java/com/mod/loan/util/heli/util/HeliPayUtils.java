package com.mod.loan.util.heli.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.MD5;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.*;

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


	public Map<String, Object> parseJSON2Map(JSONObject json) {
		Map<String, Object> map = new HashMap();
		if (json != null) {
			for (Object k : json.keySet()) {
				Object v = json.get(k);
				// 如果内层还是数组的话，继续解析
				if (v instanceof JSONArray) {
					List<Map<String, Object>> list = new ArrayList();
					Iterator<Object> it = ((JSONArray) v).iterator();
					while (it.hasNext()) {
						JSONObject json2 = (JSONObject) it.next();
						list.add(parseJSON2Map(json2));
					}
					map.put(k.toString(), list);
				} else {
					map.put(k.toString(), v);
				}
			}
		}
		System.out.println(map);
		return map;
	}

	public static String toHex(byte input[]) {
		if (input == null) {
			return null;
		}
		StringBuffer output = new StringBuffer(input.length * 2);
		for (int i = 0; i < input.length; i++) {
			int current = input[i] & 0xff;
			if (current < 16)
				output.append("0");
			output.append(Integer.toString(current, 16));
		}

		return output.toString();
	}

	/**
	 * 委托代付接口时间戳
	 * */
	public static String getTimestamp() {
		SimpleDateFormat STRING_FORMAT_TIMESTAMP_TEST = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.sss");
		String dateString1 = STRING_FORMAT_TIMESTAMP_TEST.format(new Date());
		return dateString1;
	}

	/**
	 * 委托代付接口获取订单ID
	 * */
	public static String getOrderId(String uid) {
		return String.format("%s%s%s", "p", new DateTime().toString(TimeUtils.dateformat5), uid);
	}

}
