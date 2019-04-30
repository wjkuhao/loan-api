package com.mod.loan.controller.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.util.MoneyUtil;
import com.mod.loan.util.StringUtil;

/**
 * 订单申请详细展示，及下单确认 回收和贷款文案通用
 *
 * @author yhx 2018年4月26日 下午13:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class OrderApplyController {

	@Autowired
	private UserIdentService userIdentService;
	@Autowired
	private UserBankService userBankService;
	@Autowired
	private MerchantRateService merchantRateService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private BlacklistService blacklistService;
	@Autowired
	private RedisMapper redisMapper;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private UserService userService;

	/**
	 * h5 借款确认 获取费用明细
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "order_confirm")
	public ResultMessage order_confirm() {
		Map<String, Object> map = new HashMap<String, Object>();
		Long uid = RequestThread.getUid();
		UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
		if (null == userBank) {
			return new ResultMessage(ResponseEnum.M2000, "未查到银行卡信息");
		}
		Integer borrowType = orderService.countPaySuccessByUid(uid);
		MerchantRate merchantRate = merchantRateService.findByMerchantAndBorrowType(RequestThread.getClientAlias(),
				borrowType);
		if (null == merchantRate) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "未查到产品信息");
		}
		BigDecimal totalFee = MoneyUtil.totalFee(merchantRate.getProductMoney(), merchantRate.getTotalRate());// 综合费用
		BigDecimal actualMoney = MoneyUtil.actualMoney(merchantRate.getProductMoney(), totalFee);// 实际到账
		map.put("productId", merchantRate.getId());
		map.put("productMoney", merchantRate.getProductMoney());
		map.put("productDay", merchantRate.getProductDay());
		map.put("totalFee", totalFee);
		map.put("actualMoney", actualMoney);

		map.put("cardName", userBank.getCardName());
		map.put("cardNo", StringUtil.bankTailNo(userBank.getCardNo()));

		return new ResultMessage(ResponseEnum.M2000, map);
	}

	/**
	 * h5 借款确认 提交订单
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "order_submit")
	public ResultMessage order_submit(@RequestParam(required = true) Long productId,
			@RequestParam(required = true) Integer productDay, @RequestParam(required = true) BigDecimal productMoney,
			@RequestParam(required = false) String phoneType, @RequestParam(required = false) String paramValue,
			@RequestParam(required = false) String phoneModel, @RequestParam(required = false) Integer phoneMemory) {
		Long uid = RequestThread.getUid();
		if (!redisMapper.lock(RedisConst.lock_user_order + uid, 5)) {
			return new ResultMessage(ResponseEnum.M4005);
		}
		UserIdent userIdent = userIdentService.selectByPrimaryKey(uid);
		if (2 != userIdent.getRealName() || 2 != userIdent.getUserDetails() || 2 != userIdent.getBindbank()
				|| 2 != userIdent.getMobile() || 2 != userIdent.getLiveness() || 2 != userIdent.getAlipay()
				|| 2 != userIdent.getTaobao()) {
			// 提示认证未完成
			return new ResultMessage(ResponseEnum.M4000.getCode(), "认证未完成");
		}

		User user = userService.selectByPrimaryKey(uid);
		Blacklist blacklist = blacklistService.getByPhone(user.getUserPhone());
		if (null != blacklist) {
			// 校验灰名单锁定天数
			if (1 == blacklist.getType()) {
				DateTime d1 = new DateTime(new Date());
				DateTime d2 = new DateTime(blacklist.getInvalidTime());
				Integer remainDays = Days.daysBetween(d1, d2).getDays() + 1;
				return new ResultMessage(ResponseEnum.M4000.getCode(), "暂时无法下单，请于" + remainDays + "天后再尝试");
			}
			// 黑名单
			if (2 == blacklist.getType()) {
				return new ResultMessage(ResponseEnum.M4000.getCode(), "您不符合下单条件");
			}
		}
		// 是否有正在借款中的订单
		Order orderIng = orderService.findUserLatestOrder(uid);
		if (null != orderIng) {
			if (orderIng.getStatus() < 40) {
				return new ResultMessage(ResponseEnum.M4000.getCode(), "订单进行中，无法提单");
			}
			// 审核拒绝的订单7天内无法再下单
			if (orderIng.getStatus() == 51 || orderIng.getStatus() == 52) {
				DateTime applyTime = new DateTime(orderIng.getCreateTime()).plusDays(7);
				DateTime nowTime = new DateTime();
				Integer remainDays = Days.daysBetween(nowTime.withMillisOfDay(0), applyTime.withMillisOfDay(0))
						.getDays();
				if (0 < remainDays && remainDays <= 7) {
					return new ResultMessage(ResponseEnum.M4000.getCode(), "请" + remainDays + "天后重试提单");
				}
			}
		}
		Order order = new Order();
		MerchantRate merchantRate = merchantRateService.selectByPrimaryKey(productId);
		if (null == merchantRate) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "未查到规则");
		}
		if (merchantRate.getProductMoney().compareTo(productMoney) != 0 || merchantRate.getProductDay() != productDay) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "规则不匹配");
		}
		BigDecimal totalFee = MoneyUtil.totalFee(merchantRate.getProductMoney(), merchantRate.getTotalRate());// 综合费用
		BigDecimal interestFee = MoneyUtil.interestFee(merchantRate.getProductMoney(), merchantRate.getProductDay(),
				merchantRate.getProductRate());// 利息
		BigDecimal actualMoney = MoneyUtil.actualMoney(merchantRate.getProductMoney(), totalFee);// 实际到账
		BigDecimal shouldRepay = MoneyUtil.shouldrepay(merchantRate.getProductMoney(), interestFee, new BigDecimal(0),
				new BigDecimal(0));// 应还金额
		// 判断客群
		Integer userType = orderService.judgeUserTypeByUid(uid);
		order.setOrderNo(StringUtil.getOrderNumber("b"));
		order.setUid(uid);
		order.setBorrowDay(merchantRate.getProductDay());
		order.setBorrowMoney(merchantRate.getProductMoney());
		order.setActualMoney(actualMoney);
		order.setTotalRate(merchantRate.getTotalRate());
		order.setTotalFee(totalFee);
		order.setInterestRate(merchantRate.getProductRate());
		order.setOverdueDay(0);
		order.setOverdueFee(new BigDecimal(0));
		order.setOverdueRate(merchantRate.getOverdueRate());
		order.setInterestFee(interestFee);
		order.setShouldRepay(shouldRepay);
		order.setHadRepay(new BigDecimal(0));
		order.setReduceMoney(new BigDecimal(0));
		order.setStatus(11);
		order.setCreateTime(new Date());
		order.setMerchant(RequestThread.getClientAlias());
		order.setProductId(productId);
		order.setUserType(userType);
		OrderPhone orderPhone = new OrderPhone();
		orderPhone.setParamValue(paramValue);
		orderPhone.setPhoneModel(phoneModel + "|" + phoneMemory);
		orderPhone.setPhoneType(phoneType);

		// 老客户不走风控，直接进入放款列表
		Integer borrowType = orderService.countPaySuccessByUid(uid);
		if(borrowType!=null && borrowType>0){
			order.setAuditTime(new Date());
			order.setStatus(OrderEnum.DAI_FANGKUAN.getCode());
			orderService.addOrder(order, orderPhone);
			return new ResultMessage(ResponseEnum.M2000);
		}

		orderService.addOrder(order, orderPhone);
		// 发送消息，等待请求风控
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("orderId", order.getId());
		jsonObject.put("merchant", order.getMerchant());
		rabbitTemplate.convertAndSend("queue_risk_order_notify", jsonObject);

		return new ResultMessage(ResponseEnum.M2000);
	}

}
