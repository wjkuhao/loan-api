package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.Bank;
import com.mod.loan.model.UserBank;

public interface UserChangjieBankService extends BaseService<UserBank, Long> {

    /**
     * 畅捷鉴权绑卡请求（API）
     *
     * @param uid
     * @param cardNo
     * @param cardPhone
     * @param bank
     * @return
     */
    ResultMessage changjieBindBankCard4SendMsg(Long uid, String cardNo, String cardPhone, Bank bank);

    /**
     * 畅捷鉴权绑卡确认接口（API）
     *
     * @param validateCode
     * @param uid
     * @param bindInfo
     * @return
     */
    ResultMessage changjieBindBankCard4Confirm(String validateCode, Long uid, String bindInfo);

}
