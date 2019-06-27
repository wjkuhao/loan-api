package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ChangjiePayCallBackStatusEnum;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.SmsTemplate;
import com.mod.loan.common.message.QueueSmsMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderPay;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import com.mod.loan.util.changjie.BaseConstant;
import com.mod.loan.util.changjie.ChanPayUtil;
import com.mod.loan.util.changjie.RSA;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NIELIN
 * @version $Id: ChangjiePayCallBackServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class ChangjiePayCallBackServiceImpl implements ChangjiePayCallBackService {
    private static Logger logger = LoggerFactory.getLogger(ChangjiePayCallBackServiceImpl.class);

    @Autowired
    MerchantService merchantService;
    @Autowired
    OrderPayService orderPayService;
    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisMapper redisMapper;

    @Override
    public void payCallback(HttpServletRequest request) {
        String outerTradeNo = request.getParameter("outer_trade_no");
        String withdrawalStatus = request.getParameter("withdrawal_status");
        String sign = request.getParameter("sign");
        logger.info("#[单笔代付放款异步回调-放款订单流水号、状态]-outerTradeNo={},withdrawalStatus={},sign={}", outerTradeNo, withdrawalStatus, sign);
        //根据流水号查询放款流水信息
        OrderPay orderPay = orderPayService.selectByPrimaryKey(outerTradeNo);
        logger.info("#[根据流水号查询放款流水信息]-orderPay={}", JSONObject.toJSON(orderPay));
        if (null == orderPay) {
            logger.info("根据流水号查询订单信息为空");
            return;
        }
        //幂等
        if (1 != orderPay.getPayStatus()) {
            logger.info("该笔订单的放款流水状态异常");
            return;
        }
        //根据订单号获取商户别名
        Order order = orderService.selectByPrimaryKey(orderPay.getOrderId());
        logger.info("#[根据订单号获去订单信息]-order={}", JSONObject.toJSON(order));
        if (null == order) {
            logger.info("根据订单号获去订单信息为空");
            return;
        }
        //幂等
        if (!OrderEnum.LOANING.getCode().equals(order.getStatus())) {
            logger.info("该笔订单的状态不是放款中");
            return;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
        if (null == merchant || StringUtils.isEmpty(merchant.getCjPartnerId()) || StringUtils.isEmpty(merchant.getCjPublicKey()) || StringUtils.isEmpty(merchant.getCjMerchantPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return;
        }
        Map<String, String> map = new HashMap();
        //单笔代付放款异步回调-业务参数(post--request.getParameterMap()拿不到)
        map.put("notify_id", request.getParameter("notify_id"));
        map.put("notify_type", request.getParameter("notify_type"));
        map.put("notify_time", request.getParameter("notify_time"));
        map.put("_input_charset", request.getParameter("_input_charset"));
        map.put("version", request.getParameter("version"));
        map.put("outer_trade_no", request.getParameter("outer_trade_no"));
        map.put("inner_trade_no", request.getParameter("inner_trade_no"));
        map.put("withdrawal_amount", request.getParameter("withdrawal_amount"));
        map.put("withdrawal_status", request.getParameter("withdrawal_status"));
        map.put("uid", request.getParameter("uid"));
        map.put("return_code", request.getParameter("return_code"));
        map.put("fail_reason", request.getParameter("fail_reason"));
        map.put("gmt_withdrawal", request.getParameter("gmt_withdrawal"));
        //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String prestr = ChanPayUtil.createLinkString(map, false);
        logger.info("#[把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串]-prestr={}", prestr);
        //验签
        boolean flag = true;
        try {
            flag = RSA.verify(prestr, sign, merchant.getCjPublicKey(), BaseConstant.CHARSET);
        } catch (Exception e) {
            logger.error("#[验签异常]-e={}", e);
            return;
        }
        if (flag) {
            //更新订单状态和放款流水状态
            Order order1 = new Order();
            order1.setId(order.getId());
            order1.setUpdateTime(new Date());

            OrderPay orderPay1 = new OrderPay();
            orderPay1.setPayNo(outerTradeNo);
            orderPay1.setUpdateTime(new Date());
            Date repayTime = null;
            //成功
            if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_SUCCESS.getCode().equals(withdrawalStatus)) {
                order1.setArriveTime(new Date());
                repayTime = new DateTime(order1.getArriveTime()).plusDays(order.getBorrowDay() - 1).toDate();
                order1.setRepayTime(repayTime);
                order1.setStatus(OrderEnum.REPAYING.getCode());

                orderPay1.setPayStatus(3);
            } else if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_FAIL.getCode().equals(withdrawalStatus)) {
                order1.setStatus(OrderEnum.LOAN_FAILED.getCode());

                orderPay1.setPayStatus(4);
                orderPay1.setRemark(request.getParameter("fail_reason"));
            }
            orderService.updatePayCallbackInfo(order1, orderPay1);
            //成功
            if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_SUCCESS.getCode().equals(withdrawalStatus)) {
                //给用户短信通知放款成功
                User user = userService.selectByPrimaryKey(order.getUid());
                if (null != user) {
                    QueueSmsMessage smsMessage = new QueueSmsMessage();
                    smsMessage.setClientAlias(order.getMerchant());
                    smsMessage.setType(SmsTemplate.T2001.getKey());
                    smsMessage.setPhone(user.getUserPhone());
                    smsMessage.setParams(order.getActualMoney() + "|" + new DateTime(repayTime).toString("MM月dd日"));
                    rabbitTemplate.convertAndSend(RabbitConst.queue_sms, smsMessage);
                }
            } else if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_FAIL.getCode().equals(withdrawalStatus)) {
                redisMapper.unlock(RedisConst.ORDER_LOCK + orderPay.getOrderId());
            }
        }
    }
}
