package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.MessageHandle;
import com.mod.loan.util.heli.vo.dto.BindPayActiveDto;
import com.mod.loan.util.heli.vo.dto.BindPaySmsCodeDto;
import com.mod.loan.util.heli.vo.request.BindCardPayVo;
import com.mod.loan.util.heli.vo.request.BindPayValidateCodeVo;
import com.mod.loan.util.heli.vo.response.BindCardPayResponseVo;
import com.mod.loan.util.heli.vo.response.BindPayValidateCodeResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Auther: wzg
 * @Date: 2019-05-11 15:22
 * @Description:合利宝支付业务处理类
 */
@Service
public class HelipayRepayServiceImpl implements HelipayRepayService {

    private static Logger logger = LoggerFactory.getLogger(HelipayRepayServiceImpl.class);

    @Value("${helipay.url:}")
    private String helipay_url;
    @Value("${helipay.path:}")
    private String helipay_path;
    @Value("${helipay.pfx.pwd:}")
    private String helipay_pfx_pwd;
    @Autowired
    private UserService userService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepayService orderRepayService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private OrderDeferService deferService;


    /**
     * 合利宝支付,短信验证码发送业务处理方法
     */
    @Override
    public ResultMessage bindPaySmsProcess(String orderId, String type) {
        Long uid = RequestThread.getUid();
        User user = userService.selectByPrimaryKey(uid);
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        if ("order".equals(type)) {
            Order order = orderService.selectByPrimaryKey(NumberUtils.toLong(orderId));
            // 已放款，逾期，坏账，展期，逾期后展期，展期逾期，展期坏账状态
            if (OrderEnum.REPAYING.getCode().equals(order.getStatus())
                    || OrderEnum.OVERDUE.getCode().equals(order.getStatus())
                    || OrderEnum.BAD_DEBTS.getCode().equals(order.getStatus())
                    || OrderEnum.DEFER.getCode().equals(order.getStatus())
                    || OrderEnum.OVERDUE_DEFER.getCode().equals(order.getStatus())
                    || OrderEnum.DEFER_OVERDUE.getCode().equals(order.getStatus())
                    || OrderEnum.DEFER_BAD_DEBTS.getCode().equals(order.getStatus())) {
                // 支付流水号
                return bindPaySmsCode(new BindPaySmsCodeDto(StringUtil.getOrderNumber("r"), order.getShouldRepay().toString(), merchant.getHlb_id(), userBank.getForeignId(),
                        user.getId().toString(), userBank.getCardPhone(), merchant.getMerchantAlias(), orderId));
            }
            return new ResultMessage(ResponseEnum.M4000.getCode(), "该订单状态下不能还款,订单号为:[" + orderId + "],订单状态为:[" + order.getStatus() + "]");
        } else if ("orderDefer".equals(type)) {
            OrderDefer orderDefer = deferService.findLastValidByOrderId(NumberUtils.toLong(orderId));
            if (orderDefer == null) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "续期订单[" + orderId + "]不存在");
            }
            if (orderDefer.getPayStatus() == 3) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "续期订单[" + orderId + "]已支付完成,请勿重复支付");
            }
            orderDefer.setPayNo(StringUtil.getOrderNumber("r"));
            deferService.updateByPrimaryKey(orderDefer);
            return bindPaySmsCode(new BindPaySmsCodeDto(orderDefer.getPayNo(), orderDefer.getDeferFee().toString(), merchant.getHlb_id(), userBank.getForeignId(),
                    user.getId().toString(), userBank.getCardPhone(), merchant.getMerchantAlias(), orderId));
        }
        return new ResultMessage(ResponseEnum.M4000.getCode(), "合利宝绑卡短信发送失败,订单号为:[" + orderId + "],短信类型为:[" + type + "]");
    }

    /**
     * 回购,待付费详情,查询出待付费金额
     */
    @Override
    public ResultMessage repayInfo(String repayNo, String type) {
        Long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text + repayNo));
        if (orderId == 0) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
        }
        Map<String, String> data = new HashMap<String, String>();
        if ("order".equals(type)) {
            Order order = orderService.selectByPrimaryKey(orderId);
            data.put("repayMoney", order.getShouldRepay().toString());
        } else if ("orderDefer".equals(type)) {
            OrderDefer order = deferService.findLastValidByOrderId(orderId);
            data.put("repayMoney", order.getDeferTotalFee().toString());
        }
        return new ResultMessage(ResponseEnum.M2000, data);
    }

    /**
     * 付款请求发起
     */
    @Override
    public ResultMessage repayActive(String repayNo, String validateCode, String type) {
        Long uid = RequestThread.getUid();
        if (validateCode.length() > 6) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码长度过长");
        }
        Long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text + repayNo));
        if (orderId == 0) {
            logger.info("订单异常，uid={},订单号={}", uid, repayNo);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
        }
        User user = userService.selectByPrimaryKey(uid);
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        String amount = "";
        if ("order".equals(type)) {
            Order order = orderService.selectByPrimaryKey(orderId);
            if (!order.getUid().equals(uid)) {
                logger.info("订单异常，订单号为：{}", order.getId());
                return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
            }
            // 已放款，逾期，坏账状态
            if (order.getStatus() >= OrderEnum.NORMAL_REPAY.getCode() || order.getStatus() < OrderEnum.REPAYING.getCode()) {
                logger.info("订单非还款状态，订单号为：{}", order.getId());
                return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
            }
            amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : order.getShouldRepay().toString();
            // 还款记录表
            OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
            if (orderRepay == null) {
                orderRepay = new OrderRepay();
                orderRepay.setRepayNo(repayNo);
                orderRepay.setUid(order.getUid());
                orderRepay.setOrderId(orderId);
                orderRepay.setRepayType(1);
                orderRepay.setRepayMoney(new BigDecimal(amount));
                orderRepay.setBank(userBank.getCardName());
                orderRepay.setBankNo(userBank.getCardNo());
                orderRepay.setCreateTime(new Date());
                orderRepay.setUpdateTime(new Date());
                orderRepay.setRepayStatus(0);
                orderRepayService.insertSelective(orderRepay);
            }
            return bindPayActive(new BindPayActiveDto(repayNo, order.getShouldRepay().toString(), merchant.getHlb_id(), userBank.getForeignId(),
                    user.getId().toString(), userBank.getCardPhone(), merchant.getMerchantAlias(), orderId.toString(),
                    validateCode, Constant.SERVER_API_URL + "order/repay_result"));
        } else if ("orderDefer".equals(type)) {
            OrderDefer order = deferService.findLastValidByOrderId(orderId);
            amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : order.getDeferTotalFee().toString();
            return bindPayActive(new BindPayActiveDto(repayNo, amount, merchant.getHlb_id(), userBank.getForeignId(),
                    user.getId().toString(), userBank.getCardPhone(), merchant.getMerchantAlias(), orderId.toString(),
                    validateCode, Constant.SERVER_API_URL + "order/defer_repay_result"));
        }
        return new ResultMessage(ResponseEnum.M4000.getCode(), "合利宝绑卡短信发送失败,订单号为:[" + orderId + "],短信类型为:[" + type + "]");

    }

    @Override
    public void repayResult(String rt2_retCode,
                            String rt9_orderStatus, String rt5_orderId) {
        // 只处理受理成功并且支付成功的订单
        if ("0000".equals(rt2_retCode) && "SUCCESS".equals(rt9_orderStatus)) {
            OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(rt5_orderId);
            if (orderRepay.getRepayStatus() == 3) {
                return;
            }
            Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
            if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus())
                    || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus())
                    || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
                logger.info("异步通知:订单{}已还款：", order.getId());
                return;
            }
            Order order1 = new Order();
            order1.setId(orderRepay.getOrderId());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(order.getShouldRepay());
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(rt5_orderId);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRepayStatus(3);
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
        } else {
            logger.error("异步通知异常：rt2_retCode={},rt9_orderStatus={},rt5_orderId={}", rt2_retCode, rt9_orderStatus, rt5_orderId);
        }
    }

    @Override
    public void deferRepayResult(String rt2_retCode, String rt9_reason,
                                 String rt9_orderStatus, String rt5_orderId) {
        OrderDefer orderDefer = deferService.selectByPayNo(rt5_orderId);
        if (orderDefer == null) {
            logger.error("异步通知异常,展期订单不存在：rt2_retCode={},rt9_orderStatus={},rt5_orderId={}", rt2_retCode, rt9_orderStatus, rt5_orderId);
            return;
        }
        if (orderDefer.getPayStatus() == 3) {
            logger.info("续期订单[" + orderDefer.getOrderId() + "]已支付完成,请勿重复支付");
            return;
        }
        //备注信息
        orderDefer.setRemark(rt9_orderStatus + ":" + rt9_reason);
        orderDefer.setPayType("helipay");
        // 只处理受理成功并且支付成功的订单
        if ("0000".equals(rt2_retCode) && "SUCCESS".equals(rt9_orderStatus)) {
            orderDefer.setPayStatus(3);
            logger.info("异步通知成功：rt2_retCode={},rt9_orderStatus={},rt5_orderId={}", rt2_retCode, rt9_orderStatus, rt5_orderId);
        } else {
            orderDefer.setPayStatus(4);
            logger.error("异步通知异常：rt2_retCode={},rt9_orderStatus={},rt5_orderId={}", rt2_retCode, rt9_orderStatus, rt5_orderId);
        }
        deferService.modifyOrderDeferByPayCallback(orderDefer);
    }

    /**
     * 合利宝支付,绑卡支付短信发送
     */
    private ResultMessage bindPaySmsCode(BindPaySmsCodeDto dto) {
        ResultMessage message = null;
        String response = null;
        try {
            String amount = "dev".equals(Constant.ENVIROMENT) ? "0.11" : dto.getAmount();
            BindPayValidateCodeVo requestVo = new BindPayValidateCodeVo();
            requestVo.setP1_bizType("QuickPayBindPayValidateCode");
            requestVo.setP2_customerNumber(dto.getHlbId());
            requestVo.setP3_bindId(dto.getForeignId());
            requestVo.setP4_userId(dto.getUserId());
            requestVo.setP5_orderId(dto.getRepayNo());
            requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
            requestVo.setP7_currency("CNY");
            requestVo.setP8_orderAmount(amount);
            requestVo.setP9_phone(dto.getPhone());
            requestVo.setSignatureType("MD5WITHRSA");
            response = getHeliPayResponse(dto.getMerchant(), requestVo, null);
            logger.info("helipay bindPaySmsCode:{}", JSON.toJSONString(requestVo));
            BindPayValidateCodeResponseVo responseVo = JSONObject.parseObject(response, BindPayValidateCodeResponseVo.class);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                redisMapper.set(RedisConst.repay_text + dto.getRepayNo(), dto.getOrderId(), 300);
                return new ResultMessage(ResponseEnum.M2000, dto.getRepayNo());
            } else {
                logger.info("绑卡支付短信受理失败，失败订单号为：{}，失败原因为：{}", dto.getOrderId(), response);
                message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付短信受理失败");
            }

        } catch (Exception e) {
            logger.info("绑卡支付短信受理异常，订单号为：{}，response={}, 错误信息={}", dto.getOrderId(), response, e.getStackTrace());
            message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付短信受理失败");
        }
        return message;
    }

    /**
     * 合利宝绑卡支付支付请求
     */
    private ResultMessage bindPayActive(BindPayActiveDto dto) {
        ResultMessage message = null;
        String response = "";
        BindCardPayVo requestVo = new BindCardPayVo();
        try {
            String ip = RequestThread.getIp();
            requestVo.setP1_bizType("QuickPayBindPay");
            requestVo.setP2_customerNumber(dto.getHlbId());
            requestVo.setP3_bindId(dto.getForeignId());
            requestVo.setP4_userId(dto.getUserId());
            requestVo.setP5_orderId(dto.getRepayNo());
            requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
            requestVo.setP7_currency("CNY");
            requestVo.setP8_orderAmount(dto.getAmount());
            requestVo.setP9_goodsName("回收手机");
            requestVo.setP10_goodsDesc("回收手机");
            requestVo.setP11_terminalType("OTHER");
            requestVo.setP12_terminalId(UUID.randomUUID().toString());
            requestVo.setP13_orderIp(ip);
            requestVo.setP14_period("");
            requestVo.setP15_periodUnit("");
            requestVo.setP16_serverCallbackUrl(dto.getCallBackUrl());
            requestVo.setP17_validateCode(dto.getValidateCode());
            requestVo.setSignatureType("MD5WITHRSA");
            response = getHeliPayResponse(dto.getMerchant(), null, requestVo);
            logger.info("helipay bindPayActive:{}", requestVo);
            BindCardPayResponseVo responseVo = JSONObject.parseObject(response, BindCardPayResponseVo.class);
            if (!"0000".equals(responseVo.getRt2_retCode())) {
                logger.error("绑卡支付受理失败，result={}", response);
                OrderRepay orderRepay1 = new OrderRepay();
                orderRepay1.setRepayNo(dto.getRepayNo());// bug fix,,, 失败设置主键更新
                orderRepay1.setRepayNo(dto.getOrderId());
                orderRepay1.setRepayStatus(2);
                String responseMsg = responseVo.getRt3_retMsg();
                if (StringUtils.isNotBlank(responseMsg) && responseMsg.length() > 30) {
                    responseMsg = responseMsg.substring(0, 30);
                }
                orderRepay1.setRemark(responseMsg);
                orderRepayService.updateByPrimaryKeySelective(orderRepay1);
                return new ResultMessage(ResponseEnum.M4000.getCode(), responseVo.getRt3_retMsg());
            }
            if ("SUCCESS".equalsIgnoreCase(responseVo.getRt9_orderStatus())) {
                return new ResultMessage(ResponseEnum.M2000, dto.getOrderId());// 成功返回订单号，便于查看详情
            }
            if ("DOING".equalsIgnoreCase(responseVo.getRt9_orderStatus())) {
                logger.info("绑卡支付受理中，result={}", response);
                return new ResultMessage(ResponseEnum.M2000.getCode(), dto.getOrderId());// 处理中回订单号，便于查看详情
            }
            logger.info("绑卡支付状态异常，params={}，result={}", JSON.toJSONString(requestVo), response);
            message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付失败，请重试！");
        } catch (Exception e) {
            logger.info("绑卡支付异常，params={}，result={}", JSON.toJSONString(requestVo), response);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    /**
     * 发起合利宝请求
     */
    private String getHeliPayResponse(String merchant, BindPayValidateCodeVo vo1, BindCardPayVo vo2) throws Exception {
        String pfxPath = helipay_path + merchant + ".pfx";
        Map handleMap = MessageHandle.getReqestMap((vo1 == null ? vo2 : vo1), pfxPath, helipay_pfx_pwd);
        return HttpClientService.getHttpResp(handleMap, helipay_url);
    }
}
