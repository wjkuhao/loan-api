package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppHome;

public interface AppHomeMapper extends MyBaseMapper<AppHome> {

    AppHome findLatestHomeByMerchant(String merchant);
}