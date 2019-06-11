package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.UserRegisterCodeStat;
import org.apache.ibatis.annotations.Param;

public interface UserRegisterCodeStatMapper extends MyBaseMapper<UserRegisterCodeStat> {

    UserRegisterCodeStat selectDayCount(@Param("userPhone") String userPhone, @Param("merchant") String merchant);
}