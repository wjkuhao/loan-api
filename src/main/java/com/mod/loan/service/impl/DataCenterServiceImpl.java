package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import com.mod.loan.service.DataCenterService;
import com.mod.loan.util.OkHttpReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataCenterServiceImpl implements DataCenterService {
	private static Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);

	private final OkHttpReader okHttpReader;

	@Autowired
	public DataCenterServiceImpl(OkHttpReader okHttpReader) {
		this.okHttpReader = okHttpReader;
	}

	public boolean checkMultiLoan(String phone, String certNo){
		try {
			JSONObject reqJson = new JSONObject();
            reqJson.put("phone", phone);
            reqJson.put("idCard", certNo);

			String result = okHttpReader.postJson(Constant.MULTI_LOAN_QUERY_URL, reqJson.toJSONString(), null);

			JSONObject respObject = JSONObject.parseObject(result);
			String status = respObject.getString("status");
			if ("200".equals(status)){
                String count = respObject.getJSONObject("data").getString("count");
                if (StringUtils.isNotEmpty(count) && count.compareTo("1")>=0){
                    return true;
                }
			}else {
                logger.error("checkMultiLoan err={}", respObject.getString("msg"));
            }
		}catch (Exception e){
			logger.error("checkMultiLoan Exception phone={}, err={}", phone, e);
		}
		return false;
	}

}
