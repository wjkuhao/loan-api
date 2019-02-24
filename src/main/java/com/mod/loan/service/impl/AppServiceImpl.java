package com.mod.loan.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.AppBannerMapper;
import com.mod.loan.mapper.AppEntryMapper;
import com.mod.loan.mapper.AppHomeMapper;
import com.mod.loan.mapper.AppNoticeMapper;
import com.mod.loan.mapper.AppStartupMapper;
import com.mod.loan.mapper.AppVersionMapper;
import com.mod.loan.model.AppBanner;
import com.mod.loan.model.AppEntry;
import com.mod.loan.model.AppHome;
import com.mod.loan.model.AppNotice;
import com.mod.loan.model.AppStartup;
import com.mod.loan.model.AppVersion;
import com.mod.loan.service.AppService;

@Service
public class AppServiceImpl implements AppService {

	@Autowired
	private AppNoticeMapper noticeMapper;
	@Autowired
	private AppBannerMapper bannerMapper;
	@Autowired
	private AppStartupMapper startupMapper;
	@Autowired
	private AppEntryMapper entryMapper;
	@Autowired
	private RedisMapper redisMapper;
	@Autowired
	private AppVersionMapper versionMapper;
	@Autowired
	private AppHomeMapper homeMapper;

	@Override
	public List<AppNotice> findNoticeListByMerchant(String merchant) {
		List<AppNotice> list = redisMapper.get(RedisConst.app_notice+merchant, new TypeReference<List<AppNotice>>() {
		});
		if (list == null) {
			list = noticeMapper.findNoticeListByMerchant(merchant);
			if (list != null) {
				redisMapper.set(RedisConst.app_notice+merchant, list, 60);
			}
		}
		return list;
	}

	@Override
	public List<AppBanner> findBannerListByMerchant(String merchant) {
		List<AppBanner> list = redisMapper.get(RedisConst.app_banner+merchant, new TypeReference<List<AppBanner>>() {
		});
		if (list == null) {
			list = bannerMapper.findBannerListByMerchant(merchant);
			if (list != null) {
				list.forEach(item -> {
					item.setBannerImgurl(Constant.SERVER_PREFIX_URL + item.getBannerImgurl());
				});
				redisMapper.set(RedisConst.app_banner+merchant, list, 60);
			}
		}
		return list;
	}

	@Override
	public List<AppEntry> findEntryList(String merchant) {
		List<AppEntry> list = redisMapper.get(RedisConst.app_entry+merchant, new TypeReference<List<AppEntry>>() {
		});
		if (list == null) {
			list = entryMapper.findEntryList(merchant);
			list.forEach(item -> {
				item.setEntryImgurl(Constant.SERVER_PREFIX_URL+item.getEntryImgurl());
			});
			if (list != null) {
				redisMapper.set(RedisConst.app_entry+merchant, list, 60);
			}
		}
		return list;
	}

	@Override
	public AppStartup findAppStartupByMerchant(String merchant) {
		// TODO Auto-generated method stub
		AppStartup appStartup = redisMapper.get(RedisConst.app_startup+merchant, new TypeReference<AppStartup>() {
		});
		if (appStartup == null) {
			appStartup = startupMapper.findAppStartupByMerchant(merchant);
			if (appStartup != null) {
				appStartup.setAdImgurl(Constant.SERVER_PREFIX_URL+appStartup.getAdImgurl());
				redisMapper.set(RedisConst.app_startup+merchant, appStartup, 60);
			}
		}
		return appStartup;
	}

	@Override
	public AppVersion findNewVersion(String versionAlias, String versionType) {
		// TODO Auto-generated method stub
		AppVersion version = redisMapper.get(RedisConst.app_version+versionAlias+versionType, new TypeReference<AppVersion>() {
		});
		if (version == null) {
			version = versionMapper.findNewVersion(versionAlias, versionType);
			if (version != null) {
				redisMapper.set(RedisConst.app_version+versionAlias+versionType, version, 60);
			}
		}
		return version;
	}

	@Override
	public AppHome findLatestHomeByMerchant(String merchant) {
		AppHome home = redisMapper.get(RedisConst.app_home+merchant, new TypeReference<AppHome>() {
		});
		if (home == null) {
			home = homeMapper.findLatestHomeByMerchant(merchant);
			if (home != null) {
				home.setImgurl(Constant.SERVER_PREFIX_URL+home.getImgurl());
				redisMapper.set(RedisConst.app_home+merchant,home, 60);
			}
		}
		return home;
	}

}
