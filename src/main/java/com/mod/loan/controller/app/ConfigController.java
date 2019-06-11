package com.mod.loan.controller.app;

import java.util.HashMap;
import java.util.Map;

import com.mod.loan.model.AppVersion;
import com.mod.loan.model.User;
import com.mod.loan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mod.loan.common.annotation.Api;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.AppStartup;
import com.mod.loan.model.Merchant;
import com.mod.loan.service.AppService;
import com.mod.loan.service.MerchantService;

@CrossOrigin("*")
@RestController
public class ConfigController {
	
	@Autowired
	private AppService appService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private UserService userService;

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
		//只有注册后才能下载
        AppVersion newVersion = appService.findNewVersion(RequestThread.getClientAlias(), RequestThread.getClientType());

        if ("haitun".equals(RequestThread.getClientAlias())){
            User user = userService.selectUserByPhone(phone, RequestThread.getClientAlias());
            if (user!=null) {
                String versionUrl = newVersion.getVersionUrl();
                int index = versionUrl.lastIndexOf("."); //去掉.后面的格式，前段拼接，防止链接被盗用
                newVersion.setVersionUrl(versionUrl.substring(0,index));
                return new ResultMessage(ResponseEnum.M2000, newVersion);
            }
            else {
                return new ResultMessage(ResponseEnum.M4002);
            }
		}
		else {
            return new ResultMessage(ResponseEnum.M2000, newVersion);
		}
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
}