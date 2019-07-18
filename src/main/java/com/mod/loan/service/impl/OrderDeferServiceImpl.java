package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.*;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.OrderDeferMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.model.UserBank;
import com.mod.loan.model.request.BindBankCard4RepayConfirmRequest;
import com.mod.loan.model.request.BindBankCard4RepaySendMsgRequest;
import com.mod.loan.model.request.TransCode4QueryRequest;
import com.mod.loan.service.*;
import com.mod.loan.util.DesUtil;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.changjie.BaseConstant;
import com.mod.loan.util.changjie.ChanPayUtil;
import com.mod.loan.util.changjie.RSA;
import com.mod.loan.util.kuaiqian.KuaiqianPost;
import com.mod.loan.util.kuaiqian.mgw.entity.TransInfo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("orderDeferService")
public class OrderDeferServiceImpl extends BaseServiceImpl<OrderDefer, Integer> implements OrderDeferService {

    private static Logger logger = LoggerFactory.getLogger(OrderDeferServiceImpl.class);

    private final OrderDeferMapper orderDeferMapper;
    private final OrderService orderService;
    private final MerchantService merchantService;
    private final UserBankService userBankService;
    private final YeepayService yeepayService;
    private final ChangjieRepayService changjieRepayService;

    @Value("${yeepay.defer.callback.url:}")
    String yeepay_defer_callback_url;

