package com.mod.loan.task;

import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.HelipayEntrustedService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: wzg
 * @Date: 2019-06-21 14:19
 * @Description:合利宝委托代付绑卡处理任务
 */
@Profile("online")
@Component
public class HelipayEntrustedBindCardTask {

    private static final Logger logger = LoggerFactory.getLogger(HelipayEntrustedBindCardTask.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private HelipayEntrustedService helipayEntrustedService;

    /**
     * 合利宝委托代付单用户绑卡
     */
    public void bindCard(String phone, String merchantAlias) {
        User user = new User();
        user.setUserPhone(phone);
        user.setMerchant(merchantAlias);
        user = userService.selectOne(user);

        Merchant merchant = new Merchant();
        merchant.setMerchantAlias(merchantAlias);
        merchant.setBindType(1);
        merchant = merchantService.selectOne(merchant);

        if (user == null) {
            logger.error("用户信息不存在,phone:{},merchant:{}", phone, merchantAlias);
            return;
        }
        if (merchant == null) {
            logger.error("商户信息不存在,phone:{},merchant:{}", phone, merchantAlias);
            return;
        }
        UserBank userBank = userBankService.selectUserCurrentBankCard(user.getId());
        if (userBank == null) {
            logger.error("用户银行卡信息不存在,uid:{},phone:{},merchant:{}", user.getId(), phone, merchantAlias);
            return;
        }
        //调用合利宝委托代付绑卡
        helipayEntrustedService.bindUserCard(user, userBank, merchant);
    }

    /**
     * 合利宝委托代付merchant用户处理
     */
    public void bindCardBatch(String merchantAlias) {
        Merchant merchant = new Merchant();
        merchant.setMerchantAlias(merchantAlias);
        merchant.setBindType(1);
        merchant = merchantService.selectOne(merchant);
        if (merchant == null) {
            logger.error("商户信息不存在,merchant:{}", merchantAlias);
            return;
        }
        if (StringUtils.isEmpty(merchant.getHlbEntrustedSignKey()) || StringUtils.isEmpty(merchant.getHlbEntrustedPrivateKey())) {
            logger.error("商户非合利宝委托代付方式代付,merchant:{}", merchantAlias);
            return;
        }
        List<UserBank> bankList = userBankService.selectEntrustedBindFailList(merchantAlias);
        if (bankList != null && bankList.size() > 0) {
            for (UserBank userBank : bankList) {
                //调用合利宝委托代付绑卡
                helipayEntrustedService.bindUserCard(userBank.getUid(), merchantAlias);
            }
        }
    }
}
