package com.mod.loan.task;

import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.HelipayEntrustedService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.helientrusted.vo.MerchantUserUploadResVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: wzg
 * @Date: 2019-06-21 14:19
 * @Description:合利宝委托代付绑卡处理任务
 */
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
    public void bindCardByPhone(String phone, String merchantAlias) {
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
     * 暂时不用
     */
//    public void bindCardByOrderStatus(String merchantAlias) {
//        Merchant merchant = new Merchant();
//        merchant.setMerchantAlias(merchantAlias);
//        merchant.setBindType(1);
//        merchant = merchantService.selectOne(merchant);
//        if (merchant == null) {
//            logger.error("商户信息不存在,merchant:{}", merchantAlias);
//            return;
//        }
//        if (StringUtils.isEmpty(merchant.getHlbEntrustedSignKey()) || StringUtils.isEmpty(merchant.getHlbEntrustedPrivateKey())) {
//            logger.error("商户非合利宝委托代付方式代付,merchant:{}", merchantAlias);
//            return;
//        }
//        List<UserBank> bankList = userBankService.selectEntrustedBindFailList(merchantAlias);
//        if (bankList != null && bankList.size() > 0) {
//            for (UserBank userBank : bankList) {
//                //调用合利宝委托代付绑卡
//                helipayEntrustedService.bindUserCard(userBank.getUid(), merchantAlias);
//            }
//        }
//    }

    /**
     * 合利宝委托代付,merchant下所有用户批量处理
     */
    public void bindCardMerchant(String merchantAlias, String createDate) throws Exception {
        if (StringUtils.isEmpty(createDate)) {
            createDate = TimeUtils.getTime();
        }
        logger.info("bindCardMerchant merchant:{}, createDate:{}", merchantAlias, createDate);
        List<UserBank> bankList = userBankService.selectEntrustedBindCardList(merchantAlias, createDate);
        int count = 0;
        while (count < 10 && bankList != null && bankList.size() > 0) {
            logger.info("bindCardMerchant selectEntrustedBindCardList size:{}", bankList.size());
            for (UserBank userBank : bankList) {
                //调用合利宝委托代付绑卡
                MerchantUserUploadResVo resVo = helipayEntrustedService.bindUserCard(userBank.getUid(), merchantAlias);
                if ("0000".equals(resVo.getRt2_retCode())) {
                    logger.info("合利宝委托代付绑卡成功,uid:{},cuid:{}", userBank.getUid(), resVo.getRt6_userId());
                } else {
                    count++;
                    logger.info("合利宝委托代付绑卡失败,uid:{},msg:{}", userBank.getUid(), resVo.getRt3_retMsg() + resVo.getRt8_desc());
                }
                Thread.sleep(200);
            }
            bankList = userBankService.selectEntrustedBindCardList(merchantAlias, createDate);
        }

    }
}
