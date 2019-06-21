package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.MerchantQuotaConfig;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantQuotaConfigService extends BaseService<MerchantQuotaConfig, Integer> {


    int QUOTA_TYPE_TIANJI_SCORE = 1;
    int QUOTA_TYPE_DEFER_COUNT = 2;

    /**
     * 通过商户alias查询对应的配置
     *
     * @param merchant alias
     * @return 该商户的配置
     */
    List<MerchantQuotaConfig> selectByMerchant(String merchant);

    /**
     * @param borrowType: merchantRate表对应的借款次数
     */
    List<MerchantQuotaConfig> selectByBorrowType(String merchant, Integer borrowType);
    /**
     * 通过借款测试计算出该用户的提额金额
     */
    BigDecimal computeQuota(String merchant, Long uid, BigDecimal basicQuota, Integer borrowType);

}

