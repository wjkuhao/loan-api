package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 订单还款确认，回收和贷款文案通用
 *
 * @author yhx 2018年5月3日 上午9:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class HelipayRepayController {

	private static Logger logger = LoggerFactory.getLogger(HelipayRepayController.class);
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

	/**
	 * h5 借款详情 线上主动还款 绑卡支付短信
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "repay_text")
	public ResultMessage repay_text(@RequestParam(required = true) String orderId) {
		ResultMessage message = null;
		Long uid = RequestThread.getUid();
		User user = userService.selectByPrimaryKey(uid);
		UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
		Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
		Order order = orderService.selectByPrimaryKey(NumberUtils.toLong(orderId));
		if (order.getStatus() == 31 || order.getStatus() == 33 || order.getStatus() == 34) { // 已放款，逾期，坏账状态
			String repayNo = StringUtil.getOrderNumber("r");// 支付流水号
			String amount = "dev".equals(Constant.ENVIROMENT)? "0.11":order.getShouldRepay().toString();
			BindPayValidateCodeVo requestVo = new BindPayValidateCodeVo();
			requestVo.setP1_bizType("QuickPayBindPayValidateCode");
			requestVo.setP2_customerNumber(merchant.getHlb_id());
			requestVo.setP3_bindId(userBank.getForeignId());
			requestVo.setP4_userId(user.getId().toString());
			requestVo.setP5_orderId(repayNo);
			requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
			requestVo.setP7_currency("CNY");
			requestVo.setP8_orderAmount(amount);
			requestVo.setP9_phone(userBank.getCardPhone());
			requestVo.setSignatureType("MD5WITHRSA");
			try {
				String pfxPath = helipay_path + merchant.getMerchantAlias()+".pfx";
				Map handleMap = MessageHandle.getReqestMap(requestVo,pfxPath,helipay_pfx_pwd);
				String response = HttpClientService.getHttpResp(handleMap, helipay_url);
				BindPayValidateCodeResponseVo responseVo = JSONObject.parseObject(response, BindPayValidateCodeResponseVo.class);
				if ("0000".equals(responseVo.getRt2_retCode())) {
					redisMapper.set(RedisConst.repay_text + repayNo, orderId, 300);
					return new ResultMessage(ResponseEnum.M2000, repayNo);
				}else{
					logger.info("绑卡支付短信受理失败，失败订单号为：{}，失败原因为：{}", order.getId(),response);
					message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付短信受理失败");
				}

			} catch (Exception e) {
				logger.info("绑卡支付短信受理异常，订单号为：{}", order.getId());
				message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付短信受理失败");
			}
			return  message;
		}
		logger.info("订单非还款状态，订单号为：{}", order.getId());
		return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
	}

	/**
	 * h5 回购详情
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "repay_info")
	public ResultMessage repay_info(@RequestParam(required = true) String repayNo) {
		Long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text  + repayNo));
		if(orderId==0) {
			return new ResultMessage(ResponseEnum.M4000.getCode(),"请重新获取验证码");
		}
		Order order = orderService.selectByPrimaryKey(orderId);
		Map<String, String> data = new HashMap<String, String>();
		data.put("repayMoney", order.getShouldRepay().toString());
		return new ResultMessage(ResponseEnum.M2000, data);
	}

	/**
	 * 绑卡支付
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "repay_active")
	public ResultMessage repay_active(@RequestParam(required = true) String repayNo,
			@RequestParam(required = true) String validateCode) {
		ResultMessage message = null;
		Long uid = RequestThread.getUid();
		if (validateCode.length() > 6) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码长度过长");
		}
		Long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.repay_text  + repayNo));
		if(orderId==0) {
			logger.info("订单异常，uid={},订单号={}", uid,repayNo);
			return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
		}
		Order order = orderService.selectByPrimaryKey(orderId);
		if (!order.getUid().equals(uid)) {
			logger.info("订单异常，订单号为：{}", order.getId());
			return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
		}
		if (order.getStatus() != 31 && order.getStatus() != 33 && order.getStatus() != 34) { // 已放款，逾期，坏账状态
			logger.info("订单非还款状态，订单号为：{}", order.getId());
			return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
		}
		User user = userService.selectByPrimaryKey(uid);
		UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
		Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
		String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();

		String ip = RequestThread.getIp();
		BindCardPayVo requestVo = new BindCardPayVo();
		requestVo.setP1_bizType("QuickPayBindPay");
		requestVo.setP2_customerNumber(merchant.getHlb_id());
		requestVo.setP3_bindId(userBank.getForeignId());
		requestVo.setP4_userId(user.getId().toString());
	    requestVo.setP5_orderId(repayNo);
	    requestVo.setP6_timestamp(new DateTime().toString(TimeUtils.dateformat5));
	    requestVo.setP7_currency("CNY");
	    requestVo.setP8_orderAmount(amount);
	    requestVo.setP9_goodsName("回收手机");
	    requestVo.setP10_goodsDesc("回收手机");
	    requestVo.setP11_terminalType("OTHER");
	    requestVo.setP12_terminalId(UUID.randomUUID().toString());
	    requestVo.setP13_orderIp(ip);
	    requestVo.setP14_period("");
	    requestVo.setP15_periodUnit("");
	    requestVo.setP16_serverCallbackUrl(Constant.SERVER_API_URL + "order/repay_result");
	    requestVo.setP17_validateCode(validateCode);
	    requestVo.setSignatureType("MD5WITHRSA");

		// 还款记录表
		OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
		if(orderRepay == null){
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
		String response = "";
		try {
			String pfxPath = helipay_path + merchant.getMerchantAlias() + ".pfx";
			Map handleMap = MessageHandle.getReqestMap(requestVo,pfxPath,helipay_pfx_pwd);
			response  = HttpClientService.getHttpResp(handleMap, helipay_url);
			BindCardPayResponseVo responseVo = JSONObject.parseObject(response, BindCardPayResponseVo.class);
			if (!"0000".equals(responseVo.getRt2_retCode())) {
				logger.error("绑卡支付受理失败，result={}", response);
				OrderRepay orderRepay1 = new OrderRepay();
				orderRepay1.setRepayNo(repayNo);
				orderRepay1.setRepayStatus(2);
				String responseMsg = responseVo.getRt3_retMsg();
				if(StringUtils.isNotBlank(responseMsg) && responseMsg.length() > 30){
					responseMsg = responseMsg.substring(0,30);
				}
				orderRepay1.setRemark(responseMsg);
				orderRepayService.updateByPrimaryKeySelective(orderRepay1);
				return new ResultMessage(ResponseEnum.M4000.getCode(), responseVo.getRt3_retMsg());
			}
			if ("SUCCESS".equalsIgnoreCase(responseVo.getRt9_orderStatus())) {
				return new ResultMessage(ResponseEnum.M2000, order.getId());// 成功返回订单号，便于查看详情
			}
			if ("DOING".equalsIgnoreCase(responseVo.getRt9_orderStatus())) {
				logger.info("绑卡支付受理中，result={}", response);
				return new ResultMessage(ResponseEnum.M2000.getCode(), order.getId());// 处理中回订单号，便于查看详情
			}
			logger.info("绑卡支付状态异常，params={}，result={}", JSON.toJSONString(requestVo),response);
			message = new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡支付失败，请重试！");
		} catch (Exception e) {
			logger.info("绑卡支付异常，params={}，result={}", JSON.toJSONString(requestVo),response);
			message = new ResultMessage(ResponseEnum.M4000);
		}
		return message;
	}

	/**
	 * 异步通知
	 */
	@LoginRequired(check = false)
	@RequestMapping(value = "repay_result")
	public String repay_result(@RequestParam(required = true) String rt2_retCode,
							   @RequestParam(required = true) String rt9_orderStatus, @RequestParam(required = true) String rt5_orderId) {
		// 只处理受理成功并且支付成功的订单
		if ("0000".equals(rt2_retCode) && "SUCCESS".equals(rt9_orderStatus)) {
			OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(rt5_orderId);
			if (orderRepay.getRepayStatus() == 3) {
				return "success";
			}
			Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
			if (41 == order.getStatus() || 42 == order.getStatus()) {
				logger.info("异步通知:订单{}已还款：", order.getId());
				return "success";
			}
			Order order1 = new Order();
			order1.setId(orderRepay.getOrderId());
			order1.setRealRepayTime(new Date());
			order1.setHadRepay(order.getShouldRepay());
			if (33 == order.getStatus() || 34 == order.getStatus()) {
				order1.setStatus(42);
			} else {
				order1.setStatus(41);
			}
			OrderRepay orderRepay1 = new OrderRepay();
			orderRepay1.setRepayNo(rt5_orderId);
			orderRepay1.setUpdateTime(new Date());
			orderRepay1.setRepayStatus(3);
			orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
			return "success";
		}else {
			logger.info("异步通知异常：rt2_retCode={},rt9_orderStatus={},rt5_orderId={}", rt2_retCode, rt9_orderStatus,rt5_orderId);
		}
		return "success";
	}


}
