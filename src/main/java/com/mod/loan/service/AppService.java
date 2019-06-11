package com.mod.loan.service;

import java.util.List;

import com.mod.loan.model.*;

public interface AppService {

	List<AppNotice> findNoticeListByMerchant(String merchant);
	
	List<AppBanner> findBannerListByMerchant(String merchant);

	List<AppEntry> findEntryList(String merchant);
	
	AppStartup  findAppStartupByMerchant(String merchant);

	AppVersion findNewVersion(String versionAlias,String versionType);

	AppHome findLatestHomeByMerchant(String merchant);
}
