package com.mod.loan.service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.model.UserIdent;
import com.mod.loan.model.UserInfo;

import java.util.List;

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
	
	Long addUser(String phone,String password,String userOrigin,String merchant, String browserType);
	
	void updateUserRealName(User user,UserIdent userIdent);
	
	void updateUserInfo(UserInfo userInfo,UserIdent userIdent);
	
	boolean insertUserBank(Long uid,UserBank userBank);

	String saveRealNameAuthInfo(JSONObject jsonObject, Long uid);

    UserInfo selectUserInfo(Long uid);

    /**
     * 取出该身份证号在所有系统注册的信息
     * @param certNo 身份证号
     */
    List<User> selectUserByCertNo(String certNo);

	/**
	 * 取出该手机号在所有系统注册的信息
	 * @param phone 身份证号
	 */
	List<User> selectUserByPhone(String phone);

	/**
	 * 点击PV、UV统计
	 *
	 * @param userId        用户id
	 * @param merchant      商户名称
	 * @param loanMarketUrl 贷超链接
	 * @return
	 * @author NIELIN 20190604
	 */
	void pvTotal(Long userId, String merchant, String loanMarketUrl) throws Exception;

}
