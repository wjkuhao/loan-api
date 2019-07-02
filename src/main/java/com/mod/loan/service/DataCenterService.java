package com.mod.loan.service;

import com.mod.loan.model.MerchantConfig;

public interface DataCenterService {

	boolean checkMultiLoan(String phone, String certNo, MerchantConfig merchantConfig);

	void delMultiLoanOrder(String merchant, Long orderId);


}
