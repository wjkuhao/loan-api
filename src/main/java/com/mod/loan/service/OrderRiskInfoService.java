package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.OrderRiskInfo;

public interface OrderRiskInfoService extends BaseService<OrderRiskInfo, Long> {

        OrderRiskInfo getLastOneByOrderId(Long orderId);

        OrderRiskInfo getLastOneByPhone(String userPhone);
}
