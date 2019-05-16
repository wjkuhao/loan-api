package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
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

import java.math.BigDecimal;

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
        return orderDeferMapper.findLastValidByOrderId(orderId);
    }

    @Override
    public void modifyOrderDeferByPayCallback(OrderDefer orderDefer) {
        // 修改订单的还款日期
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            Order modifiedOrder = orderService.selectByPrimaryKey(orderDefer.getOrderId());
            modifiedOrder.setRepayTime(TimeUtil.parseDate(orderDefer.getDeferRepayDate()));
            modifiedOrder.setOverdueFee(new BigDecimal(0));
            modifiedOrder.setOverdueDay(0);
            modifiedOrder.setShouldRepay(modifiedOrder.getBorrowMoney());

            Integer status = modifiedOrder.getStatus();
            if (status.equals(OrderEnum.REPAYING.getCode()) || status.equals(OrderEnum.DEFER_OVERDUE.getCode())) {
                modifiedOrder.setStatus(OrderEnum.DEFER.getCode());
            } else if (status.equals(OrderEnum.OVERDUE.getCode())) {
                modifiedOrder.setStatus(OrderEnum.OVERDUE_DEFER.getCode());
            }
            orderService.updateByPrimaryKeySelective(modifiedOrder);
        }
        // 修改续期单 支付时间和支付状态
        orderDefer.setPayTime(TimeUtil.nowTime());
        orderDeferMapper.updateByPrimaryKeySelective(orderDefer);
    }

    @Override
    public String yeepayDeferNoSms(Long orderId) {
        OrderDefer orderDefer = findLastValidByOrderId(orderId);
        if (orderDefer == null) {
            logger.error("orderId={}，找不到对应的展期订单", orderId);
            return "找不到对应的展期订单";
        }

        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
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

            orderDefer.setPayNo(payNo);
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
            logger.error("展期易宝支付受理异常，error={}", (Object) e.getStackTrace());
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

    @Override
    public JSONObject userDeferDetail(Long uid) {
        OrderDefer orderDefer = orderDeferMapper.selectDeferByUid(uid);
        if (orderDefer != null) {
            JSONObject data = new JSONObject();
            data.put("userDeferCount", orderDefer.getDeferTimes());
            data.put("userDeferStatus", orderDefer.getPayStatus());
            String msg = "";
            switch (orderDefer.getPayStatus()) {
                case 1:
                    msg = "受理成功";
                    break;
                case 2:
                    msg = "受理失败";
                    break;
                case 3:
                    msg = "还款成功";
                    break;
                case 4:
                    msg = "还款失败";
                    break;
                case 5:
                    msg = "回调信息异常";
                    break;
                default:
                    msg = "初始";
                    break;
            }
            data.put("userDeferMsg", msg);
            return data;
        }
        return new JSONObject();
    }
}
