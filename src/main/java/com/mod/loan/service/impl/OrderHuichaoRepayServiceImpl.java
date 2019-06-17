package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderRepayMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.request.AliAppH5RepayQueryRequest;
import com.mod.loan.model.request.AliAppH5RepayRequest;
import com.mod.loan.service.HuichaoRepayService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderHuichaoRepayService;
import com.mod.loan.service.OrderRepayService;
import com.mod.loan.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author NIELIN
 * @version $Id: OrderHuichaoRepayServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class OrderHuichaoRepayServiceImpl implements OrderHuichaoRepayService {
    private static Logger logger = LoggerFactory.getLogger(OrderHuichaoRepayServiceImpl.class);

    @Autowired
    HuichaoRepayService huichaoRepayService;
    @Autowired
    MerchantService merchantService;
    @Autowired
    OrderRepayService orderRepayService;
    @Autowired
    OrderRepayMapper orderRepayMapper;
    @Autowired
    OrderMapper orderMapper;


    @Override
    public String huichaoRepay4AliAppH5(Long orderId) {
        logger.info("#[汇潮订单支付宝还款]-[开始]-orderId={}", orderId);
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
            if (null == merchant || StringUtils.isBlank(merchant.getHuichaoMerid()) || StringUtils.isBlank(merchant.getHuichaoMerchantRepayPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return null;
            }
            //获取用户信息
            //唯一流水号
            String repayNo = StringUtil.getOrderNumber("r");
            String amount = "dev".equals(Constant.ENVIROMENT) ? "1.00" : order.getShouldRepay().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            AliAppH5RepayRequest aliAppH5RepayRequest = new AliAppH5RepayRequest();
            aliAppH5RepayRequest.setRequestSeriesNo(repayNo);
            aliAppH5RepayRequest.setAmount(new BigDecimal(amount));
            aliAppH5RepayRequest.setPartnerId(merchant.getHuichaoMerid());
            aliAppH5RepayRequest.setPrivateKey(merchant.getHuichaoMerchantPayPrivateKey());
            aliAppH5RepayRequest.setPublicKey(merchant.getHuichaoPublicKey());
            aliAppH5RepayRequest.setPrivateKey4Repay(merchant.getHuichaoMerchantRepayPrivateKey());
            //去调汇潮支付宝还款
            String result = huichaoRepayService.aliAppH5RepayUrl(aliAppH5RepayRequest);
            if (null == result) {
                logger.info("#[去调汇潮支付宝还款]-[返回结果为空]");
                return null;
            }
            //落还款记录表
            OrderRepay orderRepay = new OrderRepay();
            orderRepay.setRepayNo(repayNo);
            orderRepay.setUid(order.getUid());
            orderRepay.setOrderId(order.getId());
            //1-银行卡,2-支付宝，3-微信，4-线下转账
            orderRepay.setRepayType(2);
            orderRepay.setRepayMoney(new BigDecimal(amount));
            orderRepay.setCreateTime(new Date());
            orderRepay.setUpdateTime(new Date());
            //受理成功
            orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderRepay.setRemark("汇潮订单支付宝还款受理成功");
            orderRepayMapper.insertSelective(orderRepay);
            logger.info("#[汇潮订单支付宝还款]-[结束]-result={}", result);
            return result;
        }
        return null;
    }

    @Override
    public String huichaoRepay4AliAppH5OrWxScanQuery(String repayNo) {
        logger.info("#[汇潮订单支付宝还款结果查询]-[开始]-repayNo={}", repayNo);
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
        //幂等
        if (1 != orderRepay.getRepayStatus()) {
            logger.info("该笔还款流水状态不是受理成功");
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
        Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getHuichaoMerid()) || StringUtils.isBlank(merchant.getHuichaoMerchantRepayPrivateKey())) {
            logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return null;
        }
        //每次请求唯一流水号
        AliAppH5RepayQueryRequest aliAppH5RepayQueryRequest = new AliAppH5RepayQueryRequest();
        aliAppH5RepayQueryRequest.setSeriesNo(repayNo);
        aliAppH5RepayQueryRequest.setPartnerId(merchant.getHuichaoMerid());
        aliAppH5RepayQueryRequest.setPrivateKey(merchant.getHuichaoMerchantPayPrivateKey());
        aliAppH5RepayQueryRequest.setPublicKey(merchant.getHuichaoPublicKey());
        aliAppH5RepayQueryRequest.setPrivateKey4Repay(merchant.getHuichaoMerchantRepayPrivateKey());
        //去调汇潮支付宝还款结果查询
        String result = huichaoRepayService.aliAppH5OrWxScanRepayQuery(aliAppH5RepayQueryRequest);
        if (null == result) {
            logger.info("去调汇潮支付宝还款结果查询返回为空");
            return null;
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer payResult = jsonObject.getInteger("payResult");
        //成功
        if (1 == payResult) {
            orderRepayService.repaySuccess(orderRepay, order);
        }
        //失败
        else if (0 == payResult) {
            orderRepayService.repayFailed(orderRepay, jsonObject.getString("msg"));
        }
        //失败
        else if (!StringUtils.isBlank(jsonObject.getString("code"))) {
            orderRepayService.repayFailed(orderRepay, jsonObject.getString("msg"));
        }
        logger.info("#[汇潮订单支付宝还款结果查询]-[结束]");
        return repayNo;
    }

    @Override
    public String huichaoRepay4WxScan(Long orderId) {
        logger.info("#[汇潮订单微信扫码支付]-[开始]-orderId={}", orderId);
        if (null == orderId) {
            logger.info("参数为空");
            return null;
        }
        //幂等
        if (orderRepayMapper.countRepaySuccess(orderId) >= 1) {
            logger.info("已存在还款中的记录");
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
            if (null == merchant || StringUtils.isBlank(merchant.getHuichaoMerid()) || StringUtils.isBlank(merchant.getHuichaoMerchantRepayPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return null;
            }
            //获取用户信息
            //唯一流水号
            String repayNo = StringUtil.getOrderNumber("r");
            String amount = "dev".equals(Constant.ENVIROMENT) ? "1.00" : order.getShouldRepay().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            AliAppH5RepayRequest aliAppH5RepayRequest = new AliAppH5RepayRequest();
            aliAppH5RepayRequest.setRequestSeriesNo(repayNo);
            aliAppH5RepayRequest.setAmount(new BigDecimal(amount));
            aliAppH5RepayRequest.setPartnerId(merchant.getHuichaoMerid());
            aliAppH5RepayRequest.setPrivateKey(merchant.getHuichaoMerchantPayPrivateKey());
            aliAppH5RepayRequest.setPublicKey(merchant.getHuichaoPublicKey());
            aliAppH5RepayRequest.setPrivateKey4Repay(merchant.getHuichaoMerchantRepayPrivateKey());
            //去调汇潮微信扫码支付
            String result = huichaoRepayService.wxScanRepay(aliAppH5RepayRequest);
            if (null == result) {
                logger.info("#[去调汇潮微信扫码支付]-[返回结果为空]");
                return null;
            }
            //落还款记录表
            OrderRepay orderRepay = new OrderRepay();
            orderRepay.setRepayNo(repayNo);
            orderRepay.setUid(order.getUid());
            orderRepay.setOrderId(order.getId());
            //1-银行卡,2-支付宝，3-微信，4-线下转账
            orderRepay.setRepayType(3);
            orderRepay.setRepayMoney(new BigDecimal(amount));
            orderRepay.setCreateTime(new Date());
            orderRepay.setUpdateTime(new Date());
            //受理成功
            orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderRepay.setRemark("汇潮订单微信扫码支付受理成功");
            orderRepayMapper.insertSelective(orderRepay);
            logger.info("#[汇潮订单微信扫码支付]-[结束]-result={}", result);
            return result;
        }
        return null;
    }
}
