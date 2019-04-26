package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.Disguiser;
import com.mod.loan.util.heli.util.HeliPayBeanUtils;
import com.mod.loan.util.heli.vo.request.QueryMerchantAccountVo;
import com.mod.loan.util.heli.vo.response.QueryMerchantAccountResponseVo;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;

public class HelipayBanlanceQueryControllerTest {

    /**
     * 合利宝商户端签名
     * */
    private static final String KEY = "iNIhq85HBpPOHzQpPOGtiPdsfsfsdf";

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
        System.out.println("响应结果：" + response);
        QueryMerchantAccountResponseVo responseVo = JSONObject.parseObject(response, QueryMerchantAccountResponseVo.class);
        System.out.println(responseVo);
        if ("0000".equals(responseVo.getRt2_retCode())) {
            System.out.println("success,账户余额:" + responseVo.getRt6_balance());
        } else {
            System.out.println("failed");
        }
    }

    public static void main(String[] args) throws Exception {
        banlanceQuery();
    }
}