package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.enums.SmsTemplate;
import com.mod.loan.common.message.QueueSmsMessage;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.XmlUtils;
import com.mod.loan.util.kuaiqian.KuaiqianPost;
import com.mod.loan.util.kuaiqian.common.KuaiqianHttpUtil;
import com.mod.loan.util.kuaiqian.mgw.entity.TransInfo;
import com.mod.loan.util.kuaiqian.mgw.util.ParseUtil;
import com.mod.loan.util.kuaiqian.mgw.util.SignUtil;
import com.mod.loan.util.kuaiqian.notice.NotifyRequest;
import com.mod.loan.util.kuaiqian.notice.Pay2bankNotify;
import org.apache.commons.collections.MapUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
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
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${kuaiqian.pay.notice.url}")
    private String tr3Url;

    @Override
    public ResultMessage orderRepayKuaiqian(Long orderId) {
        Long uid = 1L;//RequestThread.getUid();
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
        // 支付流水号
        String repayNo = StringUtil.getOrderNumber("r");
        //还款金额
        BigDecimal amount = order.getShouldRepay();
        // 还款记录表
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
                    .append("<tr3Url>").append(tr3Url).append("</tr3Url>")
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
        } catch (Exception e) {
            logger.info("查询快钱还款订单异常。订单号为{}", orderId);
            logger.error("查询快钱还款订单异常", e);
        }
        return respMap;
    }

    @Override
    public void kuaiqianOrderRepayNotice(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        //获取客户端请求报文
        SignUtil signUtil = new SignUtil();
        String requestXml = KuaiqianHttpUtil.genNoticeRequestXml(httpRequest);
        TransInfo transInfo = new TransInfo();
        if (signUtil.verSignForXml(requestXml)) {
            //返回TR3后的第一个标志字段
            transInfo.setRecordeText_1("TxnMsgContent");
            //返回TR3后的错误标志字段
            transInfo.setRecordeText_2("ErrorMsgContent");
            //设置最后的解析方式
            transInfo.setFLAG(true);
            //开始接收TR3
            //将获取的数据传入DOM解析函数中
            HashMap respXml = ParseUtil.parseXML(requestXml, transInfo);
            if (respXml != null) {
                String version = MapUtils.getString(respXml, "version");
                //交易类型编码（txnType）
                String txnType = MapUtils.getString(respXml, "txnType");
                //商户编号
                String merchantId = MapUtils.getString(respXml, "merchantId");
                //终端编号（terminalId）
                String terminalId = MapUtils.getString(respXml, "terminalId");
                //外部检索参考号（externalRefNumber）
                String repayNo = MapUtils.getString(respXml, "externalRefNumber");
                //检索参考号（refNumber）
                String refNumber = MapUtils.getString(respXml, "refNumber");
                //应答码（responseCode）
                String responseCode = MapUtils.getString(respXml, "responseCode");
                //应答文本信息（responseTextMessage）
                String responseTextMessage = MapUtils.getString(respXml, "responseTextMessage");
                // 还款记录表
                OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
                //查询订单信息
                Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
                if ("00".equals(responseCode)) {
                    orderRepayService.repaySuccess(orderRepay, order);
                } else if ("C0".equals(responseCode) || "68".equals(responseCode)) {
                    logger.info("---------kuaiqian query repayno= {}订单处理中-----------------", repayNo);
                } else {
                    orderRepayService.repayFailed(orderRepay, responseTextMessage);
                }
                //输出TR4
                StringBuffer tr4XML = new StringBuffer();
                tr4XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
                tr4XML.append("<version>").append(version).append("</version>");
                tr4XML.append("<TxnMsgContent>");
                tr4XML.append("<txnType>").append(txnType).append("</txnType>");
                tr4XML.append("<interactiveStatus>TR4</interactiveStatus>");
                tr4XML.append("<merchantId>").append(merchantId).append("</merchantId>");
                tr4XML.append("<terminalId>").append(terminalId).append("</terminalId>");
                tr4XML.append("<refNumber>").append(refNumber).append("</refNumber>");
                tr4XML.append("</TxnMsgContent>");
                tr4XML.append("</MasMessage>");

                try (BufferedWriter outW = new BufferedWriter(httpResponse.getWriter());) {
                    outW.write(tr4XML.toString());
                    outW.flush();
                    outW.close();
                } catch (IOException e) {
                    logger.error("ReceiveTR3ToTR4:异步通知响应失败");
                }
            }
        } else {
            logger.info("ReceiveTR3ToTR4:验签失败");
        }
    }

    @Override
    public void kuaiqianOrderPayNotice(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        //获取客户端请求报文
        String requestXml = KuaiqianHttpUtil.genNoticeRequestXml(httpRequest);
        NotifyRequest request = XmlUtils.convertToJavaBean(requestXml, NotifyRequest.class);
        //异步通知商户会员号需要拼接来区分：memberCode_1
        String memberCode = request.getNotifyHead().getMemberCode() + "_1";
        //解密请求报文
        Pay2bankNotify pay2bankNotify = KuaiqianHttpUtil.unsealOrderPayNotice(request, memberCode);
        String payNo = pay2bankNotify.getOrder_seq_id();
        String msg = pay2bankNotify.getError_msg();
        if ("0000".equals(pay2bankNotify.getError_code())) {
            String state = pay2bankNotify.getStatus();
            // 交易成功
            if ("111".equals(state)) {
                paySuccess(payNo);
                // 交易失败
            } else if ("112".equals(state)) {
                payFail(payNo, msg);
            }
        } else {
            logger.info("查询代付结果失败,payNo={},快钱返回结果={},msg={}", payNo, msg);
        }
        //调用单笔快到银api2.0服务
        String responseXml = KuaiqianHttpUtil.genPayNoticeXml(request.getNotifyRequestBody().getSealDataType().getOriginalData(), memberCode);
        try {
            //返回响应报文
            httpResponse.setCharacterEncoding("utf-8");
            httpResponse.setContentType("utf-8");
            httpResponse.getWriter().write(responseXml);
            httpResponse.getWriter().flush();
        } catch (Exception e) {
            logger.error("快钱代付异步通知响应异常={}", e);
        }
        logger.info("快钱代付异步通知结果={}", responseXml);
    }

    private void paySuccess(String payNo) {
        OrderPay orderPay = orderPayService.selectByPrimaryKey(payNo);
        // 只处理受理中的状态
        if (orderPay.getPayStatus() == 1) {
            Order order = orderService.selectByPrimaryKey(orderPay.getOrderId());
            Order order1 = new Order();
            order1.setId(order.getId());
            order1.setArriveTime(new Date());
            Date repayTime = new DateTime(order1.getArriveTime()).plusDays(order.getBorrowDay() - 1).toDate();
            order1.setRepayTime(repayTime);
            order1.setStatus(31);

            OrderPay orderPay1 = new OrderPay();
            orderPay1.setPayNo(payNo);
            orderPay1.setPayStatus(3);
            orderPay1.setUpdateTime(new Date());
            orderService.updatePayCallbackInfo(order1, orderPay1);
            // 给用户短信通知 放款成功
            User user = userService.selectByPrimaryKey(order.getUid());
            QueueSmsMessage smsMessage = new QueueSmsMessage();
            smsMessage.setClientAlias(order.getMerchant());
            smsMessage.setType(SmsTemplate.T2001.getKey());
            smsMessage.setPhone(user.getUserPhone());
            smsMessage.setParams(order.getActualMoney() + "|" + new DateTime(repayTime).toString("MM月dd日"));
            rabbitTemplate.convertAndSend(RabbitConst.queue_sms, smsMessage);
        } else {
            logger.error("查询代付结果异常,payNo={}", payNo);
        }
    }

    private void payFail(String payNo, String errorMsg) {
        OrderPay orderPay = orderPayService.selectByPrimaryKey(payNo);
        // 只处理受理中的状态
        if (orderPay.getPayStatus() == 1) {
            Order order1 = new Order();
            order1.setId(orderPay.getOrderId());
            order1.setStatus(23);
            OrderPay orderPay1 = new OrderPay();
            orderPay1.setPayNo(payNo);
            orderPay1.setPayStatus(4);
            orderPay1.setRemark(errorMsg);
            orderPay1.setUpdateTime(new Date());
            orderService.updatePayCallbackInfo(order1, orderPay1);
            redisMapper.unlock(RedisConst.ORDER_LOCK + orderPay.getOrderId());
        } else {
            logger.error("查询代付结果异常,payNo={}", payNo);
        }
    }
}
