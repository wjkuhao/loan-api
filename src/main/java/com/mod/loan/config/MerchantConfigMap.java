package com.mod.loan.config;

import com.mod.loan.model.MerchantConfig;
import com.mod.loan.service.MerchantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MerchantConfigMap implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantConfigMap.class);

    private final MerchantConfigService merchantConfigService;
    private Map<String, MerchantConfig> merchantConfigMap = new ConcurrentHashMap<>();

    @Autowired
    public MerchantConfigMap(MerchantConfigService merchantConfigService) {
        this.merchantConfigService = merchantConfigService;
    }

    private void initMerchantConfigMap(){
        List<MerchantConfig> merchantConfigs = merchantConfigService.selectAll();
        for (MerchantConfig merchantConfig : merchantConfigs) {
            merchantConfigMap.put(merchantConfig.getMerchant(), merchantConfig);
        }
    }

    //3小时刷新一次
    @Scheduled(fixedDelay = 1000*60*60*3)
    public void flush() {
        initMerchantConfigMap();
        LOGGER.info("----------------flush MerchantConfigMap-------------------");
    }

    @Override
    public void afterPropertiesSet() {
        initMerchantConfigMap();
    }

    public MerchantConfig get(String merchant){
        return merchantConfigMap.get(merchant);
    }
}
