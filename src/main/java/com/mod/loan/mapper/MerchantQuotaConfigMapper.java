package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MerchantQuotaConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MerchantQuotaConfigMapper extends MyBaseMapper<MerchantQuotaConfig> {

    List<MerchantQuotaConfig> selectByMerchant(@Param("merchant") String merchant);

    List<MerchantQuotaConfig> selectByBorrowType(@Param("merchant") String merchant, @Param("borrowType") Integer borrowType);

}