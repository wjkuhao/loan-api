package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.UserRegisterCodeStatMapper;
import com.mod.loan.model.UserRegisterCodeStat;
import com.mod.loan.service.UserRegisterCodeStatService;
import com.mod.loan.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserRegisterCodeStatServiceImpl extends BaseServiceImpl< UserRegisterCodeStat,Long> implements UserRegisterCodeStatService{

	private static Logger log = LoggerFactory.getLogger(UserRegisterCodeStatServiceImpl.class);

	@Autowired
    UserRegisterCodeStatMapper userRegisterCodeStatMapper;

	@Override
	public UserRegisterCodeStat selectDayCount(String phone, String merchant) {
        UserRegisterCodeStat userRegisterCodeStat = userRegisterCodeStatMapper.selectDayCount(phone,merchant);
        if (userRegisterCodeStat==null){
            userRegisterCodeStat = new  UserRegisterCodeStat();
            userRegisterCodeStat.setTotalCount(1);
            userRegisterCodeStat.setCreateTime(new Date());
            userRegisterCodeStat.setDayCount(1);
            userRegisterCodeStat.setMerchant(merchant);
            userRegisterCodeStat.setUserPhone(phone);
            userRegisterCodeStat.setRegisterDate(TimeUtil.nowDate());
            insert(userRegisterCodeStat);
        }
        else {
            //如果是当天则当天累加次数+1， 不是则更新日期，重置为1
            if (userRegisterCodeStat.getRegisterDate().equals(TimeUtil.nowDate())){
                userRegisterCodeStat.setDayCount(userRegisterCodeStat.getDayCount() + 1);
            }else {
                userRegisterCodeStat.setDayCount(1);
                userRegisterCodeStat.setRegisterDate(TimeUtil.nowDate());
            }
            userRegisterCodeStat.setTotalCount(userRegisterCodeStat.getTotalCount() + 1);
            userRegisterCodeStat.setUpdateTime(new Date());
            updateByPrimaryKeySelective(userRegisterCodeStat);
        }
        return userRegisterCodeStat;
	}
}
