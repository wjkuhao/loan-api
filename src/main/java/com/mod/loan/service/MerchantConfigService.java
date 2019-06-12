package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.MerchantConfig;
import com.mod.loan.model.UserInfo;

public interface MerchantConfigService extends BaseService<MerchantConfig, Integer> {
    /**
     * 用户地址、公司是否包含配置的拒绝关键字
     *
     * @param merchant 商户简写
     * @param userInfo 用户信息
     * @return true 表示包含
     */
    boolean includeRejectKeyword(String merchant, UserInfo userInfo);

    /**
     * 通过商户alias查询对应的配置
     *
     * @param merchant alias
     * @return 该商户的配置
     */
    MerchantConfig selectByMerchant(String merchant);
}
