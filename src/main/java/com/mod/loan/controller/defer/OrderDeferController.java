package com.mod.loan.controller.defer;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.DesUtil;
import com.mod.loan.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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
        // 续期天数: 如果配置了有效的续期天数 使用配置的续期天数; 否则使用默认的续期天数
        if (null != merchantDeferConfig.getDeferDay() && merchantDeferConfig.getDeferDay() > 0) {
            deferDay = merchantDeferConfig.getDeferDay();
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
        // 计算逾期费
        Integer overdueDay = null == order.getOverdueDay() ? 0 : order.getOverdueDay();// 逾期天数
        Double overdueFee = null == order.getOverdueFee() ? 0.0D : order.getOverdueFee().doubleValue();// 逾期费
        // 计算还款时间
        String deferRepayDate = TimeUtil.datePlusDays(order.getRepayTime(), deferDay + overdueDay);
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
        } else {
            // 更新续期单子: 可能是用户生成了续期单 一直未支付;后面要重新按照新的费率进行计算
            orderDeferOld.setDeferDay(deferDay);
            orderDeferOld.setDailyDeferFee(dailyDeferFee);
            orderDeferOld.setDeferFee(deferFee);
            orderDeferOld.setRepayDate(TimeUtil.dateFormat(order.getRepayTime()));
            orderDeferOld.setDeferRepayDate(deferRepayDate);
            orderDeferOld.setOverdueDay(overdueDay);
            orderDeferOld.setOverdueFee(overdueFee);
            orderDeferOld.setDeferTotalFee(deferTotalFee);
            orderDeferService.updateByPrimaryKeySelective(orderDeferOld);
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
        if (StringUtils.isEmpty(errMsg)) {
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
        String callbackErr = null;
        try {
            callbackErr = yeepayService.repayCallbackMultiAcct(DesUtil.decryption(merchant.getYeepay_repay_private_key()), responseMsg, repayNo);
        } catch (Exception e) {
            logger.error("易宝异步通知:异常uid={}, e={}",param, e);
        }
        logger.info("展期易宝异步通知:param={},callbackErr={},repayNo={}", param, callbackErr, repayNo);

        //设置OrderRepay
        OrderDefer orderDefer = orderDeferService.selectByPayNo(repayNo.toString());
        //对应的订单不存在 或者 可能已经线下还款
        if (orderDefer == null || orderDefer.getPayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.info("展期易宝异步通知:param={}订单已还款", param);
            return "SUCCESS"; //收到通知 固定格式
        }

        if (StringUtils.isEmpty(callbackErr)) {
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
        } else {
            orderDefer.setRemark(callbackErr);
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
        if (StringUtils.isEmpty(errMsg)) {
            orderDefer.setRemark("展期易宝交易成功");
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M2000, orderId);
        } else if ("PROCESSING".equals(errMsg)) {
            logger.info("---------yeepay query repayno= {}展期订单处理中-----------------", orderDefer.getPayNo());
            return new ResultMessage(ResponseEnum.M4000, "订单处理中,请等待结果");
        } else {
            orderDefer.setRemark(errMsg);
            orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
            orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
            return new ResultMessage(ResponseEnum.M4000, errMsg);
        }
    }

    @RequestMapping(value = "user_defer_detail")
    public String user_defer_detail(Long uid) {
        try {
            return orderDeferService.userDeferDetail(uid).toJSONString();
        } catch (Exception e) {
            logger.error("user_defer_detail error", e);
        }
        return "";
    }

    /**
     * 畅捷续期时协议支付还款发送验证码
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "changjieDeferRepay4SendMsg")
    public ResultMessage changjieDeferRepay4SendMsg(@RequestParam("orderId") Long orderId) {
        logger.info("#[畅捷续期时协议支付还款发送验证码]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderDeferService.changjieDeferRepay4SendMsg(orderId);
        if (null == result) {
            logger.error("#[畅捷续期时协议支付还款发送验证码]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[畅捷续期时协议支付还款发送验证码]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 畅捷续期时协议支付还款确认
     *
     * @param seriesNo 协议支付的流水号
     * @param smsCode  短信验证码
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "changjieDeferRepay4Confirm")
    public ResultMessage changjieDeferRepay4Confirm(String seriesNo, String smsCode) {
        logger.info("#[畅捷续期时协议支付还款确认]-[开始]-seriesNo={},smsCode={}", seriesNo, smsCode);
        if (StringUtils.isEmpty(seriesNo) || StringUtils.isEmpty(smsCode)) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderDeferService.changjieDeferRepay4Confirm(seriesNo, smsCode);
        if (null == result) {
            logger.error("#[畅捷续期时协议支付还款确认]-[异常]");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "展期失败");
        } else if ("DOING".equals(result)) {
            logger.error("#[畅捷续期时协议支付还款确认]-[还款处理中]");
            return new ResultMessage(ResponseEnum.M4000.getCode(), "展期处理中");
        }
        logger.info("#[畅捷续期时协议支付还款确认]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 畅捷续期时协议支付还款结果查询
     *
     * @param repayNo 还款流水号
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "changjieDeferRepay4Query")
    public ResultMessage changjieDeferRepay4Query(@RequestParam("repayNo") String repayNo) {
        logger.info("#[畅捷续期时协议支付还款结果查询]-[开始]-request={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderDeferService.changjieDeferRepay4Query(repayNo);
        if (null == result) {
            logger.error("#[畅捷续期时协议支付还款结果查询]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[畅捷续期时协议支付还款结果查询]-[结束]");
        return new ResultMessage(ResponseEnum.M2000);
    }

    /**
     * 畅捷续期时协议支付还款异步回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "changjie_deferRepay_callback")
    public String changjieDeferRepayCallback(HttpServletRequest request, HttpServletResponse response) {
        logger.info("#[畅捷续期时协议支付还款异步回调]-[开始]");
        try {
            Map<String, String> map = new HashMap();
            //异步回调-业务参数(post--request.getParameterMap()拿不到)
            map.put("notify_id", request.getParameter("notify_id"));
            map.put("notify_type", request.getParameter("notify_type"));
            map.put("notify_time", request.getParameter("notify_time"));
            map.put("_input_charset", request.getParameter("_input_charset"));
            map.put("version", request.getParameter("version"));
            map.put("outer_trade_no", request.getParameter("outer_trade_no"));
            map.put("inner_trade_no", request.getParameter("inner_trade_no"));
            map.put("trade_amount", request.getParameter("trade_amount"));
            map.put("trade_status", request.getParameter("trade_status"));
            map.put("gmt_create", request.getParameter("gmt_create"));
            map.put("gmt_payment", request.getParameter("gmt_payment"));
            map.put("gmt_close", request.getParameter("gmt_close"));
            if (!org.springframework.util.StringUtils.isEmpty(request.getParameter("extension"))) {
                map.put("extension", request.getParameter("extension"));
            }
            String sign = request.getParameter("sign");
            orderDeferService.changjieDeferRepayCallback(map, sign);
            logger.info("#[畅捷续期时协议支付还款异步回调]-[结束]");
        } catch (Exception e) {
            logger.error("#[畅捷续期时协议支付还款异步回调]-[异常]-e={}", e);
        }
        return "success";
    }

    /**
     * 快钱续期时支付还款
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "kuaiqianDeferRepay")
    public ResultMessage kuaiqianDeferRepay(@RequestParam("orderId") Long orderId) {
        logger.info("#[快钱续期时支付还款]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderDeferService.kuaiqianDeferRepay(orderId);
        if (null == result) {
            logger.error("#[快钱续期时支付还款]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[快钱续期时支付还款]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 快钱续期时支付还款结果查询
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "kuaiqianDeferRepayQuery")
    public ResultMessage kuaiqianDeferRepayQuery(@RequestParam("orderId") Long orderId) {
        logger.info("#[快钱续期时支付还款结果查询]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderDeferService.kuaiqianDeferRepayQuery(orderId);
        if (null == result) {
            logger.error("#[快钱续期时支付还款结果查询]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[快钱续期时支付还款结果查询]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

}
