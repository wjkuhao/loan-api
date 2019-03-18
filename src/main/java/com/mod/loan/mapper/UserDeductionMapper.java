package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.UserDeduction;

public interface UserDeductionMapper extends MyBaseMapper<UserDeduction> {
    void insertUser(UserDeduction userDeduction);
}