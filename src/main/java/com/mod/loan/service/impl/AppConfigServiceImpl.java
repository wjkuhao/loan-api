package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.AppArticleMapper;
import com.mod.loan.mapper.AppConfigMapper;
import com.mod.loan.mapper.MerchantConfigMapper;
import com.mod.loan.model.AppConfig;
import com.mod.loan.model.MerchantConfig;
import com.mod.loan.model.UserInfo;
import com.mod.loan.service.AppConfigService;
import com.mod.loan.service.MerchantConfigService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppConfigServiceImpl extends BaseServiceImpl<AppConfig, Integer> implements AppConfigService {

    private static Logger logger = LoggerFactory.getLogger(AppConfigServiceImpl.class);

    @Autowired
    private AppConfigMapper appConfigMapper;
    @Override
    public AppConfig selectByClientAlias(String clientAlias) {
        return appConfigMapper.selectByClientAlias(clientAlias);
    }
}
