package com.mod.loan.service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.model.UserIdent;
import com.mod.loan.model.UserInfo;

public interface UserService extends BaseService< User,Long>{

	/**
	 * 判断手机号在商户下是否注册过
	 * @param userPhone 手机号
	 * @param merchant 商户
	 * @return 用户
	 */
	User selectUserByPhone(String userPhone,String merchant);
	/**
	 * 判断身份证在商户下是否注册
	 * @param userCertNo 身份证号
	 * @param merchant 商户
	 * @return 用户
	 */
	User selectUserByCertNo(String userCertNo,String merchant);
	
	Long addUser(String phone,String password,String userOrigin,String merchant);
	
	void updateUserRealName(User user,UserIdent userIdent);
	
	void updateUserInfo(UserInfo userInfo,UserIdent userIdent);
	
	boolean insertUserBank(Long uid,UserBank userBank);

	String saveRealNameAuthInfo(JSONObject jsonObject, Long uid);

    UserInfo selectUserInfo(Long uid);
}
