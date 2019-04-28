package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.Disguiser;
import com.mod.loan.util.heli.util.HeliPayBeanUtils;
import com.mod.loan.util.heli.vo.request.QueryMerchantAccountVo;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;

public class HelipayBanlanceQueryControllerTest {

    /**
     * 合利宝商户端签名
     */
    private static final String KEY = "iNIhq85HBpPOHzQpPOGtiPY0gsegB1112";

    /**
     * 合利宝商户余额查询
     */
    public static void banlanceQuery() throws Exception {
        QueryMerchantAccountVo requestVo = new QueryMerchantAccountVo();
        requestVo.setP1_bizType("MerchantAccountQuery");
        //tb_merchant.hlb_id
        requestVo.setP2_customerNumber("C1800626358");
        requestVo.setP3_timestamp(new DateTime().toString(TimeUtils.dateformat5));
        Map<String, String> map = HeliPayBeanUtils.convertBean(requestVo, new LinkedHashMap());
        //拼接上商户的签名
        String oriMessage = HeliPayBeanUtils.getSigned(map, null) + KEY;
        System.out.println("签名原文串：" + oriMessage);
        String sign = Disguiser.disguiseMD5(oriMessage.trim());
        System.out.println("签名串：" + sign);
        map.put("sign", sign);
        System.out.println("发送参数：" + map);
        String response = HttpClientService.getHttpResp(map, "http://pay.trx.helipay.com/trx/merchant/interface.action");
        //{"rt12_desc":"","rt9_T1Balance":"161919.61","rt14_rechargeBalance":"0.00","sign":"b771a202e8d7850f6f25ca302eb44462","rt1_bizType":"MerchantAccountQuery","rt2_retCode":"0000","rt8_d0Balance":"225399.74","rt15_amountToBeSettled":"161919.61","rt5_accountStatus":"AVAILABLE","rt6_balance":"387319.35","rt7_frozenBalance":"0.00","rt11_createDate":"2019-03-12 10:06:28","rt10_currency":"CNY","rt4_customerNumber":"C1800626358","rt13_d1Balance":"161919.61","rt3_retMsg":"成功"}
        System.out.println("响应结果：" + response);
        JSONObject data = JSONObject.parseObject(response);
        if ("0000".equals(data.getString("rt2_retCode"))) {
            System.out.println("账户余额:" + data.getString("rt6_balance"));
            System.out.println("可结算金额:" + data.getString("rt15_amountToBeSettled"));
            System.out.println("待结算金额:" + data.getString("rt8_d0Balance"));
        } else {
            System.out.println("failed");
        }
    }

    public static void main(String[] args) throws Exception {
        banlanceQuery();
    }
}