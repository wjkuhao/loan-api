package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ChangjieBindBankCardStatusEnum;
import com.mod.loan.common.enums.ChangjiePayOrRepayOrQueryReturnCodeEnum;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.controller.order.YeepayRepayController;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderRepayMapper;
import com.mod.loan.mapper.UserMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.UserBank;
import com.mod.loan.model.request.BindBankCard4RepayConfirmRequest;
import com.mod.loan.model.request.BindBankCard4RepaySendMsgRequest;
import com.mod.loan.model.request.TransCode4QueryRequest;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderChangjieRepayServiceImpl extends BaseServiceImpl<OrderRepay, String> implements OrderChangjieRepayService {

    private static Logger logger = LoggerFactory.getLogger(YeepayRepayController.class);

    @Autowired
    OrderRepayMapper orderRepayMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisMapper redisMapper;
    @Autowired
    MerchantService merchantService;
    @Autowired
    UserBankService userBankService;
    @Autowired
    ChangjieRepayService changjieRepayService;
    @Autowired
    OrderRepayService orderRepayService;
    @Autowired
    OrderService orderService;

    @Override
    public String bindBankCard4RepaySendMsg(Long orderId) {
        logger.info("#[畅捷订单协议支付还款发送验证码]-[开始]-orderId={}", orderId);
        if (null == orderId) {
            logger.info("参数为空");
            return null;
        }
        //幂等
        if (orderRepayMapper.countRepaySuccess(orderId) >= 1) {
            logger.info("orderId={}已存在还款中的记录", orderId);
            return null;
        }
        //获取订单信息
        Order order = orderMapper.selectByPrimaryKey(orderId);
        logger.info("#[获取订单信息]-order={}", JSONObject.toJSON(order));
        if (null == order) {
            logger.info("获取订单信息为空");
            return null;
        }
        //幂等--还款中30～40
        if (order.getStatus() >= OrderEnum.REPAYING.getCode() && order.getStatus() < OrderEnum.NORMAL_REPAY.getCode()) {
            //获取商户信息
            Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
            if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return null;
            }
            //获取该订单的银行卡号信息
            UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());
            logger.info("#[获取该订单的银行卡号信息]-userBank={}", JSONObject.toJSON(userBank));
            //唯一流水号
            String repayNo = StringUtil.getOrderNumber("r");
            String amount = "dev".equals(Constant.ENVIROMENT) ? "0.01" : order.getShouldRepay().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            BindBankCard4RepaySendMsgRequest bindBankCard4RepaySendMsgRequest = new BindBankCard4RepaySendMsgRequest();
            bindBankCard4RepaySendMsgRequest.setRequestSeriesNo(repayNo);
            bindBankCard4RepaySendMsgRequest.setBankCardNo(userBank.getCardNo());
            bindBankCard4RepaySendMsgRequest.setMerchantName(merchant.getMerchantAlias());
            bindBankCard4RepaySendMsgRequest.setAmount(new BigDecimal(amount));
            bindBankCard4RepaySendMsgRequest.setPartnerId(merchant.getCjPartnerId());
            bindBankCard4RepaySendMsgRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
            bindBankCard4RepaySendMsgRequest.setPublicKey(merchant.getCjPublicKey());
            //去调畅捷协议支付还款发送验证码
            String result = null;
            try {
                result = changjieRepayService.bindBankCard4RepaySendMsg(bindBankCard4RepaySendMsgRequest);
            } catch (Exception e) {
                logger.error("#[去调畅捷协议支付还款发送验证码]-[异常]-e={}", e);
                return null;
            }
            if (null == result) {
                logger.info("#[去调畅捷协议支付还款发送验证码]-[返回结果为空]");
                return null;
            }
            //解析返回结果
            JSONObject jsonObject = JSONObject.parseObject(result);
            //失败
            if (ChangjieBindBankCardStatusEnum.F.getCode().equals(jsonObject.getString("Status"))) {
                logger.info("#[去调畅捷协议支付还款发送验证码]-[失败]");
                return null;
            }
            redisMapper.set(RedisConst.repay_text + repayNo, orderId, Constant.SMS_EXPIRATION_TIME);
            logger.info("#[畅捷订单协议支付还款发送验证码]-[结束]");
            return repayNo;
        }
        return null;
    }

    @Override
    public String bindBankCard4RepayConfirm(String seriesNo, String smsCode) {
        logger.info("#[畅捷订单协议支付还款确认]-[开始]-seriesNo={},smsCode={}", seriesNo, smsCode);
        if (StringUtils.isBlank(seriesNo) || StringUtils.isBlank(smsCode)) {
            logger.info("参数为空");
            return null;
        }
        long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text + seriesNo));
        if (0 == orderId) {
            logger.info("该订单已过期-seriesNo={}", seriesNo);
            return null;
        }
        //幂等
        if (orderRepayMapper.countRepaySuccess(orderId) >= 1) {
            logger.info("orderId={}已存在还款中的记录", orderId);
            return null;
        }
        //获取订单信息
        Order order = orderMapper.selectByPrimaryKey(orderId);
        logger.info("#[获取订单信息]-order={}", JSONObject.toJSON(order));
        if (null == order) {
            return null;
        }
        //幂等--还款中30～40
        if (order.getStatus() >= OrderEnum.REPAYING.getCode() && order.getStatus() < OrderEnum.NORMAL_REPAY.getCode()) {
            //获取商户信息
            Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
            if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return null;
            }
            //获取该订单的银行卡号信息
            UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());
            logger.info("#[获取该订单的银行卡号信息]-userBank={}", JSONObject.toJSON(userBank));
            //唯一流水号
            String repayNo = StringUtil.getOrderNumber("r");
            BindBankCard4RepayConfirmRequest bindBankCard4RepayConfirmRequest = new BindBankCard4RepayConfirmRequest();
            bindBankCard4RepayConfirmRequest.setRequestSeriesNo(repayNo);
            bindBankCard4RepayConfirmRequest.setSeriesNo(seriesNo);
            bindBankCard4RepayConfirmRequest.setSmsCode(smsCode);
            bindBankCard4RepayConfirmRequest.setPartnerId(merchant.getCjPartnerId());
            bindBankCard4RepayConfirmRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
            bindBankCard4RepayConfirmRequest.setPublicKey(merchant.getCjPublicKey());
            //去调畅捷协议支付还款确认
            String result = null;
            try {
                result = changjieRepayService.bindBankCard4RepayConfirm(bindBankCard4RepayConfirmRequest);
            } catch (Exception e) {
                logger.error("#[去调畅捷协议支付还款确认]-[异常]-e={}", e);
                return null;
            }
            if (null == result) {
                logger.info("#[去调畅捷协议支付还款确认]-[返回结果为空]");
                return null;
            }
            //落还款记录表
            OrderRepay orderRepay = new OrderRepay();
            orderRepay.setRepayNo(seriesNo);
            orderRepay.setUid(order.getUid());
            orderRepay.setOrderId(order.getId());
            //1-银行卡
            orderRepay.setRepayType(1);
            String amount = "dev".equals(Constant.ENVIROMENT) ? "0.01" : order.getShouldRepay().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            orderRepay.setRepayMoney(new BigDecimal(amount));
            orderRepay.setBank(userBank.getCardName());
            orderRepay.setBankNo(userBank.getCardNo());
            orderRepay.setCreateTime(new Date());
            orderRepay.setUpdateTime(new Date());
            //解析返回结果
            JSONObject jsonObject = JSONObject.parseObject(result);
            //畅捷协议支付还款确认
            if (StringUtils.equals("S", jsonObject.getString("Status"))) {
                //成功
                orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
                orderRepay.setRemark("畅捷还款成功");
            } else if (StringUtils.equals("F", jsonObject.getString("Status"))) {
                //失败
                orderRepay.setRemark(jsonObject.getString("AppRetMsg"));
                orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
            } else {
                //处理中
                orderRepay.setRemark("畅捷还款处理中");
                orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            }
            orderRepayMapper.insertSelective(orderRepay);
            if (StringUtils.equals("S", jsonObject.getString("Status"))) {
                //成功
                order.setRealRepayTime(new Date());
                order.setHadRepay(orderRepay.getRepayMoney());
                order.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
                orderMapper.updateByPrimaryKeySelective(order);
            }
            logger.info("#[畅捷订单协议支付还款确认]-[结束]");
            return seriesNo;
        }
        return null;
    }

    @Override
    public String bindBankCard4RepayQuery(String repayNo) {
        logger.info("#[畅捷订单协议支付还款结果查询]-[开始]-repayNo={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            logger.info("参数为空");
            return null;
        }
        //根据还款流水号查询还款流水信息
        OrderRepay orderRepay = orderRepayMapper.selectByPrimaryKey(repayNo);
        logger.info("#[根据还款流水号查询还款流水信息]-orderRepay={}", JSONObject.toJSON(orderRepay));
        if (null == orderRepay) {
            logger.info("根据还款流水号查询还款流水信息为空");
            return null;
        }
        //根据订单id查询订单信息
        Order order = orderMapper.selectByPrimaryKey(orderRepay.getOrderId());
        logger.info("#[根据订单id查询订单信息]-order={}", JSONObject.toJSON(order));
        if (null == order) {
            logger.info("根据订单id查询订单信息为空");
            return null;
        }
        //幂等
        if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus()) || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus()) || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
            logger.info("该笔订单状态已还款");
            return null;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        if (null == merchant || StringUtils.isEmpty(merchant.getCjPartnerId()) || StringUtils.isEmpty(merchant.getCjPublicKey()) || StringUtils.isEmpty(merchant.getCjMerchantPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return null;
        }
        //每次请求唯一流水号
        String seriesNo = StringUtil.getOrderNumber("r");
        TransCode4QueryRequest transCode4QueryRequest = new TransCode4QueryRequest();
        transCode4QueryRequest.setRequestSeriesNo(seriesNo);
        transCode4QueryRequest.setSeriesNo(repayNo);
        transCode4QueryRequest.setPartnerId(merchant.getCjPartnerId());
        transCode4QueryRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        transCode4QueryRequest.setPublicKey(merchant.getCjPublicKey());
        //去调畅捷协议支付还款结果查询
        String result = changjieRepayService.bindBankCard4RepayQuery(transCode4QueryRequest);
        if (null == result) {
            logger.info("去调畅捷协议支付还款结果查询返回为空");
            return null;
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //畅捷协议支付还款确认成功
        if (StringUtils.equals("S", jsonObject.getString("Status")) && (StringUtils.equals(ChangjiePayOrRepayOrQueryReturnCodeEnum.SUCCESS_QT000000.getCode(), jsonObject.getString("AppRetcode")))) {
            //成功
            orderRepayService.repaySuccess(orderRepay, order);
        } else if (StringUtils.equals("F", jsonObject.getString("Status"))) {
            //失败
            orderRepayService.repayFailed(orderRepay, jsonObject.getString("AppRetMsg"));
        }
        logger.info("#[畅捷订单协议支付还款结果查询]-[结束]");
        return repayNo;
    }

    @Override
    public List<OrderRepay> changjieRepayQuery4Task() {
        return orderRepayMapper.changjieRepayQuery4Task();
    }


}
