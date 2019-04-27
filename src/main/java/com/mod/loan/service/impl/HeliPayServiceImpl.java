package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.model.Merchant;
import com.mod.loan.service.HeliPayService;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.Disguiser;
import com.mod.loan.util.heli.util.HeliPayBeanUtils;
import com.mod.loan.util.heli.vo.request.QueryMerchantAccountVo;
import com.mod.loan.util.heli.vo.response.QueryMerchantAccountResponseVo;
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
            log.info("响应结果：" + response);
            QueryMerchantAccountResponseVo responseVo = JSONObject.parseObject(response, QueryMerchantAccountResponseVo.class);
            log.info("responseVo:{}", responseVo);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                log.info("success,账户余额:" + responseVo.getRt6_balance());
                balance.append(responseVo.getRt6_balance());
                return null;
            } else {
                return responseVo.getRt3_retMsg();
            }
        } catch (Exception e) {
            log.error("合利宝商户余额查询失败:error:{}", e);
            return "合利宝商户余额查询失败:" + e.getMessage();
        }
    }


}