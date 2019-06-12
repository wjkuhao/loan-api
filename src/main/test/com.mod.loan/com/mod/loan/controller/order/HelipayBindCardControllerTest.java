package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.MessageHandle;
import com.mod.loan.util.heli.vo.request.AgreementBindCardValidateCodeVo;
import com.mod.loan.util.heli.vo.response.AgreementSendValidateCodeResponseVo;
import org.joda.time.DateTime;

import java.util.Map;

public class HelipayBindCardControllerTest {

    /**
     * 合利宝代扣接口测试
     */
    public static void bindCard() {
        AgreementBindCardValidateCodeVo requestVo = null;
        String response = "";
        try {
            Long uid = 7951902l;
            requestVo = new AgreementBindCardValidateCodeVo();
            String bindNo = StringUtil.getOrderNumber("c");
            requestVo.setP1_bizType("AgreementPayBindCardValidateCode");
            requestVo.setP2_customerNumber("C1800473156");
            requestVo.setP3_userId(uid.toString());
            requestVo.setP4_orderId(bindNo);
            requestVo.setP5_timestamp(new DateTime().toString(TimeUtils.dateformat5));
            requestVo.setP6_cardNo("6228480329322167670");
            requestVo.setP7_phone("18557530599");
            requestVo.setP8_idCardNo("352225199307145556");
            requestVo.setP9_idCardType("IDCARD");
            requestVo.setP10_payerName("童官浦");
            requestVo.setP11_isEncrypt("true");
            requestVo.setP12_year("");
            requestVo.setP13_month("");
            requestVo.setP14_cvv2("");
            requestVo.setSignatureType("MD5WITHRSA");
            String pfxPath = "/data/conf/" +  "mx.pfx";
            Map handleMap = MessageHandle.getReqestMap(requestVo, pfxPath, "wusl1q2a#@");
            response = HttpClientService.getHttpResp(handleMap, "http://pay.trx.helipay.com/trx/quickPayApi/interface.action");
            System.out.println(response);
            AgreementSendValidateCodeResponseVo responseVo = JSONObject.parseObject(response,
                    AgreementSendValidateCodeResponseVo.class);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                requestVo.setBankCode("ABC");
                requestVo.setBankName("农业银行");

                System.out.println("success");
            } else {
                System.out.println("failed");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) {
        bindCard();
    }
}