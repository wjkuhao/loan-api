package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import com.mod.loan.model.MerchantConfig;
import com.mod.loan.service.DataCenterService;
import com.mod.loan.service.MerchantConfigService;
import com.mod.loan.util.OkHttpReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DataCenterServiceImpl implements DataCenterService {
	private static Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);

	private final OkHttpReader okHttpReader;
	private final MerchantConfigService merchantConfigService;

	@Autowired
	public DataCenterServiceImpl(OkHttpReader okHttpReader, MerchantConfigService merchantConfigService) {
		this.okHttpReader = okHttpReader;
		this.merchantConfigService = merchantConfigService;
	}

	@Override
	public boolean isMultiLoan(String phone, String certNo , String merchant){
        try {
			MerchantConfig merchantConfig = merchantConfigService.selectByMerchant(merchant);
			JSONObject reqJson = new JSONObject();
			reqJson.put("phone", phone);
			reqJson.put("idCard", certNo);

			Integer multiLoanCount = 0;
			if (merchantConfig!=null && merchantConfig.getMultiLoanCount() != null) {
				multiLoanCount = merchantConfig.getMultiLoanCount();
				reqJson.put("merchant", merchantConfig.getMultiLoanMerchant());
			}
			String result = okHttpReader.postJson(Constant.MULTI_LOAN_QUERY_URL, reqJson.toJSONString(), null);
			return checkCount(result, multiLoanCount);

        } catch (Exception e) {
            logger.error("isMultiLoan Exception phone={}, err={}", phone, e);
        }
        return false;
	}

	@Override
	public boolean isMultiLoanOverdue(String phone, String certNo){
		try {
			JSONObject reqJson = new JSONObject();
			reqJson.put("phone", phone);
			reqJson.put("idCard", certNo);

			String result = okHttpReader.postJson(Constant.MULTI_LOAN_QUERY_OVERDUE_URL, reqJson.toJSONString(), null);
			return checkCount(result, 0);
		} catch (Exception e) {
			logger.error("isMultiLoanOverdue Exception phone={}, err={}", phone, e);
		}
		return false;
	}

	private boolean checkCount(String result, int countMulti){
		// 请求异常
		if (null == result || result.length() < 1) {
			return false;
		}
		//
		JSONObject respObject = JSONObject.parseObject(result);
		String status = respObject.getString("status");
		if ("200".equals(status)){
			Integer count = respObject.getJSONObject("data").getInteger("count");
			return (count != null && count > countMulti);
		}else {
			logger.error("isMultiLoan err={}", respObject.getString("msg"));
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
