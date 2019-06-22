package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderRepay;

import java.util.List;

public interface OrderRepayMapper extends MyBaseMapper<OrderRepay> {

    int countRepaySuccess(Long orderId);

    List<OrderRepay> selectReapyingOrder();

    OrderRepay selectLastByOrderId(Long orderId);

    /**
     * 畅捷代扣还款订单--定时任务
     *
     * @return
     */
    List<OrderRepay> changjieRepayQuery4Task();

    /**
     * 汇潮支付宝还款/微信扫码支付结果查询--定时任务
     *
     * @return
     */
    List<OrderRepay> huichaoRepay4AliAppH5OrWxScanQuery();
}