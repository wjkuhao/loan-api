package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MerchantConfig;
import org.apache.ibatis.annotations.Param;

public interface MerchantConfigMapper extends MyBaseMapper<MerchantConfig> {

    MerchantConfig selectByMerchant(@Param("merchant") String merchant);

}