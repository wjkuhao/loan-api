package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.model.Merchant;
import com.mod.loan.service.HeliPayService;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.Disguiser;
import com.mod.loan.util.heli.util.HeliPayBeanUtils;
import com.mod.loan.util.heli.vo.request.QueryMerchantAccountVo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HeliPayServiceImpl implements HeliPayService {

    private static Logger log = LoggerFactory.getLogger(HeliPayServiceImpl.class);

    @Value("${helipay.merchant.url:}")
    private String helipay_merchant_url;

    /**
     * 合利宝查询商户余额
     */
    @Override
    public String balanceQuery(Merchant merchant, StringBuffer balance) {
        try {
            if (StringUtils.isEmpty(merchant.getHlbMerchantSign())) {
                return "合利宝商户端签名sign为空,merchant:" + merchant.getMerchantAlias();
            }
            QueryMerchantAccountVo requestVo = new QueryMerchantAccountVo();
            requestVo.setP1_bizType("MerchantAccountQuery");
            //tb_merchant.hlb_id
            requestVo.setP2_customerNumber(merchant.getHlb_id());
            requestVo.setP3_timestamp(new DateTime().toString(TimeUtils.dateformat5));
            Map<String, String> map = HeliPayBeanUtils.convertBean(requestVo, new LinkedHashMap());
            //拼接上商户的签名
            String oriMessage = HeliPayBeanUtils.getSigned(map, null) + merchant.getHlbMerchantSign();
            log.info("签名原文串：" + oriMessage);
            String sign = Disguiser.disguiseMD5(oriMessage.trim());
            log.info("签名串：" + sign);
            map.put("sign", sign);
            log.info("发送参数：" + map);
            String response = HttpClientService.getHttpResp(map, helipay_merchant_url);
            //{"rt12_desc":"","rt9_T1Balance":"161919.61","rt14_rechargeBalance":"0.00","sign":"b771a202e8d7850f6f25ca302eb44462","rt1_bizType":"MerchantAccountQuery","rt2_retCode":"0000","rt8_d0Balance":"225399.74","rt15_amountToBeSettled":"161919.61","rt5_accountStatus":"AVAILABLE","rt6_balance":"387319.35","rt7_frozenBalance":"0.00","rt11_createDate":"2019-03-12 10:06:28","rt10_currency":"CNY","rt4_customerNumber":"C1800626358","rt13_d1Balance":"161919.61","rt3_retMsg":"成功"}
            log.info("响应结果：" + response);
            JSONObject data = JSONObject.parseObject(response);
            if ("0000".equals(data.getString("rt2_retCode"))) {
                balance.append("账户余额:" + data.getString("rt6_balance"));
                balance.append("可结算金额:" + data.getString("rt15_amountToBeSettled"));
                balance.append("待结算金额:" + data.getString("rt8_d0Balance"));
                log.info("合利宝商户查询->{}", balance.toString());
                return null;
            } else {
                return data.getString("rt3_retMsg");
            }
        } catch (Exception e) {
            log.error("合利宝商户余额查询失败:error:{}", e);
            return "合利宝商户余额查询失败:" + e.getMessage();
        }
    }

}