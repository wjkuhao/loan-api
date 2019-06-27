package com.mod.loan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.UserBank;

import java.util.List;


public interface UserBankMapper extends MyBaseMapper<UserBank> {

	/**
	 * 获取当前使用中的银行卡
	 * @param uid
	 * @return
	 */
	UserBank selectUserCurrentBankCard(@Param("uid")Long uid);

	/**
	 * 将用户原来已绑定的老卡禁用
	 * @param uid
	 * @return
	 */
	int updateUserOldCardInvaild(Long uid);

	/**
	 * 获取富友最近绑定过的卡号
	 */
	UserBank selectFuyouBankCard(Long uid);

	/**
	 * 查询合利宝委托代付绑卡失败的银行卡列表
	 * */
	List<UserBank> selectEntrustedBindFailList(String merchant);


	UserBank selectUserMerchantBankCard(@Param("uid")Long uid, @Param("bindType")Integer bindType);

	List<UserBank> selectEntrustedBindCardList(String merchant);
}