package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.OrderRepay;

import java.util.List;

public interface OrderChangjieRepayService extends BaseService<OrderRepay, String> {

    /**
     * 畅捷订单协议支付还款发送验证码
     *
     * @param orderId 订单id
     */
    String bindBankCard4RepaySendMsg(Long orderId);

    /**
     * 畅捷订单协议支付还款确认
     *
     * @param seriesNo 协议支付的流水号
     * @param smsCode  短信验证码
     */
    String bindBankCard4RepayConfirm(String seriesNo, String smsCode);

    /**
     * 畅捷订单协议支付还款结果查询
     *
     * @param repayNo 还款流水号
     */
    void bindBankCard4RepayQuery(String repayNo);

    /**
     * 畅捷代扣还款订单--定时任务
     *
     * @return
     */
    List<OrderRepay> changjieRepayQuery4Task();
}
