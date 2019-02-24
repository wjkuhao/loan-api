package com.mod.loan.mapper;

import java.util.List;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppBanner;

public interface AppBannerMapper extends MyBaseMapper<AppBanner> {
	
	List<AppBanner> findBannerListByMerchant(String merchant);
}