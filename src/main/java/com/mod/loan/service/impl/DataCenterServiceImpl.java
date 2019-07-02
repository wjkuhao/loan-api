package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.MerchantConfigMapper;
import com.mod.loan.model.MerchantConfig;
import com.mod.loan.service.DataCenterService;
import com.mod.loan.util.OkHttpReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DataCenterServiceImpl implements DataCenterService {
	private static Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);

	private final OkHttpReader okHttpReader;
	private final MerchantConfigMapper merchantConfigMapper;

	@Autowired
	public DataCenterServiceImpl(OkHttpReader okHttpReader,MerchantConfigMapper merchantConfigMapper) {
		this.okHttpReader = okHttpReader;
		this.merchantConfigMapper = merchantConfigMapper;
	}

	public boolean checkMultiLoan(String phone, String certNo ,String merchant){
        try {
            MerchantConfig merchantConfig = merchantConfigMapper.selectByMerchant(merchant);
            JSONObject reqJson = new JSONObject();
            reqJson.put("phone", phone);
            reqJson.put("idCard", certNo);
            if (merchantConfig == null) {
                return countMultiLoan(reqJson, "0");
            }
            Integer multiLoanCount = 0;
            if (merchantConfig.getMultiLoanCount() != null) {
                multiLoanCount = merchantConfig.getMultiLoanCount();
            }
            reqJson.put("merchant", merchantConfig.getMultiLoanMerchant());
            return countMultiLoan(reqJson, multiLoanCount.toString());

        } catch (Exception e) {
            logger.error("checkMultiLoan Exception phone={}, err={}", phone, e);
        }
        return false;
	}


	private Boolean countMultiLoan(JSONObject reqJson,String countMulti){

		String result = okHttpReader.postJson(Constant.MULTI_LOAN_QUERY_URL, reqJson.toJSONString(), null);
		// 请求异常
		if ("".equals(result)) {
			return false;
		}
		//
		JSONObject respObject = JSONObject.parseObject(result);
		String status = respObject.getString("status");
		if ("200".equals(status)){
			String count = respObject.getJSONObject("data").getString("count");
			if (StringUtils.isNotEmpty(count) && count.compareTo(countMulti)>0){
				return true;
			}
		}else {
			logger.error("checkMultiLoan err={}", respObject.getString("msg"));
		}
		return false;
	}

	@Async
	public void delMultiLoanOrder(String merchant, Long orderId){
		try {
			JSONObject reqJson = new JSONObject();
			reqJson.put("merchant", merchant);
			reqJson.put("orderId", orderId);

			String result = okHttpReader.postJson(Constant.MULTI_LOAN_DEL_URL, reqJson.toJSONString(), null);

			JSONObject respObject = JSONObject.parseObject(result);
			String status = respObject.getString("status");
			if (!"200".equals(status)){
				logger.error("delMultiLoanOrder err={}", respObject.getString("msg"));
			}
		}catch (Exception e){
			logger.error("delMultiLoanOrder Exception merchant={} orderId={}, err={}", merchant, orderId, e);
		}
	}
}
