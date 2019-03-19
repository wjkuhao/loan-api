package com.mod.loan.util;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;


public class UdcreditUtils {

	public static ResultMessage checkArgs(JSONObject authJson) {
		ResultMessage faceDTO = new ResultMessage();

        String authResult = authJson.getString("auth_result");
        if (!authResult.equals("T")){
            faceDTO.setMessage(authJson.getString("fail_reason"));
            faceDTO.setStatus(ResponseEnum.M4000.getCode());
            return faceDTO;
        }

		faceDTO.setStatus(ResponseEnum.M2000.getCode());
		return faceDTO;
	}

	public static void main(String[] args) {
		String str = "\t{\n" +
				"        \"partner_order_id\":\"2294ef51c-319a-4ad5-91d5-5c62a90e1111\",\n" +
				"        \"sign\":\"B6F1C4A0FEE11AC740C2FBAFE7F0DBD9\",\n" +
				"        \"sign_time\":\"20170902110236\",\n" +
				"        \"address\":\"浙江省杭州市滨江区\",\n" +
				"        \"age\":\"28\",\n" +
				"        \"auth_result\":\"T\",\n" +
				"        \"result_status\":\"01\",\n" +
				"        \"birthday\":\"1989.09.01\",\n" +
				"        \"gender\":\"男\",\n" +
				"        \"id_name\":\"小盾\",\n" +
				"        \"id_number\":\"340826198912281234\",\n" +
				"        \"idcard_back_photo\":\"/9j/4AAQSkZJRgABAQAASABIAAD/4...\",\n" +
				"        \"idcard_front_photo\":\"/9j/4AAQSkZJRgABAQAASABIAAD/4...\",\n" +
				"        \"idcard_portrait_photo\":\"/9j/4AAQSkZJRgABAQAASABIAAD/4...\",\n" +
				"        \"issuing_authority\":\"杭州市公安局\",\n" +
				"        \"living_photo\":\"/9j/4AAQSkZJRgABAQAAAQABAAD/2...\",\n" +
				"        \"nation\":\"汉\",\n" +
				"        \"partner_code\":\"201708076623\",\n" +
				"        \"risk_tag\":{\n" +
				"            \"living_attack\":\"0\"\n" +
				"        },\n" +
				"        \"similarity\":\"0.8094\",\n" +
				"        \"validity_period\":\"2015.04.01-2025.04.01\",\n" +
				"        \"verify_status\":\"1\"\n" +
				"    } ";
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(checkArgs(jsonObject));
	}

}
