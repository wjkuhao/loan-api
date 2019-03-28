package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.UserDeductionMapper;
import com.mod.loan.model.MerchantOrigin;
import com.mod.loan.model.UserDeduction;
import com.mod.loan.service.MerchantOriginService;
import com.mod.loan.service.UserDeductionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class UserDeductionServiceImpl extends BaseServiceImpl<UserDeduction,Long> implements UserDeductionService {

	private static Logger log = LoggerFactory.getLogger(UserDeductionServiceImpl.class);

	private final MerchantOriginService merchantOriginService;

	@Autowired
    UserDeductionMapper userDeductionMapper;

    @Autowired
    public UserDeductionServiceImpl(MerchantOriginService merchantOriginService) {
        this.merchantOriginService = merchantOriginService;
    }

    @Override
	public void AddUser(Long uid, String userOrigin, String merchant) {

        MerchantOrigin merchantOrigin = new MerchantOrigin();
        merchantOrigin.setOriginName(userOrigin);
        merchantOrigin.setMerchant(merchant);
        merchantOrigin = merchantOriginService.selectOne(merchantOrigin);
        if (merchantOrigin==null) {
            return;
        }

        Integer deductionNum = merchantOrigin.getDeductionRate();
        int randomNum = new Random().nextInt(100);
        //比例数设置为0到99之间的数，小于等于随机数则扣掉该客户
        if (randomNum>deductionNum){
            UserDeduction userDeduction = new UserDeduction();
            userDeduction.setId(uid);
            userDeduction.setMerchant(merchant);
            userDeduction.setUserOrigin(userOrigin);
            userDeduction.setCreateTime(new Date());
            userDeductionMapper.insertUser(userDeduction);
        }else {
            log.info("扣量该用户uid={}, origin={}，merchant={}", uid, userOrigin, merchant);
        }
    }
}
