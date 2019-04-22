package com.mod.loan.controller.order;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import com.mod.loan.util.CheckUtils;
import com.mod.loan.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;

@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class YeepayRepayController {

	private static Logger logger = LoggerFactory.getLogger(YeepayRepayController.class);
	private final OrderService orderService;
	private final OrderRepayService orderRepayService;
	private final YeepayService yeepayService;
    private final RedisMapper redisMapper;
    private final MerchantService merchantService;
    private final UserService userService;

    @Autowired
    public YeepayRepayController(OrderService orderService, OrderRepayService orderRepayService, YeepayService yeepayService, RedisMapper redisMapper, MerchantService merchantService, UserService userService) {
        this.orderService = orderService;
        this.orderRepayService = orderRepayService;
        this.yeepayService = yeepayService;
        this.redisMapper = redisMapper;
        this.merchantService = merchantService;
        this.userService = userService;
    }

    @LoginRequired
	@RequestMapping(value = "yeepay_repay_text")
	public ResultMessage yeepay_repay_text(String orderId,String cardNo, String tel) {
        if (!CheckUtils.isMobiPhoneNum(tel)) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号码错误");
        }

        if (orderRepayService.countRepaySuccess(NumberUtils.toLong(orderId)) >= 1) {
            logger.error("orderId={}已存在还款中的记录", NumberUtils.toLong(orderId));
            return new ResultMessage(ResponseEnum.M4000.getCode(), "请勿重复还款");
        }

        Long uid = RequestThread.getUid();

        Order order = orderService.selectByPrimaryKey(NumberUtils.toLong(orderId));
        if (order.getStatus() == 31 || order.getStatus() == 33 || order.getStatus() == 34) { // 已放款，逾期，坏账状态
            try {
                String repayNo = StringUtil.getOrderNumber("r");// 支付流水号
                String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();
                String alias = RequestThread.getClientAlias();
                Merchant merchant = merchantService.findMerchantByAlias(alias);

                String err = yeepayService.payRequest(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(), repayNo, String.valueOf(uid), cardNo, amount);
                if(err!=null){
                    return new ResultMessage(ResponseEnum.M4000, err);
                }
                redisMapper.set(RedisConst.repay_text + repayNo, orderId, Constant.SMS_EXPIRATION_TIME);
                return new ResultMessage(ResponseEnum.M2000);
            } catch (Exception e) {
                logger.error("易宝支付短信受理异常，error={}", e.getMessage());
                return new ResultMessage(ResponseEnum.M4000);
            }
        }
        return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
	}

	@LoginRequired
	@RequestMapping(value = "yeepay_repay_active")
	public ResultMessage yeepay_repay_active(String repayNo, String validateCode, String cardNo, String cardName) {
        Long uid = RequestThread.getUid();

        if (validateCode.length() > 6) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码长度过长");
        }

        long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text + repayNo));
        if(orderId==0) {
            logger.error("订单异常，uid={},订单号={}", uid, repayNo);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
        }

        Order order = orderService.selectByPrimaryKey(orderId);
        if (!order.getUid().equals(uid)) {
            logger.error("订单异常，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
        }

        if (order.getStatus() != 31 && order.getStatus() != 33 && order.getStatus() != 34) { // 已放款，逾期，坏账状态
            logger.error("订单非还款状态，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
        }

        // 还款记录表
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
        if(orderRepay == null){
            //暂不支持部分还款
            String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();
            orderRepay = new OrderRepay();
            orderRepay.setRepayNo(repayNo);
            orderRepay.setUid(order.getUid());
            orderRepay.setOrderId(orderId);
            orderRepay.setRepayType(1);
            orderRepay.setRepayMoney(new BigDecimal(amount));
            orderRepay.setBank(cardName);
            orderRepay.setBankNo(cardNo);
            orderRepay.setCreateTime(new Date());
            orderRepay.setUpdateTime(new Date());
            orderRepay.setRepayStatus(0);//初始状态
            orderRepayService.insertSelective(orderRepay);
        }

        try {
            OrderRepay orderRepayUpd = new OrderRepay();
            orderRepayUpd.setRepayNo(repayNo);

            String alias = RequestThread.getClientAlias();
            Merchant merchant = merchantService.findMerchantByAlias(alias);
            String err = yeepayService.payConfirm(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(), repayNo, validateCode);
            if (err!=null) {
                orderRepayUpd.setRepayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
                orderRepayUpd.setRemark("易宝支付失败:" + err);
                orderRepayService.updateByPrimaryKeySelective(orderRepayUpd);
                return new ResultMessage(ResponseEnum.M4000.getCode(), err);
            }

            orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
            orderRepay.setRemark("易宝支付成功");
            orderRepayService.updateByPrimaryKeySelective(orderRepay);
            return new ResultMessage(ResponseEnum.M2000, order.getId());// 成功返回订单号，便于查看详情
        } catch (Exception e) {
            logger.error("绑卡支付异常，result={}", e.getMessage());
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    @RequestMapping(value = "repay_callback")
    public String repay_callback(HttpServletRequest request, HttpServletResponse response){
        String responseMsg = request.getParameter("response");
        String param = request.getParameter("param");

        if (StringUtils.isEmpty(responseMsg) || StringUtils.isEmpty(param)){
            logger.error("responseMsg={},param={}",responseMsg,param);
            logger.error("易宝异步通知:返回为空");
            return "SUCCESS";
        }

        Long uid = Long.valueOf(param);
        User user = userService.selectByPrimaryKey(uid);
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());

        StringBuffer repayNo = new StringBuffer();
        String callbackErr = yeepayService.repayCallbackMultiAcct(merchant.getYeepay_repay_private_key(), responseMsg, repayNo);

        //设置OrderRepay
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo.toString());
        //对应的订单不存在 或者 可能已经线下还款
        if (orderRepay==null||orderRepay.getRepayStatus().equals(OrderRepayStatusEnum.REPAY_SUCCESS.getCode())) {
            logger.error("易宝异步通知:订单已还款");
            return "SUCCESS"; //收到通知 固定格式
        }
        if (callbackErr==null){
            orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
        }else {
            logger.error("易宝异步通知:订单错误信息{}",callbackErr);
            orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
        }
        orderRepay.setUpdateTime(new Date());

        //设置Order
        Order order = new Order();
        order.setId(orderRepay.getOrderId());
        order.setRealRepayTime(new Date());
        order.setHadRepay(orderRepay.getRepayMoney());
        if (33 == order.getStatus() || 34 == order.getStatus()) {
            order.setStatus(42);
        } else {
            order.setStatus(41);
        }
        orderRepayService.updateOrderRepayInfo(orderRepay, order);
        return "SUCCESS"; //收到通知 固定格式
    }


}