    @Autowired
    public OrderDeferServiceImpl(OrderDeferMapper orderDeferMapper,
                                 OrderService orderService, MerchantService merchantService, UserBankService userBankService, YeepayService yeepayService,
                                 ChangjieRepayService changjieRepayService) {
        this.orderDeferMapper = orderDeferMapper;
        this.orderService = orderService;
        this.merchantService = merchantService;
        this.userBankService = userBankService;
        this.yeepayService = yeepayService;
        this.changjieRepayService = changjieRepayService;
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
            modifiedOrder.setUpdateTime(new Date());
            Integer status = modifiedOrder.getStatus();
            if (status.equals(OrderEnum.REPAYING.getCode())) {
                modifiedOrder.setStatus(OrderEnum.DEFER.getCode());
            } else if (status.equals(OrderEnum.OVERDUE.getCode()) || status.equals(OrderEnum.DEFER_OVERDUE.getCode())) {
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
            String amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : orderDefer.getDeferTotalFee().toString();
            Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
            UserBank userBank = userBankService.selectUserCurrentBankCard(orderDefer.getUid());

            String err = yeepayService.payRequest(DesUtil.decryption(merchant.getYeepay_repay_appkey()), DesUtil.decryption(merchant.getYeepay_repay_private_key()),
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
        String errMsg = null;
        try {
            errMsg = yeepayService.repayQuery(DesUtil.decryption(merchant.getYeepay_repay_appkey()), DesUtil.decryption(merchant.getYeepay_repay_private_key()), repayNo, null);
        }catch (Exception e){
            logger.error("易宝还款查询异常，repayNo={}, error={}",repayNo, e);
        }
        return errMsg;
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

    @Override
    public List<OrderDefer> selectOrderDefer() {
       return orderDeferMapper.selectOrderDefer();
    }

    @Override
    public Integer deferSuccessCount(Long uid) {
        return orderDeferMapper.deferSuccessCount(uid);
    }

    @Override
    public String changjieDeferRepay4SendMsg(Long orderId) {
        logger.info("#[畅捷续期时订单协议支付还款发送验证码]-[开始]-orderId={}", orderId);
        if (null == orderId) {
            logger.info("参数为空");
            return null;
        }
        //根据订单id查询续期订单信息
        OrderDefer orderDefer = findLastValidByOrderId(orderId);
        logger.info("#[根据订单id查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
        if (orderDefer == null) {
            logger.info("根据订单id查询续期订单信息为空");
            return null;
        }
        //幂等
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已受理成功的");
            return null;
        }
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已还款成功的");
            return null;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return null;
        }
        //获取该订单的银行卡号信息
        UserBank userBank = userBankService.selectUserCurrentBankCard(orderDefer.getUid());
        logger.info("#[获取该订单的银行卡号信息]-userBank={}", JSONObject.toJSON(userBank));
        //唯一流水号
        String repayNo = StringUtil.getOrderNumber("d");
        String amount = "dev".equals(Constant.ENVIROMENT) ? "0.01" : new BigDecimal(orderDefer.getDeferTotalFee()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        BindBankCard4RepaySendMsgRequest bindBankCard4RepaySendMsgRequest = new BindBankCard4RepaySendMsgRequest();
        bindBankCard4RepaySendMsgRequest.setRequestSeriesNo(repayNo);
        bindBankCard4RepaySendMsgRequest.setBankCardNo(userBank.getCardNo());
        bindBankCard4RepaySendMsgRequest.setMerchantName(merchant.getMerchantAlias());
        bindBankCard4RepaySendMsgRequest.setAmount(new BigDecimal(amount));
        bindBankCard4RepaySendMsgRequest.setPartnerId(merchant.getCjPartnerId());
        bindBankCard4RepaySendMsgRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        bindBankCard4RepaySendMsgRequest.setPublicKey(merchant.getCjPublicKey());
        //去调畅捷协议支付还款发送验证码
        String result = changjieRepayService.bindBankCard4RepaySendMsg(bindBankCard4RepaySendMsgRequest);
        if (null == result) {
            logger.info("#[续期时去调畅捷协议支付还款发送验证码]-[返回结果为空]");
            return null;
        }
        orderDefer.setPayNo(repayNo);
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //失败
        if (ChangjieBindBankCardStatusEnum.F.getCode().equals(jsonObject.getString("Status"))) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
            orderDefer.setRemark("畅捷受理失败:" + jsonObject.getString("RetMsg"));
        } else {
            orderDefer.setPayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderDefer.setRemark("畅捷受理成功");
        }
        orderDeferMapper.updateByPrimaryKey(orderDefer);
        return repayNo;
    }

    @Override
    public String changjieDeferRepay4Confirm(String seriesNo, String smsCode) {
        logger.info("#[畅捷续期时订单协议支付还款确认]-[开始]-seriesNo={},smsCode={}", seriesNo, smsCode);
        if (StringUtils.isBlank(seriesNo) || StringUtils.isBlank(smsCode)) {
            logger.info("参数为空");
            return null;
        }
        //根据支付流水号查询续期订单信息
        OrderDefer orderDefer = selectByPayNo(seriesNo);
        logger.info("#[根据支付流水号查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
        if (orderDefer == null) {
            logger.info("根据支付流水号查询续期订单信息为空");
            return null;
        }
        //幂等
        if (!orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
            logger.info("该续期订单的状态不是受理成功的");
            return null;
        }
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已还款成功的");
            return null;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return null;
        }
        //唯一流水号
        String repayNo = StringUtil.getOrderNumber("d");
        BindBankCard4RepayConfirmRequest bindBankCard4RepayConfirmRequest = new BindBankCard4RepayConfirmRequest();
        bindBankCard4RepayConfirmRequest.setRequestSeriesNo(repayNo);
        bindBankCard4RepayConfirmRequest.setSeriesNo(seriesNo);
        bindBankCard4RepayConfirmRequest.setSmsCode(smsCode);
        bindBankCard4RepayConfirmRequest.setPartnerId(merchant.getCjPartnerId());
        bindBankCard4RepayConfirmRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        bindBankCard4RepayConfirmRequest.setPublicKey(merchant.getCjPublicKey());
        //去调畅捷协议支付还款确认
        String result = changjieRepayService.bindBankCard4RepayConfirm(bindBankCard4RepayConfirmRequest);
        if (null == result) {
            logger.info("#[续期时去调畅捷协议支付还款确认]-[返回结果为空]");
            return null;
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //畅捷协议支付还款确认
        if (StringUtils.equals("S", jsonObject.getString("Status"))) {
            //成功
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDefer.setRemark("畅捷展期成功");
            modifyOrderDeferByPayCallback(orderDefer);
        } else if (StringUtils.equals("F", jsonObject.getString("Status"))) {
            //失败
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDefer.setRemark("畅捷展期失败：" + jsonObject.getString("RetMsg"));
            modifyOrderDeferByPayCallback(orderDefer);
            return null;
        } else {
            //处理中
            return "DOING";
        }
        return seriesNo;
    }

    @Override
    public String changjieDeferRepay4Query(String repayNo) {
        logger.info("#[畅捷续期时订单协议支付还款结果查询]-[开始]-repayNo={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            logger.info("参数为空");
            return null;
        }
        //根据支付流水号查询续期订单信息
        OrderDefer orderDefer = selectByPayNo(repayNo);
        logger.info("#[根据支付流水号查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
        if (orderDefer == null) {
            logger.info("根据支付流水号查询续期订单信息为空");
            return null;
        }
        //幂等
        if (!orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
            logger.info("该续期订单的状态不是受理成功的");
            return null;
        }
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已还款成功的");
            return null;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return null;
        }
        //每次请求唯一流水号
        String seriesNo = StringUtil.getOrderNumber("d");
        TransCode4QueryRequest transCode4QueryRequest = new TransCode4QueryRequest();
        transCode4QueryRequest.setRequestSeriesNo(seriesNo);
        transCode4QueryRequest.setSeriesNo(repayNo);
        transCode4QueryRequest.setPartnerId(merchant.getCjPartnerId());
        transCode4QueryRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        transCode4QueryRequest.setPublicKey(merchant.getCjPublicKey());
        //去调畅捷协议支付还款结果查询
        String result = changjieRepayService.bindBankCard4RepayQuery(transCode4QueryRequest);
        if (null == result) {
            logger.info("续期时去调畅捷协议支付还款结果查询返回为空");
            return null;
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //畅捷协议支付还款确认成功
        if (StringUtils.equals("S", jsonObject.getString("Status")) && (StringUtils.equals(ChangjiePayOrRepayOrQueryReturnCodeEnum.SUCCESS_QT000000.getCode(), jsonObject.getString("AppRetcode")))) {
            //成功
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDefer.setRemark("畅捷展期成功");
            modifyOrderDeferByPayCallback(orderDefer);
        } else if (StringUtils.equals("F", jsonObject.getString("Status"))) {
            //失败
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDefer.setRemark("畅捷展期失败：" + jsonObject.getString("RetMsg"));
            modifyOrderDeferByPayCallback(orderDefer);
            return null;
        }
        logger.info("#[畅捷续期时订单协议支付还款结果查询]-[结束]");
        return repayNo;
    }

    @Override
    @Async
    public void changjieDeferRepayCallback(Map<String, String> map, String sign) {
        String outerTradeNo = MapUtils.getString(map, "outer_trade_no");
        String tradeStatus = MapUtils.getString(map, "trade_status");
        logger.info("#[畅捷续期时订单协议支付还款异步回调-还款订单流水号、状态]-outerTradeNo={},tradeStatus={},sign={}", outerTradeNo, tradeStatus, sign);
        try {
            //线程停滞1分钟
            Thread.sleep(60000);
            //根据支付流水号查询续期订单信息
            OrderDefer orderDefer = selectByPayNo(outerTradeNo);
            logger.info("#[根据支付流水号查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
            if (orderDefer == null) {
                logger.info("根据支付流水号查询续期订单信息为空");
                return;
            }
            //幂等
            if (!orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
                logger.info("该续期订单的状态不是受理成功的");
                return;
            }
            if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
                logger.info("该续期订单的状态是已还款成功的");
                return;
            }
            //获取商户信息
            Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
            if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return;
            }
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
                //成功
                if (ChangjieRePayCallBackStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus) || ChangjieRePayCallBackStatusEnum.TRADE_FINISHED.getCode().equals(tradeStatus)) {
                    orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
                    orderDefer.setRemark("畅捷展期成功");
                    modifyOrderDeferByPayCallback(orderDefer);
                } else if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_FAIL.getCode().equals(tradeStatus)) {
                    orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
                    orderDefer.setRemark("畅捷展期失败");
                    modifyOrderDeferByPayCallback(orderDefer);
                }
            }
        } catch (Exception e) {
            logger.error("#[异常]-e={}", e);
        }
    }

