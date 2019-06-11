package com.mod.loan.service;

import com.mod.loan.util.helientrusted.vo.MerchantUserUploadResVo;

public interface HelipayEntrustedService {

    MerchantUserUploadResVo bindUserCard(Long uid, String merchantAlias);
}
