package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppStartup;

public interface AppStartupMapper extends MyBaseMapper<AppStartup> {
	
	AppStartup findAppStartupByMerchant(String merchant);
}