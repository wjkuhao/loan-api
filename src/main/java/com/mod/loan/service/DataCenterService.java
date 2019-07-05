package com.mod.loan.service;

public interface DataCenterService {

	boolean isMultiLoan(String phone, String certNo, String merchant);

	void delMultiLoanOrder(String merchant, Long orderId);


}
