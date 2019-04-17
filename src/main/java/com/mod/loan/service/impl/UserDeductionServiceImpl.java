package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
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
	public void addUser(Long uid, String userOrigin, String merchant, String phone) {
        Long originId;
        try {
            originId = Long.valueOf(userOrigin);
        }
        catch (Exception e){
            log.error("自然流量,uid={},userOrigin={},phone={}, error={}", uid,  userOrigin,  phone, e.getMessage());
            originId = Constant.NATURE_ORIGIN_ID;
            userOrigin = String.valueOf(originId); //暂时自然流量记录到61上面
        }

        MerchantOrigin merchantOrigin = merchantOriginService.selectByPrimaryKey(originId);
        if (merchantOrigin==null || merchantOrigin.getDeductionRate()==0) {//不存在或者扣量为0，则保留该客户
            UserDeduction userDeduction = initUserDeduction(uid, merchant, userOrigin, phone);
            userDeductionMapper.insertUser(userDeduction);
            return;
        }

        Integer deductionNum = merchantOrigin.getDeductionRate();
        int randomNum = new Random().nextInt(100);
        //比例数设置为0到99之间的数，小于等于随机数则扣掉该客户
        if (randomNum>deductionNum){
            UserDeduction userDeduction = initUserDeduction(uid, merchant, userOrigin, phone);
            userDeductionMapper.insertUser(userDeduction);
        }else {
            log.info("扣量该用户uid={}, origin={}，merchant={}", uid, userOrigin, merchant);
        }
    }

    private UserDeduction initUserDeduction(Long uid, String merchant, String userOrigin, String phone){
        UserDeduction userDeduction = new UserDeduction();
        userDeduction.setId(uid);
        userDeduction.setMerchant(merchant);
        userDeduction.setUserOrigin(userOrigin);
        userDeduction.setCreateTime(new Date());
        userDeduction.setUserPhone(phone);
        return userDeduction;
    }
}
