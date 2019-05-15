package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.controller.order.YeepayRepayController;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderRepayMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.MessageHandle;
import com.mod.loan.util.heli.vo.request.BindCardPayVo;
import com.mod.loan.util.heli.vo.response.BindPayValidateCodeResponseVo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderRepayServiceImpl extends BaseServiceImpl<OrderRepay, String> implements OrderRepayService {

    private static Logger logger = LoggerFactory.getLogger(YeepayRepayController.class);

    @Autowired
    OrderRepayMapper orderRepayMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    MerchantService merchantService;
    @Autowired
    UserBankService userBankService;
    @Autowired
    YeepayService yeepayService;
    @Autowired
    OrderService orderService;

    @Value("${helipay.url:}")
    private String helipay_url;
    @Value("${helipay.path:}")
    private String helipay_path;
    @Value("${helipay.pfx.pwd:}")
    private String helipay_pfx_pwd;
    @Value("${yeepay.callback.url:}")
    String yeepay_callback_url;

    @Override
    public void updateOrderRepayInfo(OrderRepay orderRepay, Order order) {
        orderRepayMapper.updateByPrimaryKeySelective(orderRepay);
        if (order!=null){
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }

    @Override
    public int countRepaySuccess(Long orderId) {
        return orderRepayMapper.countRepaySuccess(orderId);
    }

    @Override
    public String yeepayRepayNoSms(Long orderId) {

        if (countRepaySuccess(orderId) >= 1) {
            logger.error("orderId={}已存在还款中的记录", orderId);
            return "请勿重复还款";
        }

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order.getStatus()>= OrderEnum.REPAYING.getCode() && order.getStatus()< OrderEnum.NORMAL_REPAY.getCode()) { // 还款中30～40
            try {
                String repayNo = StringUtil.getOrderNumber("r");// 支付流水号
                String amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : order.getShouldRepay().toString();
                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
                UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());

                String err = yeepayService.payRequest(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(),
                        repayNo, String.valueOf(order.getUid()), userBank.getCardNo(), amount, false, yeepay_callback_url);

                // 还款记录表
                OrderRepay orderRepay = new OrderRepay();
                orderRepay.setRepayNo(repayNo);
                orderRepay.setUid(order.getUid());
                orderRepay.setOrderId(order.getId());
                orderRepay.setRepayType(1); //1-银行卡
                orderRepay.setRepayMoney(new BigDecimal(amount));
                orderRepay.setBank(userBank.getCardName());
                orderRepay.setBankNo(userBank.getCardNo());
                orderRepay.setCreateTime(new Date());
                orderRepay.setUpdateTime(new Date());
                orderRepay.setRepayStatus(OrderRepayStatusEnum.INIT.getCode());//初始状态

                if (StringUtils.isNotEmpty(err)) {
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
                    orderRepay.setRemark("易宝受理失败:" + err);
                    orderRepayMapper.insertSelective(orderRepay);
                    return err;
                }

                orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
                orderRepay.setRemark("易宝受理成功");
                orderRepayMapper.insertSelective(orderRepay);
                return null;
            } catch (Exception e) {
                logger.error("易宝代付受理异常，error={}", e.getMessage());
                return "易宝代付受理异常";
            }
        }
        return "订单状态异常";
    }

    /**
     * 合利宝自动代扣(自动从用户绑卡余额扣款到商户,实现自动还款)
     */
    @Override
    public String heliPayRepayNoSms(Long orderId) {
        if (countRepaySuccess(orderId) >= 1) {
            logger.error("orderId={}已存在还款中的记录", orderId);
            return "请勿重复还款";
        }
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            return "未查询到订单信息,orderId:" + orderId;
        }
        //查询银行卡绑定信息数据
        UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());
        //无默认绑定银行卡或者默认绑卡渠道不是合利宝
        if (userBank == null || userBank.getBindType() == null || userBank.getBindType() != 1) {
            return "未查询到用户合利宝绑定银行卡信息,orderId:" + orderId + ",userId:" + order.getUid();
        }
        //查询商户信息表
        Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
        // 已放款，逾期，坏账状态
        if (order.getStatus()>= OrderEnum.REPAYING.getCode() && order.getStatus()< OrderEnum.NORMAL_REPAY.getCode()) { // 还款中30～40
            try {
                // 支付流水号
                String repayNo = StringUtil.getOrderNumber("r");
                //应还金额不能小于0.11
                String amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : order.getShouldRepay().toString();
                BindCardPayVo requestVo = new BindCardPayVo();
                requestVo.setP1_bizType("QuickPayBindPay");
                //tb_merchant.hlb_id
                requestVo.setP2_customerNumber(merchant.getHlb_id());
                //tb_user_bank.foreign_id
                requestVo.setP3_bindId(userBank.getForeignId());
                requestVo.setP5_orderId(repayNo);
                requestVo.setP4_userId(order.getUid().toString());
                requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
                requestVo.setP7_currency("CNY");
                requestVo.setP8_orderAmount(amount);
                requestVo.setP9_goodsName("apple");
                requestVo.setP11_terminalType("UUID");
                requestVo.setP12_terminalId(repayNo);
                requestVo.setP13_orderIp("127.0.0.1");
                requestVo.setSignatureType("MD5WITHRSA");
                //请求合利宝扣款
                String pfxPath = helipay_path + merchant.getMerchantAlias() + ".pfx";
                Map handleMap = MessageHandle.getReqestMap(requestVo, pfxPath, helipay_pfx_pwd);
                String response = HttpClientService.getHttpResp(handleMap, helipay_url);
                BindPayValidateCodeResponseVo responseVo = JSONObject.parseObject(response, BindPayValidateCodeResponseVo.class);

                // 还款记录表
                OrderRepay orderRepay = new OrderRepay();
                orderRepay.setUid(order.getUid());
                orderRepay.setRepayNo(repayNo);
                //1-银行卡
                orderRepay.setRepayType(1);
                orderRepay.setOrderId(order.getId());
                orderRepay.setBank(userBank.getCardName());
                orderRepay.setRepayMoney(new BigDecimal(amount));
                orderRepay.setCreateTime(new Date());
                orderRepay.setBankNo(userBank.getCardNo());
                //初始状态
                orderRepay.setRepayStatus(OrderRepayStatusEnum.INIT.getCode());
                orderRepay.setUpdateTime(new Date());

                //代扣成功
                if ("0000".equals(responseVo.getRt2_retCode())) {
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
                    orderRepay.setRemark("易宝受理成功");
                    orderRepayMapper.insertSelective(orderRepay);
                    return null;
                } else {
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
                    orderRepay.setRemark("合利宝代扣款受理失败:" + responseVo.getRt3_retMsg());
                    orderRepayMapper.insertSelective(orderRepay);
                    return responseVo.getRt3_retMsg();
                }

            } catch (Exception e) {
                logger.error("合利宝代付受理异常，error={}", e);
                return "合利宝代付受理异常";
            }
        }
        return "订单状态异常";
    }

    @Override
    public String yeepayRepayQuery(String repayNo, String merchantAlias) {
        Merchant merchant = merchantService.findMerchantByAlias(merchantAlias);
        return yeepayService.repayQuery(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(), repayNo, null);
    }

    @Override
    public List<OrderRepay> selectReapyingOrder() {
        return orderRepayMapper.selectReapyingOrder();
    }

    @Override
    public OrderRepay selectLastByOrderId(Long orderId) {
        return orderRepayMapper.selectLastByOrderId(orderId);
    }

    @Override
    public void repaySuccess(OrderRepay orderRepay, Order order) {
        if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
            logger.info("订单{}已还款：", order.getId());
            return ;
        }

        //更新repay
        orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
        orderRepay.setRemark("支付成功");
        orderRepay.setUpdateTime(new Date());

        //更新order
        order.setRealRepayTime(new Date());
        order.setHadRepay(orderRepay.getRepayMoney());
        order.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));

        updateOrderRepayInfo(orderRepay, order);
    }

    @Override
    public void repayFailed(OrderRepay orderRepay, String callbackErr) {
        logger.error("订单错误信息={}，pay_no={}",callbackErr,orderRepay.getRepayNo());
        orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
        orderRepay.setRemark("支付失败:" + callbackErr);
        orderRepay.setUpdateTime(new Date());
        updateOrderRepayInfo(orderRepay, null);
    }
}
