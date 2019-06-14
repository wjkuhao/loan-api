package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderRiskInfo;
import org.apache.ibatis.annotations.Param;

public interface OrderRiskInfoMapper extends MyBaseMapper<OrderRiskInfo> {

    OrderRiskInfo getLastOneByOrderId(@Param("orderId")Long orderId);

    OrderRiskInfo getLastOneByPhone(@Param("userPhone") String userPhone);
}