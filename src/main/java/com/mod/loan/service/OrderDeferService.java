package com.mod.loan.service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.OrderDefer;

import java.util.List;

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

    List<OrderDefer> selectOrderDefer();
}
