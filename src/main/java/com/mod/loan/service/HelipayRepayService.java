package com.mod.loan.service;

import com.mod.loan.common.model.ResultMessage;

public interface HelipayRepayService {


    ResultMessage repayInfo(String repayNo, String type);

    ResultMessage repayActive(String repayNo, String validateCode, String type);

    void repayResult(String rt2_retCode,
                       String rt9_orderStatus, String rt5_orderId);

    ResultMessage bindPaySmsProcess(String orderId, String type);

    void deferRepayResult(String rt2_retCode,
                       String rt9_orderStatus, String rt5_orderId);

}
