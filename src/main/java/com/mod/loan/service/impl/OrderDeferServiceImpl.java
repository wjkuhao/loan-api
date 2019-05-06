package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.OrderDeferMapper;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.service.OrderDeferService;
import com.mod.loan.service.OrderService;
import com.mod.loan.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orderDeferService")
public class OrderDeferServiceImpl extends BaseServiceImpl<OrderDefer, Integer> implements OrderDeferService {

    private final OrderDeferMapper orderDeferMapper;
    private final OrderService orderService;

    @Autowired
    public OrderDeferServiceImpl(OrderDeferMapper orderDeferMapper,
                                 OrderService orderService) {
        this.orderDeferMapper = orderDeferMapper;
        this.orderService = orderService;
    }

    @Override
    public OrderDefer findLastValidByOrderId(Long orderId) {
        return orderDeferMapper.findLastValidByOrderId(orderId, TimeUtil.nowDate());
    }

    @Override
    public void modifyOrderDeferByPayCallback(OrderDefer orderDefer) {
        // 修改订单的还款日期
        Order modifiedOrder = new Order();
        modifiedOrder.setId(orderDefer.getOrderId());
        modifiedOrder.setRepayTime(TimeUtil.parseDate(orderDefer.getDeferRepayDate()));
        orderService.updateByPrimaryKeySelective(modifiedOrder);
        // 修改续期单 支付时间和支付状态
        orderDefer.setPayTime(TimeUtil.nowTime());
        orderDefer.setPayStatus(1);
        orderDeferMapper.updateByPrimaryKeySelective(orderDefer);
    }

}
