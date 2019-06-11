package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderRiskInfo;

public interface OrderRiskInfoMapper extends MyBaseMapper<OrderRiskInfo> {

    OrderRiskInfo getLastOneByOrderId(Long orderId);

    OrderRiskInfo getLastOneByPhone(String userPhone);
}