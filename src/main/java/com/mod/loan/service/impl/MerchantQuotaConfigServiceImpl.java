package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.MerchantQuotaConfigMapper;
import com.mod.loan.model.MerchantQuotaConfig;
import com.mod.loan.model.OrderRiskInfo;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantQuotaConfigServiceImpl extends BaseServiceImpl<MerchantQuotaConfig, Integer> implements MerchantQuotaConfigService {

    private static Logger logger = LoggerFactory.getLogger(MerchantQuotaConfigServiceImpl.class);

    private final MerchantQuotaConfigMapper merchantQuotaConfigMapper;
    private final OrderDeferService orderDeferService;
    private final OrderService orderService;
    private final OrderRiskInfoService orderRiskInfoService;
    @Autowired
    UserService userService;
    @Autowired
    RedisMapper redisMapper;

    @Autowired
    public MerchantQuotaConfigServiceImpl(MerchantQuotaConfigMapper merchantQuotaConfigMapper, OrderDeferService orderDeferService, OrderService orderService, OrderRiskInfoService orderRiskInfoService) {
        this.merchantQuotaConfigMapper = merchantQuotaConfigMapper;
        this.orderDeferService = orderDeferService;
        this.orderService = orderService;
        this.orderRiskInfoService = orderRiskInfoService;
    }


    @Override
    public List<MerchantQuotaConfig> selectByMerchant(String merchant) {
        return merchantQuotaConfigMapper.selectByMerchant(merchant);
    }

    private Integer matchQuotaConfig(List<MerchantQuotaConfig> listQuotaConfig, String actualValue) {
        Integer quotaValue = 0;
        if (listQuotaConfig == null || actualValue == null) {
            return quotaValue;
        }

        try {
            for (MerchantQuotaConfig quotaConfig : listQuotaConfig) {
                String comparator = quotaConfig.getComparator();

                if ("range".equals(comparator)) {
                    String[] presetValues = quotaConfig.getPresetValue().split("-");
                    if (Double.valueOf(actualValue).compareTo(Double.valueOf(presetValues[0])) >= 0
                            && Double.valueOf(actualValue).compareTo(Double.valueOf(presetValues[1])) < 0) {
                        quotaValue = quotaConfig.getQuotaValue();
                        break;
                    }
                } else if ("eq".equals(comparator)) {
                    if (actualValue.compareTo(quotaConfig.getPresetValue()) == 0) {
                        quotaValue = quotaConfig.getQuotaValue();
                        break;
                    }
                }
            }
        }catch (Exception e){
            logger.error("matchQuotaConfig error {}", (Object) e.getStackTrace());
        }

        return quotaValue;
    }

    @Override
    public BigDecimal computeQuota(String merchant, Long uid, BigDecimal basicQuota) {
        BigDecimal lastQuota = basicQuota;
        try {
            String userPromoteQuota = redisMapper.get("quota:"+ uid);
            if (StringUtils.isNotEmpty(userPromoteQuota)) {
                return basicQuota.add(new BigDecimal(userPromoteQuota));
            }

            //加载提额配置
            List<MerchantQuotaConfig> merchantQuotaConfigs = selectByMerchant(merchant);
            if (merchantQuotaConfigs==null || merchantQuotaConfigs.isEmpty()){
                return lastQuota;
            }

            HashMap<Integer, List<MerchantQuotaConfig>> merchantQuotaConfigsMap = merchantQuotaConfigs.stream().collect(
                    Collectors.groupingBy(MerchantQuotaConfig::getQuotaType, HashMap::new, Collectors.toList())
            );

            //天机分
            List<MerchantQuotaConfig> tianjiQuotaConfigs = merchantQuotaConfigsMap.get(MerchantQuotaConfigService.QUOTA_TYPE_TIANJI_SCORE);
            Integer tianjiQuota = getTianjiQuota(tianjiQuotaConfigs, uid);

            //展期次数
            List<MerchantQuotaConfig> deferQuotaConfigs = merchantQuotaConfigsMap.get(MerchantQuotaConfigService.QUOTA_TYPE_DEFER_COUNT);
            Integer deferQuota = getDeferQuota(deferQuotaConfigs, uid);


            //借款金额区间范围控制在1000-5000
            int promoteQuota = tianjiQuota + deferQuota;
            redisMapper.set("quota:"+uid, String.valueOf(promoteQuota),60*60*24);

            lastQuota = lastQuota.add(new BigDecimal(promoteQuota)) ;
            if (lastQuota.compareTo(new BigDecimal(1000))<0){
                lastQuota = new BigDecimal(1000);
            }
            else if (lastQuota.compareTo(new BigDecimal(Constant.MERCHANT_MAX_PRODUCT_MONEY))>0){
                lastQuota = new BigDecimal(Constant.MERCHANT_MAX_PRODUCT_MONEY);
            }
        }catch (Exception e){
            logger.error("computeQuota err uid={}, error={}",uid, e);
        }

        return lastQuota;
    }

    //天机分对应提额
    private Integer getTianjiQuota(List<MerchantQuotaConfig> tianjiQuotaConfigs,  Long uid){
        Integer tianjiQuota = 0;
        int repaySuccessCount = 0;
        try {
            User user = userService.selectByPrimaryKey(uid);
            OrderRiskInfo orderRiskInfo = orderRiskInfoService.getLastOneByPhone(user.getUserPhone());
            if (orderRiskInfo==null) {
                return 0;
            }

            String riskModelScore = orderRiskInfo.getRiskModelScore();
            JSONObject riskModelScoreJson = JSON.parseObject(riskModelScore);
            String tianjiScore = riskModelScoreJson.getString("天机-小额模型分");

            tianjiQuota = matchQuotaConfig(tianjiQuotaConfigs, tianjiScore);

            repaySuccessCount = orderService.countPaySuccessByUid(uid);
            //新客默认设置一次，方便按天机分匹配额度
            if (repaySuccessCount==0){
                repaySuccessCount=1;
            }
        }
        catch(Exception e){
            logger.error("getTianjiQuota error orderId={},err={}",uid, e);
        }
        //按还款成功次数提额
        return tianjiQuota*repaySuccessCount;
    }

    //展期次数提额
    private Integer getDeferQuota(List<MerchantQuotaConfig> deferQuotaConfigs,  Long uid){
        Integer deferSuccessCount = orderDeferService.deferSuccessCount(uid);
        return  matchQuotaConfig(deferQuotaConfigs, String.valueOf(deferSuccessCount));
    }
}
