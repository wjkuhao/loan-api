package com.mod.loan.mapper;

import java.util.List;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Order;

public interface OrderMapper extends MyBaseMapper<Order> {
	
	Order findUserLatestOrder(Long uid);

	List<Order> getByUid(Long uid);

	Integer judgeUserTypeByUid(Long uid);

	Integer countPaySuccessByUid(Long uid);

	List<Order> findOverdueOrder();
}