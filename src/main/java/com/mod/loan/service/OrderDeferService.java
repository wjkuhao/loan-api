package com.mod.loan.service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.OrderDefer;

import java.util.List;
import java.util.Map;

public interface OrderDeferService extends BaseService<OrderDefer, Integer> {

    /**
     * 根据订单id查询最近的一笔有效续期单
     *
     * @param orderId 订单id
     * @return null 或者 最新一笔有效续期单
     */
    OrderDefer findLastValidByOrderId(Long orderId);

    /**
     * 续期单支付成功以后 更新订单以及续期单
     *
     * @param orderDefer 原始续期但 线上支付要设置好支付单号
     */
    void modifyOrderDeferByPayCallback(OrderDefer orderDefer);

    /**
     * 易宝展期支付申请
     * @param orderId 订单号
     * @return 错误信息
     */
    String yeepayDeferNoSms(Long orderId);

    /**
     * @param payNo 支付订单号
     * @param merchantAlias 商户简称
     * @return 错误信息
     */
    String yeepayRepayQuery(String payNo, String merchantAlias);

    OrderDefer selectByPayNo(String payNo);

    /**
     * 用户详情中展期订单数据查询
     * */
    JSONObject userDeferDetail(Long uid);

    /**
     * 获取最近2分钟展期订单支付详情
     * */
    List<OrderDefer> selectOrderDefer();

    /**
     * 该用户在该商户下展期成功次数
     * */
    Integer deferSuccessCount(Long uid);

    /**
     * 畅捷续期时协议支付还款发送验证码
     *
     * @param orderId 订单id
     */
    String changjieDeferRepay4SendMsg(Long orderId);

    /**
     * 畅捷续期时协议支付还款确认
     *
     * @param seriesNo 协议支付的流水号
     * @param smsCode  短信验证码
     */
    String changjieDeferRepay4Confirm(String seriesNo, String smsCode);

    /**
     * 畅捷续期时协议支付还款结果查询
     *
     * @param repayNo 还款流水号
     */
    String changjieDeferRepay4Query(String repayNo);

    /**
     * 畅捷续期时协议支付还款异步回调
     *
     * @param map
     * @param sign
     */
    void changjieDeferRepayCallback(Map<String, String> map, String sign);

    /**
     * 快钱续期时支付还款
     *
     * @param orderId 订单id
     */
    ResultMessage kuaiqianDeferRepay(Long orderId);

    /**
     * 快钱续期时支付还款结果查询
     *
     * @param orderId 订单id
     */
    ResultMessage kuaiqianDeferRepayQuery(Long orderId);
}
