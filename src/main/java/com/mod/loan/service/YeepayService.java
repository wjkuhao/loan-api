package com.mod.loan.service;

import com.yeepay.g3.sdk.yop.client.YopResponse;

public interface YeepayService {

    /**
     * 鉴权绑卡请求
     * @param requestNo 流水号
     * @param identityId 商户生成的用户唯一标识。每个用户唯一，绑卡最终会绑在这个用户标志下
     * @param cardNo 银行卡号
     * @param certNo 身份证号
     * @param userName 持卡人姓名
     * @param cardPhone 银行卡预留手机号
     * @return 错误信息
     */
    String authBindCardRequest(String requestNo, String identityId, String cardNo,
                             String certNo, String userName, String cardPhone);

    /**
     * 鉴权绑卡确认
     * @param requestNo 鉴权绑卡请求时生成的流水号
     * @param validateCode 短信验证码
     * @return 错误信息
     */
    String authBindCardConfirm(String requestNo, String validateCode);

    /**
     * 绑卡支付请求
     * @param requestNo 流水号
     * @param identityId 商户生成的用户唯一标识。每个用户唯一，绑卡最终会绑在这个用户标志下
     * @param cardNo 身份证号
     * @param amount 支付单位：元，精确到两位小数，大于等于 0.01
     * @return 错误信息
     */
    String payRequest(String requestNo, String identityId, String cardNo, String amount);

    /**
     * 绑卡支付确认
     * @param requestNo 绑卡支付请求时生成的流水号
     * @param validateCode 短信验证码
     * @return 错误信息
     */
    String payConfirm(String requestNo, String validateCode);

    /**
     * 返回结果解析
     * @param response 返回对象
     * @param validateKey 需要验证的key 一般为状态字段
     * @param validateValue 需要验证的value
     * @return 错误信息
     */
    String parseResult(YopResponse response, String validateKey, String validateValue);

    /**
     * @param responseMsg 易宝返回数据
       @param requestNo 输出参数：申请时的还款订单
     * @return 错误信息
     */
    String repayCallback(String responseMsg, StringBuffer requestNo);
}

