package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;

import java.util.List;

public interface OrderRepayService extends BaseService<OrderRepay,String>{

	
	void updateOrderRepayInfo(OrderRepay orderRepay,Order order);

	int countRepaySuccess(Long orderId);

	String yeepayRepayNoSms(Long orderId);

	String heliPayRepayNoSms(Long orderId);

	String yeepayRepayQuery(String repayNo, String merchantAlias);

	List<OrderRepay> selectReapyingOrder();

    OrderRepay selectLastByOrderId(Long orderId);

    void repaySuccess(OrderRepay orderRepay, Order orderOld);

    void repayFailed(OrderRepay orderRepay, String callbackErr);

	List<OrderRepay> huichaoRepay4AliAppH5OrWxScanQuery();

}
