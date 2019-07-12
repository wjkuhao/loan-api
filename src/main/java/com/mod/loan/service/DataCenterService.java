package com.mod.loan.service;

public interface DataCenterService {

	/**
	 * @param phone 手机号
	 * @param certNo 身份证
	 * @param merchant 商户，用于加载配置
	 * @return 按商户配置的参数去数据中心检查是否在途订单
	 */
	boolean isMultiLoan(String phone, String certNo, String merchant);

	void delMultiLoanOrder(String merchant, Long orderId);

	/**
	 * @param phone 手机号
	 * @param certNo 身份证
	 * @return 去数据中心检查是否在存在逾期
	 */
	boolean isMultiLoanOverdue(String phone, String certNo);
}
