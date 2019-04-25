package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.model.MerchantDeferConfig;
import com.mod.loan.service.MerchantDeferConfigService;
import org.springframework.stereotype.Service;

@Service("merchantDeferConfigService")
public class MerchantDeferConfigServiceImpl extends BaseServiceImpl<MerchantDeferConfig, Integer> implements MerchantDeferConfigService {
}
