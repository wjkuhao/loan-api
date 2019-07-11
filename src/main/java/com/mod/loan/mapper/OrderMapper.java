package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Order;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OrderMapper extends MyBaseMapper<Order> {

    int countLoaningOrderByUid(@Param("uid") Long uid);

    Order findUserLatestOrder(Long uid);

    List<Order> getByUid(Long uid);

    Integer judgeUserTypeByUid(Long uid);

    Integer countPaySuccessByUid(Long uid);

    List<Order> findOverdueOrder();

    Order findOneOverdueOrder(Long uid);

}