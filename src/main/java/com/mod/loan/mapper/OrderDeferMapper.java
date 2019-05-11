package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderDefer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface OrderDeferMapper extends MyBaseMapper<OrderDefer> {

    @ResultMap("BaseResultMap")
    @Select("SELECT * FROM tb_order_defer WHERE order_id = #{orderId} AND repay_date <= #{nowDate} ORDER BY id DESC LIMIT 1")
    OrderDefer findLastValidByOrderId(@Param("orderId") Long orderId,
                                      @Param("nowDate") String nowDate);

    @ResultMap("BaseResultMap")
    @Select("SELECT * FROM tb_order_defer WHERE pay_no = #{payNo}")
    OrderDefer selectByPayNo(@Param("payNo")String payNo);
}
