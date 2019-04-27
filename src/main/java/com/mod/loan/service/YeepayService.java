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
    String authBindCardRequest(String appKey, String privateKey, String requestNo, String identityId, String cardNo,
                             String certNo, String userName, String cardPhone);

    /**
     * 鉴权绑卡确认
     * @param requestNo 鉴权绑卡请求时生成的流水号
     * @param validateCode 短信验证码
     * @return 错误信息
     */
    String authBindCardConfirm(String appKey, String privateKey, String requestNo, String validateCode);

    /**
     * 绑卡支付请求
     * @param requestNo 流水号
     * @param identityId 商户生成的用户唯一标识。每个用户唯一，绑卡最终会绑在这个用户标志下
     * @param cardNo 身份证号
     * @param amount 支付单位：元，精确到两位小数，大于等于 0.01
     * @return 错误信息
     */
    String payRequest(String appKey, String privateKey, String requestNo, String identityId, String cardNo, String amount, boolean sendSms);

    /**
     * 绑卡支付确认
     * @param requestNo 绑卡支付请求时生成的流水号
     * @param validateCode 短信验证码
     * @return 错误信息
     */
    String payConfirm(String appKey, String privateKey, String requestNo, String validateCode);

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

    /**
     * 多账号，需要指定私钥
     * @param strPrivateKey 私钥字符串
     * @param responseMsg 易宝返回数据
     * @param requestNo 输出参数：申请时的还款订单
     * @return 错误信息
     */
    String repayCallbackMultiAcct(String strPrivateKey, String responseMsg, StringBuffer requestNo);

    /**
     * 查询订单状态
     * @param requestNo 申请流水号
     * @param yborderid 易宝的流水号
     * @return 错误信息
     */
    String repayQuery(String appKey, String privateKey, String requestNo, String yborderid);

    /**
     * 放款
     * @param groupNo 商户编号
     * @param appKey 放款app key
     * @param privateKey 放款私钥
     * @param batchNo 批次号 15-20数字
     * @param orderId 订单号 可以是字母
     * @param amount 金额
     * @param accountName 收款人姓名
     * @param accountNumber 收款人卡号
     * @param bankCode 收款人银行卡编码
     * @return 错误信息
     */
    String payToCustom(String groupNo, String appKey, String privateKey, String batchNo, String orderId, String amount,
                              String accountName, String accountNumber, String bankCode);

    /**
     * @param groupNo 商户编号
     * @param appKey 放款app key
     * @param privateKey 放款私钥
     * @param batchNo 批次号 15-20数字
     * @return 错误信息
     */
    String payToCustomQuery(String groupNo, String appKey, String privateKey, String batchNo);

    String balanceQuery(String appKey, String privateKey, StringBuffer balance);

}

