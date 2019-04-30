package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.model.MerchantConfig;
import com.mod.loan.model.UserInfo;
import com.mod.loan.service.MerchantConfigService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MerchantConfigServiceImpl extends BaseServiceImpl<MerchantConfig,Integer> implements MerchantConfigService {

    private static Logger logger = LoggerFactory.getLogger(MerchantConfigServiceImpl.class);

    @Override
    public boolean includeRejectKeyword(String merchant, UserInfo userInfo){
        MerchantConfig merchantConfigQry = new MerchantConfig();
        merchantConfigQry.setMerchant(merchant);
        MerchantConfig merchantConfig = selectOne(merchantConfigQry);

        try {
            String rejectKeyword = merchantConfig.getRejectKeyword();
            if (StringUtils.isNotEmpty(rejectKeyword)){
                String[] keywords = rejectKeyword.split(",");
                String workAddress = userInfo.getWorkAddress();
                String liveAddress = userInfo.getLiveAddress();
                String workCompany = userInfo.getWorkCompany();

                for (String keyword : keywords) {
                    if(workAddress.indexOf(keyword)>0
                      ||workCompany.indexOf(keyword)>0
                      || liveAddress.indexOf(keyword)>0){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            logger.error("includeRejectKeyword error={}", (Object) e.getStackTrace());
        }
        return false;

    }
}
