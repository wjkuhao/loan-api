package com.mod.loan.service.impl;

import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.OrderDeferMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("orderDeferService")
public class OrderDeferServiceImpl extends BaseServiceImpl<OrderDefer, Integer> implements OrderDeferService {

    private static Logger logger = LoggerFactory.getLogger(OrderDeferServiceImpl.class);

    private final OrderDeferMapper orderDeferMapper;
    private final OrderService orderService;
    private final MerchantService merchantService;
    private final UserBankService userBankService;
    private final YeepayService yeepayService;

    @Value("${yeepay.defer.callback.url:}")
    String yeepay_defer_callback_url;

    @Autowired
    public OrderDeferServiceImpl(OrderDeferMapper orderDeferMapper,
                                 OrderService orderService, MerchantService merchantService, UserBankService userBankService, YeepayService yeepayService) {
        this.orderDeferMapper = orderDeferMapper;
        this.orderService = orderService;
        this.merchantService = merchantService;
        this.userBankService = userBankService;
        this.yeepayService = yeepayService;
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
        orderDeferMapper.updateByPrimaryKeySelective(orderDefer);
    }

    @Override
    public String yeepayDeferNoSms(Long orderId) {
        OrderDefer orderDefer = findLastValidByOrderId(orderId);
        if (orderDefer == null){
            logger.error("orderId={}，找不到对应的展期订单", orderId);
            return "找不到对应的展期订单";        }

        if (orderDefer.getPayStatus() == 0) {
            logger.error("orderId={}已存在展期还款中的记录", orderId);
            return "请勿重复还款";
        }

        try {
            String payNo = StringUtil.getOrderNumber("d");// 支付流水号
            String amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : orderDefer.getDeferFee().toString();
            Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
            UserBank userBank = userBankService.selectUserCurrentBankCard(orderDefer.getUid());

            String err = yeepayService.payRequest(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(),
                    payNo, String.valueOf(orderDefer.getUid()), userBank.getCardNo(), amount, false, yeepay_defer_callback_url);

            if (err != null) {
                orderDefer.setPayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
                orderDefer.setRemark("展期易宝受理失败:" + err);
                orderDeferMapper.updateByPrimaryKey(orderDefer);
                return err;
            }

            orderDefer.setPayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderDefer.setRemark("展期易宝受理成功");
            orderDeferMapper.updateByPrimaryKey(orderDefer);
            return null;
        } catch (Exception e) {
            logger.error("展期易宝支付受理异常，error={}", e.getMessage());
            return "展期易宝支付受理异常";
        }
    }

    @Override
    public String yeepayRepayQuery(String repayNo, String merchantAlias) {
        Merchant merchant = merchantService.findMerchantByAlias(merchantAlias);
        return yeepayService.repayQuery(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(), repayNo, null);
    }

    @Override
    public OrderDefer selectByPayNo(String payNo) {
        return orderDeferMapper.selectByPayNo(payNo);
    }

}
