package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.UserRegisterCodeStat;

public interface UserRegisterCodeStatService extends BaseService<UserRegisterCodeStat,Long>{


	UserRegisterCodeStat selectDayCount(String phone, String merchant);
}
