package com.mod.loan.service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.Bank;
import com.mod.loan.model.OrderRepay;

import java.util.List;
import java.util.Map;

public interface OrderJInYunTongRePayService {
    ResultMessage sendBindCardSms(Long uid, String cardNo, String cardPhone, Bank bank);
    ResultMessage bindCard(String validateCode, Long uid, String bindInfo);
    String jinyuntongOrderRepayNotice(Map map);
    List<OrderRepay> jinyuntongRepayQuery4Task();
    void queryRePayStatus(String repayNo);
    ResultMessage orderRepay(Long orderId);
}