    @Override
    public ResultMessage kuaiqianDeferRepay(Long orderId) {
        logger.info("#[快钱续期时订单支付还款]-[开始]-orderId={}", orderId);
        if (null == orderId) {
            logger.info("参数为空");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "参数为空");
        }
        //根据订单id查询续期订单信息
        OrderDefer orderDefer = findLastValidByOrderId(orderId);
        logger.info("#[根据订单id查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
        if (orderDefer == null) {
            logger.info("根据订单id查询续期订单信息为空");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期订单信息为空");
        }
        //幂等
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已受理成功的");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该续期订单的状态是已受理成功的");
        }
        if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("该续期订单的状态是已还款成功的");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该续期订单的状态是已还款成功的");
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getKqMerchantId()) || StringUtils.isBlank(merchant.getKqTerminalId()) || StringUtils.isBlank(merchant.getKqCertPath()) || StringUtils.isBlank(merchant.getKqCertPwd())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该商户信息异常");
        }
        //获取该订单的银行卡号信息
        UserBank userBank = userBankService.selectUserCurrentBankCard(orderDefer.getUid());
        logger.info("#[获取该订单的银行卡号信息]-userBank={}", JSONObject.toJSON(userBank));
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
        String customerId = userBank.getCardPhone() + "_" + String.valueOf(orderDefer.getUid());
        //交易时间
        String entryTime = TimeUtils.parseTime(new Date(), TimeUtils.dateformat5);
        //支付协议号
        String payToken = userBank.getForeignId();
        // 支付流水号
        String repayNo = StringUtil.getOrderNumber("d");
        //还款金额
        BigDecimal amount = "dev".equals(Constant.ENVIROMENT) ? new BigDecimal("0.01") : new BigDecimal(orderDefer.getDeferTotalFee()).setScale(2, BigDecimal.ROUND_HALF_UP);
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
            logger.info("#[续期时快钱支付还款返回结果]-respMap={}", JSONObject.toJSON(respMap));
        } catch (Exception e) {
            logger.info("续期时快钱支付还款异常--订单号为{}，卡号为{}，银行名称为{}", orderId, userBank.getCardNo(), userBank.getCardName());
            logger.error("续期时快钱支付还款异常e={}", e);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期时支付还款异常");
        }
        orderDefer.setPayNo(repayNo);
        //还款成功
        if ("00".equals(MapUtils.getString(respMap, "responseCode"))) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDefer.setRemark("快钱展期成功");
            modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M2000, repayNo);
        }
        //订单已创建，受理中
        else if ("C0".equals(MapUtils.getString(respMap, "responseCode"))
                || "68".equals(MapUtils.getString(respMap, "responseCode"))) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderDefer.setRemark("快钱展期处理中");
            modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M2000, repayNo);
        } else {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDefer.setRemark("快钱展期失败：" + MapUtils.getString(respMap, "responseTextMessage"));
            modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
        }
    }

    @Override
    public ResultMessage kuaiqianDeferRepayQuery(Long orderId) {
        logger.info("#[快钱续期时订单支付还款结果查询]-[开始]-orderId={}", orderId);
        if (null == orderId) {
            logger.info("参数为空");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "参数为空");
        }
        //根据订单id查询续期订单信息
        OrderDefer orderDefer = findLastValidByOrderId(orderId);
        logger.info("#[根据订单id查询续期订单信息]-orderDefer={}", JSONObject.toJSON(orderDefer));
        if (orderDefer == null) {
            logger.info("根据订单id查询续期订单信息为空");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期订单信息为空");
        }
        //幂等
        if (!orderDefer.getPayStatus().equals(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode())) {
            logger.info("该续期订单的状态不是受理成功的");
            if (orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
                logger.info("该续期订单的状态是已还款成功的");
                modifyOrderDeferByPayCallback(orderDefer);
                return new ResultMessage(ResponseEnum.M2000, String.valueOf(orderId));
            }
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该续期订单的状态不是受理成功的");
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getKqMerchantId()) || StringUtils.isBlank(merchant.getKqTerminalId()) || StringUtils.isBlank(merchant.getKqCertPath()) || StringUtils.isBlank(merchant.getKqCertPwd())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该商户信息异常");
        }
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
        String repayNo = orderDefer.getPayNo();
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
            logger.info("#[续期时快钱支付还款结果查询返回结果]-respMap={}", JSONObject.toJSON(respMap));
        } catch (Exception e) {
            logger.error("续期时快钱支付还款结果查询异常-e={}", e);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期时支付还款结果查询异常");
        }
        //还款成功
        if ("00".equals(MapUtils.getString(respMap, "responseCode"))) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDefer.setRemark("快钱展期成功");
            modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M2000, repayNo);
        }
        //订单已创建，受理中
        else if ("C0".equals(MapUtils.getString(respMap, "responseCode"))
                || "68".equals(MapUtils.getString(respMap, "responseCode"))) {
            return new ResultMessage(ResponseEnum.M2000, repayNo);
        } else {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDefer.setRemark("快钱展期失败：" + MapUtils.getString(respMap, "responseTextMessage"));
            modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
        }
    }
}
