package com.mod.loan.service;

import com.mod.loan.model.Merchant;

public interface HeliPayService {

    /**
     * 合利宝商户余额查询
     */
    String balanceQuery(Merchant merchant, StringBuffer balance);
}
