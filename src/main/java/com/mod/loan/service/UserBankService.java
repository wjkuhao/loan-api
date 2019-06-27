package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.Bank;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.UserBank;

import java.util.List;

public interface UserBankService extends BaseService<UserBank,Long> {
    /**
     * 获取当前使用中的银行卡
     * @param uid
     * @return
     */
    UserBank selectUserCurrentBankCard(Long uid);

    /**
     * 获取合利宝绑卡短验
     */
    ResultMessage sendHeliSms(Long uid,String cardNo, String cardPhone,Bank bank);

    /**
     * 根据合利宝短验进行绑卡
     */
    ResultMessage bindByHeliSms(String validateCode,Long uid,String bindInfo);

    /**
     * 获取富友绑卡短验
     */
    ResultMessage sendFuyouSms(Long uid,String cardNo, String cardPhone,Bank bank);

    /**
     * 根据富友短验进行绑卡
     */
    ResultMessage bindByFuyouSms(String validateCode,Long uid,String bindInfo);

    /**
     * 根据富友协议号进行解约
     */
    ResultMessage unbindByFuyou(Long uid, String protocolNo, Merchant merchant);

    /**
     * 获取汇聚绑卡短验
     */
    ResultMessage sendHuijuSms(Long uid,String cardNo, String cardPhone,Bank bank);

    /**
     * 根据汇聚短验进行绑卡
     */
    ResultMessage bindByHuijuSms(String validateCode,Long uid,String bindInfo);

    /**
     * 获取易宝绑卡短验
     */
    ResultMessage sendYeepaySms(Long uid, String cardNo, String cardPhone,Bank bank);

    /**
     * 根据易宝短验进行绑卡
     */
    ResultMessage bindYeepaySms(String validateCode, Long uid, String bindInfo);

    /**获取快钱绑卡短验
     *
     */
    ResultMessage sendKuaiqianSms(Long uid, String cardNo, String cardPhone,Bank bank);

    /**
     * 根据快钱短验进行绑卡
     */
    ResultMessage bindKuaiqianSms(String validateCode, Long uid, String bindInfo);


    UserBank selectUserMerchantBankCard(Long uid, Integer bindType);


    /**
     * 查询合利宝委托代付绑卡失败的银行卡列表
     * */
    List<UserBank> selectEntrustedBindFailList(String merchant);

    /**
     * 查询合利宝委托代付未绑卡的用户银行卡列表
     * */
    List<UserBank> selectEntrustedBindCardList(String merchant);
}
