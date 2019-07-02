package com.mod.loan.controller.order;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.model.*;
import com.mod.loan.model.dto.LoanBefore;
import com.mod.loan.model.dto.OrderStatusDTO;
import com.mod.loan.service.*;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.jwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商城贷款接口文案，用户当前，订单明细，订单列表
 *
 * @author yhx 2018年4月26日 下午13:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class LoanOrderController {
    private static Logger logger = LoggerFactory.getLogger(LoanOrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserIdentService userIdentService;
    @Value("${server.h5.url}")
    private String h5_url;
    @Autowired
    private MerchantRateService merchantRateService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    MerchantQuotaConfigService merchantQuotaConfigService;
    @Autowired
    OrderRiskInfoService orderRiskInfoService;
    @Autowired
    UserService userService;

    /**
     * api h5获取额度 首页
     */
    @RequestMapping(value = "loan_home")
    public ResultMessage loan_home(String token) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", 0);
        map.put("descTop", "填写身份资料即可借款");
        map.put("descMid", "获取额度");
        map.put("descBottom", "目前已有50000+人在这里成功借款");
        map.put("amount", "1000-5000");
        map.put("url", Constant.SERVER_H5_URL + "user/mx_risk.html");
        if (StringUtils.isBlank(token)) {
            return new ResultMessage(ResponseEnum.M2000.getCode(), map);
        }
        Claims verifyToken = jwtUtil.ParseJwt(token);
        if (verifyToken == null) {
            return new ResultMessage(ResponseEnum.M2000.getCode(), map);
        }
        String uid = String.valueOf(verifyToken.get("uid"));
        String merchant = String.valueOf(verifyToken.get("clientAlias"));
        UserIdent userIdent = userIdentService.selectByPrimaryKey(Long.parseLong(uid));
        if (null != userIdent && 2 == userIdent.getRealName() && 2 == userIdent.getUserDetails()
                && 2 == userIdent.getMobile() && 2 == userIdent.getLiveness() && 2 == userIdent.getAlipay()) {
            Integer borrowType = orderService.countPaySuccessByUid(Long.parseLong(uid));
            MerchantRate merchantRate = merchantRateService.findByMerchantAndBorrowType(merchant, borrowType);
            BigDecimal money = merchantRate.getProductMoney();

            BigDecimal maxQuota = merchantQuotaConfigService.computeQuota(RequestThread.getClientAlias(), Long.valueOf(uid),
                    merchantRate.getProductMoney(), merchantRate.getBorrowType());
            money = maxQuota;

            map.put("descMid", "去借钱");
            map.put("descTop", "");
            map.put("amount", money.intValue());
            if (2 == userIdent.getBindbank()) {
                map.put("status", 2);
                map.put("url", Constant.SERVER_H5_URL + "order/store_order_apply.html");
            } else {
                map.put("status", 1);
                map.put("url", Constant.SERVER_H5_URL + "user/bank_card.html");
            }
        }
        return new ResultMessage(ResponseEnum.M2000.getCode(), map);
    }

    /**
     * h5  api 首页 当前订单进度
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_current_order")
    public ResultMessage loan_current_order() {
        Long uid = RequestThread.getUid();
        Map<String, Object> map = new HashMap<String, Object>();
        Order order = orderService.findUserLatestOrder(uid);
        if (null == order || OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
            map.put("orderStatus", 0);//0-首页显示获取额度
            return new ResultMessage(ResponseEnum.M2000.getCode(), map);
        }
        map.put("orderId", order.getId());
        List<LoanBefore> loanBeforeList = new ArrayList<>();
        String createdTime = "";
        if (order.getCreateTime() != null) {
            createdTime = TimeUtils.parseTime(order.getCreateTime(), TimeUtils.dateformat0);
        }
        if (OrderEnum.DAI_FUKUAN.getCode().equals(order.getStatus())
                || OrderEnum.WAIT_AUDIT.getCode().equals(order.getStatus())) {//初始提交
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("等待审核  ");
            loanBefore2.setEventTime(createdTime);
            loanBefore2.setEventDescribe("您的订单正在快速审核，请耐心等待");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);

            map.put("orderStatus", 1);//1-订单流程图
            map.put("loanBeforeList", loanBeforeList);
            return new ResultMessage(ResponseEnum.M2000, map);
        } else if (OrderEnum.WAIT_LOAN.getCode().equals(order.getStatus())
                || OrderEnum.LOANING.getCode().equals(order.getStatus())
                || OrderEnum.LOAN_FAILED.getCode().equals(order.getStatus())) {//待放款
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("恭喜您通过审核");

            LoanBefore loanBefore3 = new LoanBefore();
            loanBefore3.setEvent("等待放款");
            loanBefore3.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore3.setEventDescribe("已进入放款状态,请您耐心等待");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
            loanBeforeList.add(loanBefore3);

            map.put("orderStatus", 1);//1-订单流程图
            map.put("loanBeforeList", loanBeforeList);
            return new ResultMessage(ResponseEnum.M2000, map);
        } else if (OrderEnum.AUTO_AUDIT_REFUSE.getCode().equals(order.getStatus())
                || OrderEnum.AUDIT_REFUSE.getCode().equals(order.getStatus())
                || OrderEnum.CANCEL.getCode().equals(order.getStatus())) {//复审失败
            //审核拒绝的订单7天后可再下单
            DateTime dd1 = new DateTime(order.getCreateTime());
            if (dd1.withMillisOfDay(0).plusDays(7).isBeforeNow()) {
                map.put("orderStatus", 0);////0-首页显示获取额度
                return new ResultMessage(ResponseEnum.M2000.getCode(), map);
            }

            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("初审通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("小胖墩、飞金鱼等40+优质资金方可供您借款，点击详情查看");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);

            map.put("orderStatus", 2);//2-审核失败图
            int nextOrderRemainDays = 7 - Days.daysBetween(dd1, DateTime.now()).getDays();
            map.put("nextOrderRemainDays", nextOrderRemainDays);//下次下单剩余天数

            Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
            if (StringUtils.isBlank(merchant.getMerchantMarket())) {
                map.put("url", Constant.SERVER_H5_URL + "market.html?");
            } else if ("order".equals(merchant.getMerchantMarket())) {
                map.put("url", Constant.SERVER_H5_URL + "order/store_order_detail.html?orderId=" + order.getId());
            } else {
                map.put("url", merchant.getMerchantMarket());
            }

            map.put("loanBeforeList", loanBeforeList);
            return new ResultMessage(ResponseEnum.M2000, map);
        } else if (OrderEnum.REPAYING.getCode().equals(order.getStatus())
                || OrderEnum.REPAY_CONFIRMING.getCode().equals(order.getStatus())
                || OrderEnum.DEFER.getCode().equals(order.getStatus())
                || OrderEnum.OVERDUE_DEFER.getCode().equals(order.getStatus())) {//已放款
            if (null != order.getRepayTime()) {
                DateTime d1 = new DateTime(new Date());
                DateTime d2 = new DateTime(order.getRepayTime());
                int remainDays = Days.daysBetween(d1, d2).getDays() + 1;
                if (new DateTime(order.getRepayTime()).toString("yyyy-MM-dd ").equals(d1.toString("yyyy-MM-dd "))) {
                    remainDays = 0;
                }
                map.put("shouldRepay", StringUtil.moneyFormat(order.getShouldRepay()));
                map.put("lastRepayTime", new DateTime(order.getRepayTime()).toString("yyyy-MM-dd "));
                map.put("remainDays", remainDays);
            }
            map.put("orderStatus", 3);//3-我要还款
            map.put("url", Constant.SERVER_H5_URL + "order/store_order_detail.html?orderId=" + order.getId());
            return new ResultMessage(ResponseEnum.M2000, map);
        } else if (OrderEnum.OVERDUE.getCode().equals(order.getStatus())
                || OrderEnum.BAD_DEBTS.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_OVERDUE.getCode().equals(order.getStatus())) {//逾期或坏账
            if (null != order.getRepayTime()) {
                map.put("shouldRepay", StringUtil.moneyFormat(order.getShouldRepay()));
                map.put("lastRepayTime", new DateTime(order.getRepayTime()).toString("yyyy-MM-dd "));
                map.put("remainDays", order.getOverdueDay());
            }
            map.put("orderStatus", 4);//4-逾期还款
            map.put("url", Constant.SERVER_H5_URL + "order/store_order_detail.html?orderId=" + order.getId());
        }
        return new ResultMessage(ResponseEnum.M2000, map);
    }

    /**
     * h5 订单记录
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_order_records")
    public ResultMessage loan_order_records() {
        Long uid = RequestThread.getUid();
        List<Order> orderList = orderService.getByUid(uid);
        if (orderList.size() == 0) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "暂无数据！");
        }
        List<OrderStatusDTO> orderStatus = new ArrayList<OrderStatusDTO>();
        for (Order order : orderList) {
            OrderStatusDTO statusDTO = new OrderStatusDTO();
            statusDTO.setOrderId(String.valueOf(order.getId()));
            statusDTO.setBorrowMoney(StringUtil.moneyFormat(order.getBorrowMoney()));
            statusDTO.setStrCreateTime(TimeUtils.parseTime(order.getCreateTime(), TimeUtils.dateformat0));
            if (11 == order.getStatus() || 12 == order.getStatus()) {
                statusDTO.setStatusDesc("待审核");
            } else if (51 == order.getStatus() || 52 == order.getStatus()) {
                statusDTO.setStatusDesc("审核失败");
            } else if (53 == order.getStatus()) {
                statusDTO.setStatusDesc("订单取消");
            } else if (21 == order.getStatus() || 22 == order.getStatus() || 23 == order.getStatus()) {
                statusDTO.setStatusDesc("待放款");
            } else if (31 == order.getStatus() || 32 == order.getStatus()) {
                statusDTO.setStatusDesc("放款成功，待还款");
            } else if (41 == order.getStatus() || 42 == order.getStatus() || 43 == order.getStatus()) {
                statusDTO.setStatusDesc("已还款");
            } else if (OrderEnum.OVERDUE.getCode().equals(order.getStatus())
                    || OrderEnum.BAD_DEBTS.getCode().equals(order.getStatus())
                    || OrderEnum.DEFER_OVERDUE.getCode().equals(order.getStatus())) {
                statusDTO.setStatusDesc("已逾期");
            } else if (OrderEnum.DEFER.getCode().equals(order.getStatus()) || OrderEnum.OVERDUE_DEFER.getCode().equals(order.getStatus())) {
                statusDTO.setStatusDesc("展期中");
            } else {
                statusDTO.setStatusDesc("");//其它
            }
            orderStatus.add(statusDTO);
        }
        return new ResultMessage(ResponseEnum.M2000, orderStatus);
    }

    /**
     * h5 订单记录 订单明细
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_order_detail")
    public ResultMessage loan_order_detail(@RequestParam(required = true) Long orderId) {
        Map<String, Object> map = new HashMap<String, Object>();
        Order order = orderService.selectByPrimaryKey(orderId);
        if (null == order) {
            return new ResultMessage(ResponseEnum.M4000);
        }
        if (!order.getUid().equals(RequestThread.getUid())) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
        }
        map.put("orderId", orderId);
        //租期
        map.put("borrowDay", order.getBorrowDay());
        //租金
        map.put("totalFee", order.getTotalFee());
        map.put("reduceMoney", order.getReduceMoney());
        //实收金额
        map.put("actualMoney", order.getActualMoney());
        //回购金额
        map.put("shouldRepay", order.getShouldRepay());
        //逾期天数
        map.put("overdueDay", order.getOverdueDay());
        //延期金
        map.put("overdueFee", order.getOverdueFee());
        //应回购日期
        map.put("repayTime", order.getRepayTime() == null ? "" : new DateTime(order.getRepayTime()).toString(TimeUtils.dateformat2));
        //默认0不需要显示还款按钮
        map.put("status", 0);

        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        //是否需要用户确认借款，0：不需要，1：需要
        Integer userPayConfirm = merchant.getUserPayConfirm();
        if (userPayConfirm == 1) {
            //是否已经确认放款，0：未确认，1：已确认
            if (order.getPayConfirmStatus() == 0) {
                map.put("payConfirmStatus", 0);
            } else {
                map.put("payConfirmStatus", 1);
            }
        } else {
            map.put("payConfirmStatus", 1);
        }

        OrderPay orderPay = orderService.findOrderPaySuccessRecord(orderId);
        if (null != orderPay) {
            map.put("cardNo", orderPay.getBank() + "(尾号" + StringUtil.bankTailNo(orderPay.getBankNo()) + ")");
        } else {
            map.put("cardNo", "");
        }
        List<LoanBefore> loanBeforeList = new ArrayList<LoanBefore>();
        String createdTime = "";
        if (order.getCreateTime() != null) {
            createdTime = TimeUtils.parseTime(order.getCreateTime(), TimeUtils.dateformat0);
        }
        if (OrderEnum.DAI_FUKUAN.getCode().equals(order.getStatus()) || OrderEnum.WAIT_AUDIT.getCode().equals(order.getStatus())) {//初始提交
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("等待审核 ");
            loanBefore2.setEventTime(createdTime);
            loanBefore2.setEventDescribe("您的订单正在快速审核，请耐心等待");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
        } else if (OrderEnum.AUTO_AUDIT_REFUSE.getCode().equals(order.getStatus())
                || OrderEnum.AUDIT_REFUSE.getCode().equals(order.getStatus())
                || OrderEnum.CANCEL.getCode().equals(order.getStatus())) {//复审失败
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核失败");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("您的信用评级未达到平台要求");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
        } else if (OrderEnum.WAIT_LOAN.getCode().equals(order.getStatus())
                || OrderEnum.LOANING.getCode().equals(order.getStatus())
                || OrderEnum.LOAN_FAILED.getCode().equals(order.getStatus())) {//待放款
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("恭喜您通过审核");

            LoanBefore loanBefore3 = new LoanBefore();
            loanBefore3.setEvent("等待放款");
            loanBefore3.setEventDescribe("已进入放款状态,请您耐心等待");
            loanBefore3.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
            loanBeforeList.add(loanBefore3);
        } else if (OrderEnum.REPAYING.getCode().equals(order.getStatus())
                || OrderEnum.DEFER.getCode().equals(order.getStatus())
                || OrderEnum.OVERDUE_DEFER.getCode().equals(order.getStatus())) {//已放款
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("恭喜您通过审核");

            LoanBefore loanBefore3 = new LoanBefore();
            loanBefore3.setEvent("打款成功");
            loanBefore3.setEventTime(TimeUtils.parseTime(order.getArriveTime(), TimeUtils.dateformat0));
            loanBefore3.setEventDescribe("请及时核对资金账户");

            DateTime d1 = new DateTime(new Date());
            DateTime d2 = new DateTime(order.getRepayTime());
            int remainDays = Days.daysBetween(d1, d2).getDays() + 1;
            if (new DateTime(order.getRepayTime()).toString("yyyy-MM-dd ").equals(d1.toString("yyyy-MM-dd "))) {
                remainDays = 0;
            }

            LoanBefore loanBefore4 = new LoanBefore();
            loanBefore4.setEvent(remainDays + "天后还款");
            loanBefore4.setEventTime("");
            loanBefore4.setEventDescribe("为避免对您的信用产生影响，请务必及时还款");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
            loanBeforeList.add(loanBefore3);
            loanBeforeList.add(loanBefore4);
            map.put("status", 1);//
        } else if (OrderEnum.OVERDUE.getCode().equals(order.getStatus())
                || OrderEnum.BAD_DEBTS.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_OVERDUE.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_BAD_DEBTS.getCode().equals(order.getStatus())) {//已逾期或坏账状态
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("恭喜您通过审核");

            LoanBefore loanBefore3 = new LoanBefore();
            loanBefore3.setEvent("打款成功");
            loanBefore3.setEventTime(TimeUtils.parseTime(order.getArriveTime(), TimeUtils.dateformat0));
            loanBefore3.setEventDescribe("请及时核对资金账户");

            LoanBefore loanBefore4 = new LoanBefore();
            loanBefore4.setEvent("您已逾期" + order.getOverdueDay() + "天");
            loanBefore4.setEventTime("");
            loanBefore4.setEventDescribe("为避免对您的信用产生影响，请务必及时还款");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
            loanBeforeList.add(loanBefore3);
            loanBeforeList.add(loanBefore4);

            map.put("status", 1);//
        } else if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus())
                || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {//已还款
            LoanBefore loanBefore = new LoanBefore();
            loanBefore.setEvent("申请提交成功 ");
            loanBefore.setEventTime(createdTime);
            loanBefore.setEventDescribe(String.format("申请周转资金%s元，周期%s天，服务费%s元，到账%s元", order.getBorrowMoney(), order.getBorrowDay(), order.getTotalFee(), order.getActualMoney()));

            LoanBefore loanBefore2 = new LoanBefore();
            loanBefore2.setEvent("审核通过");
            loanBefore2.setEventTime(TimeUtils.parseTime(order.getAuditTime(), TimeUtils.dateformat0));
            loanBefore2.setEventDescribe("恭喜您通过审核");

            LoanBefore loanBefore3 = new LoanBefore();
            loanBefore3.setEvent("打款成功");
            loanBefore3.setEventTime(TimeUtils.parseTime(order.getArriveTime(), TimeUtils.dateformat0));
            loanBefore3.setEventDescribe("请及时核对资金账户");

            LoanBefore loanBefore4 = new LoanBefore();
            loanBefore4.setEvent("已还款");
            loanBefore4.setEventTime(TimeUtils.parseTime(order.getRealRepayTime(), TimeUtils.dateformat0));
            loanBefore4.setEventDescribe("恭喜您，还款成功!");

            loanBeforeList.add(loanBefore);
            loanBeforeList.add(loanBefore2);
            loanBeforeList.add(loanBefore3);
            loanBeforeList.add(loanBefore4);
        }
        map.put("processList", loanBeforeList);
        return new ResultMessage(ResponseEnum.M2000, map);
    }

    /**
     * h5 用户订单数
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_order_count")
    public ResultMessage loan_order_count() {
        Integer count = orderService.countByUid(RequestThread.getUid());
        Map<String, Integer> data = new HashMap<String, Integer>();
        data.put("count", count);
        return new ResultMessage(ResponseEnum.M2000, data);
    }

    /**
     * 按条件是否显示贷超按钮
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_market_button")
    public ResultMessage loanMarketButton(Long orderId) {
        Map<String, Object> map = new HashMap<>();
        int showButton = 0;
        String alias = RequestThread.getClientAlias();
        String merchantNeedLoanMarketButton = Constant.MERCHANT_NEED_LOAN_MARKET_BUTTON;
        if (StringUtils.isNotEmpty(merchantNeedLoanMarketButton)
                && StringUtils.isNotEmpty(alias)
                && merchantNeedLoanMarketButton.contains(alias)) {
            try {
                OrderRiskInfo orderRiskInfo = orderRiskInfoService.getLastOneByOrderId(orderId);
                String riskModelScore = orderRiskInfo.getRiskModelScore();
                if (StringUtils.isEmpty(riskModelScore)) {
                    riskModelScore = orderRiskInfoService.updateRiskMotelScore(orderRiskInfo.getId());
                }
                JSONObject riskModelScoreJson = JSON.parseObject(riskModelScore);
                String tianjiScore = riskModelScoreJson.getString("天机-小额模型分");
                //天机分520分以下显示按钮
                if (Double.valueOf(tianjiScore) < 520) {
                    showButton = 1;

                    Merchant merchant = merchantService.findMerchantByAlias(alias);
                    if (StringUtils.isBlank(merchant.getMerchantMarket())) {
                        map.put("url", Constant.SERVER_H5_URL + "market.html?");
                    } else if ("order".equals(merchant.getMerchantMarket())) {
                        map.put("url", Constant.SERVER_H5_URL + "order/store_order_detail.html?orderId=" + orderId);
                    } else {
                        map.put("url", merchant.getMerchantMarket());
                    }
                }
            } catch (Exception e) {
                logger.error("loan_market_button orderId={}, err={}", orderId, e);
            }
        }
        map.put("show", showButton);
        return new ResultMessage(ResponseEnum.M2000, map);
    }

    /**
     * 确认借款
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "loan_confirm")
    public ResultMessage loanConfirm(Long orderId) {
        orderService.updatePayConfirmStatus(orderId);
        return new ResultMessage(ResponseEnum.M2000);
    }

}
