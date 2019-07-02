package com.mod.loan.controller.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mod.loan.common.annotation.Api;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;

@CrossOrigin("*")
@RestController
public class ConfigController {
	
	@Autowired
	private AppService appService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private AppConfigService appConfigService;

	/**
	 * 启动页，首页图片弹窗
	 * @return
	 */
	@RequestMapping(value = "app_home")
	@Api
	public ResultMessage app_home() {
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		AppStartup startup = appService.findAppStartupByMerchant(RequestThread.getClientAlias());
		if(null != startup){
			map.put("imgurl",startup.getAdImgurl());
			map.put("url",startup.getAdUrl());
		}
		data.put("startup", map);
		data.put("home", appService.findLatestHomeByMerchant(RequestThread.getClientAlias()));
		return new ResultMessage(ResponseEnum.M2000, data);
	}

	/**
	 * 客户端公告，轮播图，轮番图，关键字接口
	 * @return
	 */
	@RequestMapping(value = "app_index")
	@Api
	public ResultMessage app_index() {
		Map<String, Object> data=new HashMap<>();
		data.put("notice", appService.findNoticeListByMerchant(RequestThread.getClientAlias()));
		data.put("banner", appService.findBannerListByMerchant(RequestThread.getClientAlias()));
		data.put("entry",appService.findEntryList(RequestThread.getClientAlias()));
		data.put("keyword","iPad|小米SE8|男装|面膜");//搜索关键字
		return new ResultMessage(ResponseEnum.M2000, data);
	}
	
	@RequestMapping(value = "check_version")
	@Api
	public ResultMessage check_version(String phone) {
		return new ResultMessage(ResponseEnum.M2000, appService.findNewVersion(RequestThread.getClientAlias(), RequestThread.getClientType()));
	}

	@RequestMapping(value = "mechant_info")
	@Api
	public ResultMessage mechant_info() {
		Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
		Map<String, String> data=new HashMap<>();
		data.put("alias", merchant.getMerchantAlias());
		data.put("company", merchant.getMerchantName());
		if ("ios".equals(RequestThread.getClientType())) {
			data.put("app", merchant.getMerchantAppIos());
		}else {
			data.put("app", merchant.getMerchantApp());
		}
		return new ResultMessage(ResponseEnum.M2000, data);
	}

	/**
	 * app信息查询
	 */
	@RequestMapping(value = "/appConfig")
	public ResultMessage getAppConfig(String clientAlias) {
        AppConfig appConfigNew = appConfigService.selectByClientAlias(clientAlias);
		return new ResultMessage(ResponseEnum.M2000, appConfigNew);
	}
}