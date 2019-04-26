package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.MessageHandle;
import com.mod.loan.util.heli.vo.request.BindCardPayVo;
import com.mod.loan.util.heli.vo.response.BindPayValidateCodeResponseVo;
import org.joda.time.DateTime;

import java.util.Map;

public class HelipayWithholdControllerTest {

    /**
     * 合利宝代扣接口测试
     */
    public static void withhold() {
        String uid = "7952288";
        // 支付流水号
        String repayNo = StringUtil.getOrderNumber("r");
        //还款金额不能小于0.11
        String amount = "0.12";
        BindCardPayVo requestVo = new BindCardPayVo();
        requestVo.setP1_bizType("QuickPayBindPay");
        //tb_merchant.hlb_id
        requestVo.setP2_customerNumber("C1800626358");
        //tb_user_bank.foreign_id bfab99218d0c40b2939dd0c0e66c2a0a
        requestVo.setP3_bindId("bfab99218d0c40b2939dd0c0e66c2a0a");
        requestVo.setP4_userId(uid);
        requestVo.setP5_orderId(repayNo);
        requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
        requestVo.setP7_currency("CNY");
        requestVo.setP8_orderAmount(amount);
        requestVo.setP9_goodsName("apple");
        requestVo.setP11_terminalType("UUID");
        requestVo.setP12_terminalId("122121212121");
        requestVo.setP13_orderIp("127.0.0.1");
        requestVo.setSignatureType("MD5WITHRSA");
        try {
            String pfxPath = "/data/conf/dawang.pfx";
            Map handleMap = MessageHandle.getReqestMap(requestVo, pfxPath, "wusl1q2a#@");
            String response = HttpClientService.getHttpResp(handleMap, "http://pay.trx.helipay.com/trx/quickPayApi/interface.action");
            System.out.println(response);
            BindPayValidateCodeResponseVo responseVo = JSONObject.parseObject(response, BindPayValidateCodeResponseVo.class);
            System.out.println(responseVo);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                System.out.println("success");
            } else {
                System.out.println("failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        withhold();
    }
}