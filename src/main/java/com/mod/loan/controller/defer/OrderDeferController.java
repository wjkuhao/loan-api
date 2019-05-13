package com.mod.loan.controller.defer;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 续期订单接口
 *
 * @author kibear
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/order_defer")
public class OrderDeferController {

    private static Logger logger = LoggerFactory.getLogger(OrderDeferController.class);

    private final MerchantDeferConfigService merchantDeferConfigService;
    private final OrderDeferService orderDeferService;
    private final OrderService orderService;
    private final UserService userService;
    private final MerchantService merchantService;
    private final YeepayService yeepayService;

    @Autowired
    public OrderDeferController(MerchantDeferConfigService merchantDeferConfigService, //
                                OrderDeferService orderDeferService, //
                                OrderService orderService, //
                                UserService userService, MerchantService merchantService, YeepayService yeepayService) {
        this.merchantDeferConfigService = merchantDeferConfigService;
        this.orderDeferService = orderDeferService;
        this.orderService = orderService;
        this.userService = userService;
        this.merchantService = merchantService;
        this.yeepayService = yeepayService;
    }

    @GetMapping("/compute")
    public ResultMessage compute(@RequestParam Long orderId,
                                 @RequestParam String merchant,
                                 @RequestParam(defaultValue = "7") Integer deferDay) {
        MerchantDeferConfig condition = new MerchantDeferConfig();
        condition.setMerchant(merchant);
        MerchantDeferConfig merchantDeferConfig = merchantDeferConfigService.selectOne(condition);
        // 检查商户是否支持续期
        if (null == merchantDeferConfig || merchantDeferConfig.getStatus() < 1) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户不支持续期");
        }

