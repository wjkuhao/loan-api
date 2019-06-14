package com.mod.loan.service;

import com.mod.loan.model.request.*;

/**
 * @author NIELIN
 * @version $Id: ChangjieRepayService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface ChangjieRepayService {

    /**
     * 鉴权绑卡请求（API）
     *
     * @param request
     * @return
     */
    String bindBankCard4SendMsg(BindBankCard4SendMsgRequest request);

    /**
     * 鉴权绑卡确认接口（API）
     *
     * @param request
     * @return
     */
    String bindBankCard4Confirm(BindBankCard4ConfirmRequest request);

    /**
     * 鉴权解绑接口（API）
     *
     * @param request
     * @return
     */
    String bindBankCard4Unbind(BindBankCard4UnbindRequest request);

    /**
     * 绑卡查询接口
     *
     * @param request
     * @return
     */
    String bindBankCard4Query(BindBankCard4QueryRequest request);

    /**
     * 短信验证码重发接口
     *
     * @param request
     * @return
     */
    String bindBankCard4ResendMsg(BindBankCard4ResendMsgRequest request);

    /**
     * 协议支付还款发送验证码
     *
     * @param request
     * @return
     */
    String bindBankCard4RepaySendMsg(BindBankCard4RepaySendMsgRequest request);

    /**
     * 协议支付还款确认
     *
     * @param request
     * @return
     */
    String bindBankCard4RepayConfirm(BindBankCard4RepayConfirmRequest request);

    /**
     * 协议支付还款结果查询
     *
     * @param request
     * @return
     */
    String bindBankCard4RepayQuery(TransCode4QueryRequest request);


}
