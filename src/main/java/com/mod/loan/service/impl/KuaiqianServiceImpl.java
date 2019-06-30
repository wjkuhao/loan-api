package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.kuaiqian.KuaiqianPost;
import com.mod.loan.util.kuaiqian.mgw.entity.TransInfo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class KuaiqianServiceImpl implements KuaiqianService {
    private static Logger logger = LoggerFactory.getLogger(KuaiqianServiceImpl.class);
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepayService orderRepayService;
    @Autowired
    OrderPayService orderPayService;
    @Autowired
    UserService userService;
    @Autowired
    UserBankService userBankService;
    @Autowired
    MerchantService merchantService;

    @Override
    public ResultMessage orderRepayKuaiqian(Long orderId) {
        Long uid = RequestThread.getUid();
        if (orderId == null) {
            logger.info("订单异常，uid={},订单号={}", uid, orderId);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单不存在");
        }
        //查询订单信息
        Order order = orderService.selectByPrimaryKey(orderId);
        if (!order.getUid().equals(uid)) {
            logger.info("订单异常，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
        }
        if (order.getStatus() >= OrderEnum.NORMAL_REPAY.getCode() || order.getStatus() < OrderEnum.REPAYING.getCode()) {
            logger.info("订单非还款状态，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
        }
        //查询用户绑定银行卡信息
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        //查询商户信息
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        HashMap respMap = null;
        //设置手机动态鉴权节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1("TxnMsgContent");
        transInfo.setRecordeText_2("ErrorMsgContent");
        //版本号
        String version = "1.0";
        //交易类型
        String txnType = "PUR";
        //消息状态
        String interactiveStatus = "TR1";
        //特殊交易标志
        String spFlag = "QPay02";
        //商户编号
        String merchantId = merchant.getKqMerchantId();
        //终端编号
        String terminalId = merchant.getKqTerminalId();
        //客户号
        String customerId = userBank.getCardPhone() + "_" + String.valueOf(uid);
        //交易时间
        String entryTime = TimeUtils.parseTime(new Date(), TimeUtils.dateformat5);
        //支付协议号
        String payToken = userBank.getForeignId();
        //支付流水号
        String repayNo = StringUtil.getOrderNumber("r");
        //还款金额
        BigDecimal amount = order.getShouldRepay();
        //还款记录表
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
        if (orderRepay == null) {
            orderRepay = new OrderRepay();
            orderRepay.setRepayNo(repayNo);
            orderRepay.setUid(order.getUid());
            orderRepay.setOrderId(orderId);
            orderRepay.setRepayType(1);
            orderRepay.setRepayMoney(amount);
            orderRepay.setBank(userBank.getCardName());
            orderRepay.setBankNo(userBank.getCardNo());
            orderRepay.setCreateTime(new Date());
            orderRepay.setUpdateTime(new Date());
            orderRepay.setRepayStatus(0);
            orderRepayService.insertSelective(orderRepay);
        }
        try {
            StringBuffer orderPlain = new StringBuffer();
            orderPlain.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    .append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">")
                    .append("<version>").append(version).append("</version>")
                    .append("<TxnMsgContent>")
                    .append("<interactiveStatus>").append(interactiveStatus).append("</interactiveStatus>")
                    .append("<spFlag>").append(spFlag).append("</spFlag>")
                    .append("<txnType>").append(txnType).append("</txnType>")
                    .append("<merchantId>").append(merchantId).append("</merchantId>")
                    .append("<terminalId>").append(terminalId).append("</terminalId>")
                    .append("<externalRefNumber>").append(repayNo).append("</externalRefNumber>")
                    .append("<entryTime>").append(entryTime).append("</entryTime>")
                    .append("<amount>").append(amount).append("</amount>")
                    .append("<customerId>").append(customerId).append("</customerId>")
                    .append("<payToken>").append(payToken).append("</payToken>")
                    .append("<extMap>")
                    .append("<extDate><key>phone</key><value></value></extDate>")
                    .append("<extDate><key>validCode</key><value></value></extDate>")
                    .append("<extDate><key>savePciFlag</key><value>0</value></extDate>")
                    .append("<extDate><key>token</key><value></value></extDate>")
                    .append("<extDate><key>payBatch</key><value>2</value></extDate>")
                    .append("</extMap>")
                    .append("</TxnMsgContent>")
                    .append("</MasMessage>");
            respMap = KuaiqianPost.sendPost(merchant.getKqCertPath(), merchant.getKqCertPwd(), merchantId, Constant.KUAIQIAN_PAY_URL, orderPlain.toString(), transInfo);
            logger.info("#[快钱协议返回结果]-respMap={}", JSONObject.toJSON(respMap));
        } catch (Exception e) {
            logger.info("快钱支付异常。订单号为{}，卡号为{}，银行名称为{}", orderId, userBank.getCardNo(), userBank.getCardName());
            logger.error("快钱支付异常", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
        //还款成功
        if ("00".equals(MapUtils.getString(respMap, "responseCode"))) {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(3);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRemark(MapUtils.getString(respMap, "responseTextMessage"));

            Order order1 = new Order();
            order1.setId(orderRepay.getOrderId());
            order1.setUpdateTime(new Date());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(amount);
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
            //成功返回订单号，便于查看详情
            return new ResultMessage(ResponseEnum.M2000, order.getId());
            //订单已创建，受理中
        } else if ("C0".equals(MapUtils.getString(respMap, "responseCode"))
                || "68".equals(MapUtils.getString(respMap, "responseCode"))) {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(1);
            orderRepay1.setRemark(MapUtils.getString(respMap, "responseTextMessage"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            //处理中返回订单号，便于查看详情
            return new ResultMessage(ResponseEnum.M2000.getCode(), order.getId());
        } else {
            logger.info("快钱支付失败，result={}", JSONObject.toJSON(respMap));
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(MapUtils.getString(respMap, "responseTextMessage"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
        }
    }

    @Override
    public ResultMessage queryKuaiqianRepayOrder(Long orderId) {
        Long uid = RequestThread.getUid();
        if (orderId == null) {
            logger.info("订单异常，uid={},订单号={}", uid, orderId);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单不存在");
        }
        //查询订单信息
        Order order = orderService.selectByPrimaryKey(orderId);
        if (!order.getUid().equals(uid)) {
            logger.info("订单异常，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
        }
        if (order.getStatus().equals(OrderEnum.NORMAL_REPAY.getCode())  || order.getStatus().equals(OrderEnum.OVERDUE_REPAY.getCode())
                || order.getStatus().equals(OrderEnum.DEFER_REPAY.getCode())) {
            return new ResultMessage(ResponseEnum.M2000, order.getId());
        }
        if (order.getStatus() >= OrderEnum.NORMAL_REPAY.getCode() || order.getStatus() < OrderEnum.REPAYING.getCode()) {
            logger.info("订单非还款状态，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
        }
        //查询商户信息
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        //查询最后一笔还款订单
        OrderRepay orderRepay = orderRepayService.selectLastByOrderId(orderId);
        HashMap respMap = null;
        //设置手机动态鉴权节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1("TxnMsgContent");
        transInfo.setRecordeText_2("ErrorMsgContent");
        //版本号
        String version = "1.0";
        //交易类型
        String txnType = "PUR";
        //外部跟踪编号
        String repayNo = orderRepay.getRepayNo();
        //商户编号
        String merchantId = merchant.getKqMerchantId();
        //终端编号
        String terminalId = merchant.getKqTerminalId();
        try {
            StringBuffer orderPlain = new StringBuffer();
            orderPlain.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    .append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">")
                    .append("<version>").append(version).append("</version>")
                    .append("<QryTxnMsgContent>")
                    .append("<txnType>").append(txnType).append("</txnType>")
                    .append("<merchantId>").append(merchantId).append("</merchantId>")
                    .append("<terminalId>").append(terminalId).append("</terminalId>")
                    .append("<externalRefNumber>").append(repayNo).append("</externalRefNumber>")
                    .append("</QryTxnMsgContent>")
                    .append("</MasMessage>");
            respMap = KuaiqianPost.sendPost(merchant.getKqCertPath(), merchant.getKqCertPwd(), merchantId, Constant.KUAIQIAN_PAY_QUERY_URL, orderPlain.toString(), transInfo);
            logger.info("#[查询快钱协议支付返回结果]-respMap={}", JSONObject.toJSON(respMap));
        } catch (Exception e) {
            logger.info("查询快钱还款订单异常。订单号为{}", orderId);
            logger.error("查询快钱还款订单异常", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
        //还款成功
        if ("00".equals(MapUtils.getString(respMap, "responseCode"))) {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(3);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRemark(MapUtils.getString(respMap, "responseTextMessage"));

            Order order1 = new Order();
            order1.setId(orderId);
            order1.setUpdateTime(new Date());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(new BigDecimal(MapUtils.getString(respMap, "amount")));
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
            //成功返回订单号，便于查看详情
            return new ResultMessage(ResponseEnum.M2000, order.getId());
        } else if ("C0".equals(MapUtils.getString(respMap, "responseCode"))
                || "68".equals(MapUtils.getString(respMap, "responseCode"))) {
            return new ResultMessage(ResponseEnum.M2000.getCode(), order.getId());
        } else {
            logger.info("查询快钱还款订单支付失败，result={}", JSONObject.toJSON(respMap));
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(MapUtils.getString(respMap, "responseTextMessage"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
        }
    }

    @Override
    public Map queryKuaiqianRepayOrder(Long uid, Long orderId, String merchantAlias) {
        //查询商户信息
        Merchant merchant = merchantService.findMerchantByAlias(merchantAlias);
        //查询最后一笔还款订单
        OrderRepay orderRepay = orderRepayService.selectLastByOrderId(orderId);
        HashMap respMap = null;
        //设置手机动态鉴权节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1("TxnMsgContent");
        transInfo.setRecordeText_2("ErrorMsgContent");
        //版本号
        String version = "1.0";
        //交易类型
        String txnType = "PUR";
        //外部跟踪编号
        String repayNo = orderRepay.getRepayNo();
        //商户编号
        String merchantId = merchant.getKqMerchantId();
        //终端编号
        String terminalId = merchant.getKqTerminalId();
        try {
            StringBuffer orderPlain = new StringBuffer();
            orderPlain.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    .append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">")
                    .append("<version>").append(version).append("</version>")
                    .append("<QryTxnMsgContent>")
                    .append("<txnType>").append(txnType).append("</txnType>")
                    .append("<merchantId>").append(merchantId).append("</merchantId>")
                    .append("<terminalId>").append(terminalId).append("</terminalId>")
                    .append("<externalRefNumber>").append(repayNo).append("</externalRefNumber>")
                    .append("</QryTxnMsgContent>")
                    .append("</MasMessage>");
            respMap = KuaiqianPost.sendPost(merchant.getKqCertPath(), merchant.getKqCertPwd(), merchantId, Constant.KUAIQIAN_PAY_QUERY_URL, orderPlain.toString(), transInfo);
            logger.info("#[查询快钱协议支付返回结果]-respMap={}", JSONObject.toJSON(respMap));
        } catch (Exception e) {
            logger.info("查询快钱还款订单异常。订单号为{}", orderId);
            logger.error("查询快钱还款订单异常", e);
        }
        return respMap;
    }

}
