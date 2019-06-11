package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderRepay;

import java.util.List;

public interface OrderRepayMapper extends MyBaseMapper<OrderRepay> {

    int countRepaySuccess(Long orderId);

    List<OrderRepay> selectReapyingOrder();

    OrderRepay selectLastByOrderId(Long orderId);
}