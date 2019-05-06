package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.OrderDefer;

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

}
