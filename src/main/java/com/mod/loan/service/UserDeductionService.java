package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.UserDeduction;


public interface UserDeductionService extends BaseService<UserDeduction,Long>{

    void addUser(Long uid, String userOrigin, String merchant, String phone);
}