        // 计算当前第几次续期
        int deferTimes = orderDeferService.selectCount(new OrderDefer(orderId)) + 1;// 当前续期个数加1
        if (null != merchantDeferConfig.getMaxDeferTimes() //
                && merchantDeferConfig.getMaxDeferTimes() > 0 //
                && deferTimes > merchantDeferConfig.getMaxDeferTimes()) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期次数达到最大限制");
        }
        // 计算续期费和还款时间
        Double dailyDeferFee = merchantDeferConfig.getDailyDeferFee();
        Double dailyDeferRate = merchantDeferConfig.getDailyDeferRate();
        Order order = orderService.selectByPrimaryKey(orderId);
        if (null != dailyDeferRate && dailyDeferRate > 0D) {
            // 如果设置了续期费率
            dailyDeferFee = order.getBorrowMoney().doubleValue() * dailyDeferRate / 100D;
        }
        double deferFee = dailyDeferFee * deferDay;
        // 计算还款时间
        String deferRepayDate = TimeUtil.datePlusDays(order.getRepayTime(), deferDay);
        // 计算逾期费
        Integer overdueDay = null == order.getOverdueDay() ? 0 : order.getOverdueDay();// 逾期天数
        Double overdueFee = null == order.getOverdueFee() ? 0.0D : order.getOverdueFee().doubleValue();// 逾期费
        // 计算总续期费
        Double deferTotalFee = deferFee + overdueFee;
        //如果是null 或者 为支付成功 则可以继续生成下一笔展期订单
        OrderDefer orderDeferOld = orderDeferService.findLastValidByOrderId(orderId);
        if (orderDeferOld == null || orderDeferOld.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            OrderDefer orderDefer = new OrderDefer();
            orderDefer.setOrderId(orderId);
            orderDefer.setDeferTimes(deferTimes);
            orderDefer.setDeferDay(deferDay);
            orderDefer.setDailyDeferFee(dailyDeferFee);
            orderDefer.setDeferFee(deferFee);
            orderDefer.setRepayDate(TimeUtil.dateFormat(order.getRepayTime()));
            orderDefer.setDeferRepayDate(deferRepayDate);
            orderDefer.setOverdueDay(overdueDay);
            orderDefer.setOverdueFee(overdueFee);
            orderDefer.setDeferTotalFee(deferTotalFee);

            User user = userService.selectByPrimaryKey(order.getUid());
            orderDefer.setUserName(user.getUserName());
            orderDefer.setUserPhone(user.getUserPhone());
            orderDefer.setCreateTime(TimeUtil.nowTime());
            orderDefer.setUid(user.getId());
            orderDefer.setMerchant(order.getMerchant());
            orderDefer.setPayStatus(OrderRepayStatusEnum.INIT.getCode());
            orderDeferService.insertSelective(orderDefer);
            return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
        }

        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDeferOld);
    }

    //@PostMapping("/create")
    public ResultMessage create(OrderDefer orderDefer) {

        Order order = orderService.selectByPrimaryKey(orderDefer.getOrderId());
        User user = userService.selectByPrimaryKey(order.getUid());
        orderDefer.setUserName(user.getUserName());
        orderDefer.setUserPhone(user.getUserPhone());
        orderDefer.setCreateTime(TimeUtil.nowTime());
        orderDefer.setUid(user.getId());
        orderDefer.setMerchant(order.getMerchant());
        //
        orderDeferService.insertSelective(orderDefer);
        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
    }

    //@GetMapping("/find_last_valid")
    public ResultMessage findByOrderId(@RequestParam Long orderId) {
        OrderDefer orderDefer = orderDeferService.findLastValidByOrderId(orderId);
        if (null == orderDefer) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "找不到有效的续期单");
        }
        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
    }

    @LoginRequired
    @RequestMapping(value = "yeepay_repay_no_sms")
    public ResultMessage yeepay_repay_no_sms(String orderId) {
        String errMsg = orderDeferService.yeepayDeferNoSms(Long.valueOf(orderId));
        if (errMsg == null) {
            return new ResultMessage(ResponseEnum.M2000, orderId);
        } else {
            return new ResultMessage(ResponseEnum.M4000, errMsg);
        }
    }

    @RequestMapping(value = "repay_callback")
    public String repay_callback(HttpServletRequest request, HttpServletResponse response) {
        String responseMsg = request.getParameter("response");
        String param = request.getParameter("param");

        logger.info("展期易宝异步通知:param={}", param);

        if (StringUtils.isEmpty(responseMsg) || StringUtils.isEmpty(param)) {
            logger.error("responseMsg={},param={}", responseMsg, param);
            logger.error("易宝异步通知:返回为空");
            return "SUCCESS";
        }

        Long uid = Long.valueOf(param);
        User user = userService.selectByPrimaryKey(uid);
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());

        StringBuffer repayNo = new StringBuffer();
        String callbackErr = yeepayService.repayCallbackMultiAcct(merchant.getYeepay_repay_private_key(), responseMsg, repayNo);
        logger.info("展期易宝异步通知:param={},callbackErr={},repayNo={}", param, callbackErr, repayNo);

        //设置OrderRepay
        OrderDefer orderDefer = orderDeferService.selectByPayNo(repayNo.toString());
        //对应的订单不存在 或者 可能已经线下还款
        if (orderDefer == null || orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("展期易宝异步通知:param={}订单已还款", param);
            return "SUCCESS"; //收到通知 固定格式
        }

        if (callbackErr == null) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
        } else {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
        }

        return "SUCCESS"; //收到通知 固定格式
    }


    @LoginRequired
    @RequestMapping(value = "yeepay_repay_query")
    public ResultMessage yeepay_repay_query(Long orderId) {
        Order order = orderService.selectByPrimaryKey(orderId);
        OrderDefer orderDefer = orderDeferService.findLastValidByOrderId(orderId);
        String errMsg = orderDeferService.yeepayRepayQuery(orderDefer.getPayNo(), order.getMerchant());
        if (errMsg == null) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M2000, orderId);
        } else {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M4000, errMsg);
        }
    }
}
