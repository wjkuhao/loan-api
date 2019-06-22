package com.mod.loan.service;

import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.util.helientrusted.vo.MerchantUserUploadResVo;

public interface HelipayEntrustedService {

    MerchantUserUploadResVo bindUserCard(Long uid, String merchantAlias);

    MerchantUserUploadResVo bindUserCard(User user, UserBank userBank, Merchant merchant);
}
