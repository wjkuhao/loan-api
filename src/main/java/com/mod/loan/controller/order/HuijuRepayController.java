package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderEnum;
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
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderRepayService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.CheckUtils;
import com.mod.loan.util.HttpUtils;
import com.mod.loan.util.MD5;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.huiju.CreateLinkStringByGet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单还款确认，回收和贷款文案通用
 *
 * @author yhx 2018年5月3日 上午9:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class HuijuRepayController {

	private static Logger logger = LoggerFactory.getLogger(HuijuRepayController.class);
	@Autowired
	private UserService userService;
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
	@RequestMapping(value = "huiju_repay_text")
	public ResultMessage huiju_repay_text(@RequestParam(required = true) String orderId,@RequestParam(required = true)String cardNo,
										  @RequestParam(required = true)String tel) {
		if (!CheckUtils.isMobiPhoneNum(tel)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号码错误");
		}

        if (orderRepayService.countRepaySuccess(NumberUtils.toLong(orderId)) >= 1) {
            logger.error("orderId={}已存在还款中的记录", NumberUtils.toLong(orderId));
            return new ResultMessage(ResponseEnum.M4000.getCode(), "请勿重复还款");
        }

		ResultMessage message = null;
		Long uid = RequestThread.getUid();
		User user = userService.selectByPrimaryKey(uid);
		Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
		Order order = orderService.selectByPrimaryKey(NumberUtils.toLong(orderId));
		if (order.getStatus()>= OrderEnum.REPAYING.getCode() && order.getStatus()< OrderEnum.NORMAL_REPAY.getCode()) { // 还款中30～40
			String repayNo = StringUtil.getOrderNumber("r");// 支付流水号
			String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();
			String response = "";
            Map<String, String> map = new HashMap<String, String>();
            try {
				map.put("p0_Version", "2.0");
				map.put("p1_MerchantNo", merchant.getHuiju_id());
				map.put("p2_MerchantName", "汇聚");
				map.put("q1_OrderNo", repayNo);
				map.put("q2_Amount", amount);
				map.put("q3_Cur", "1");
				map.put("q4_ProductName", "个人消费");
				map.put("q7_NotifyUrl", Constant.HUIJU_NOTIFY_URL);
				map.put("q8_FrpCode", "FAST");
				map.put("s1_PayerName", user.getUserName());
				map.put("s2_PayerCardType", "1");
				map.put("s3_PayerCardNo", user.getUserCertNo());
				map.put("s4_PayerBankCardNo", cardNo);
				map.put("s7_BankMobile", tel);

				String unsign = CreateLinkStringByGet.createLinkStringByGet(map);
				String sign =  MD5.toMD5(unsign+merchant.getHuiju_md5_key(),"utf-8").toUpperCase();
				map.put("hmac", sign);
				response = HttpUtils.doPost(Constant.HUIJU_SMS_URL,map);

				Map result = JSONObject.parseObject(response);
				String returnHmac = (String) result.remove("hmac");
				String Strmap = CreateLinkStringByGet.createLinkStringByGet(result);
				if (!MD5.toMD5(Strmap+merchant.getHuiju_md5_key(),"utf-8").toUpperCase().equals(returnHmac)) {
					logger.error("汇聚支付短信验签失败，失败订单号为：{}，失败原因为：{}", order.getId(), response);
					return  new ResultMessage(ResponseEnum.M4000.getCode(), "短信发送失败，请稍后重试");
				}
				if(!"100".equals(result.get("ra_Status").toString())){
                    logger.info("汇聚支付短信受理失败，订单号为：{}，失败原因为：{}", order.getId(),result.get("rb_Msg").toString());
					return new ResultMessage(ResponseEnum.M4000, result.get("rb_Msg").toString());
				}
				if("100".equals(result.get("ra_Status").toString())){
					redisMapper.set(RedisConst.huiju_repay_text + repayNo, orderId, 300);
					redisMapper.set(RedisConst.huiju_repay_info + repayNo, cardNo+"|"+tel, 300);
					return new ResultMessage(ResponseEnum.M2000, repayNo);
				}
			} catch (Exception e) {
				logger.error("汇聚支付短信受理异常，params={}，result={}", JSON.toJSONString(map),response);
				message = new ResultMessage(ResponseEnum.M4000);
			}
			return  message;
		}
		logger.error("汇聚支付，订单非还款状态，订单号为：{}", order.getId());
		return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
	}

	/**
	 * 绑卡支付
	 */
	@LoginRequired(check = true)
	@RequestMapping(value = "huiju_repay_active")
	public ResultMessage huiju_repay_active(@RequestParam(required = true) String repayNo,@RequestParam(required = true) String validateCode
			,@RequestParam(required = true) String cardNo,@RequestParam(required = true) String cardName) {
		ResultMessage message = null;
		Long uid = RequestThread.getUid();
		if (validateCode.length() > 6) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码长度过长");
		}
		Long orderId = NumberUtils.toLong(redisMapper.get(RedisConst.huiju_repay_text  + repayNo));
		if(orderId==0) {
			logger.error("订单异常，uid={},订单号={}", uid,repayNo);
			return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
		}
		Order order = orderService.selectByPrimaryKey(orderId);
		if (!order.getUid().equals(uid)) {
			logger.error("订单异常，订单号为：{}", order.getId());
			return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
		}
		if (order.getStatus()>=OrderEnum.NORMAL_REPAY.getCode() || order.getStatus()<OrderEnum.REPAYING.getCode()) { //非还款中状态
			logger.error("订单非还款状态，订单号为：{}", order.getId());
			return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
		}
		User user = userService.selectByPrimaryKey(uid);
		Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
		String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();
		String repayInfo = redisMapper.get(RedisConst.huiju_repay_info + repayNo);
		if(null == repayInfo){
			return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码");
		}
		String[] arr = repayInfo.split("\\|");
		if(!cardNo.equals(arr[0])){
			logger.error("银行卡号不匹配，应为：{}，实为：{}", arr[0],cardNo);
			return new ResultMessage(ResponseEnum.M4000.getCode(), "请与客服人员反馈");
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("p0_Version", "2.0");
		map.put("p1_MerchantNo", merchant.getHuiju_id());
		map.put("p2_MerchantName", "汇聚");
		map.put("q1_OrderNo", repayNo);
		map.put("q2_Amount", amount);
		map.put("q3_Cur", "1");
		map.put("q4_ProductName", "个人消费");
		map.put("q7_NotifyUrl", Constant.HUIJU_NOTIFY_URL);
		map.put("q8_FrpCode", "FAST");
		map.put("s1_PayerName", user.getUserName());
		map.put("s2_PayerCardType", "1");
		map.put("s3_PayerCardNo", user.getUserCertNo());
		map.put("s4_PayerBankCardNo",arr[0] );
		map.put("s7_BankMobile", arr[1]);
		map.put("t2_SmsCode", validateCode);

		String unsign = CreateLinkStringByGet.createLinkStringByGet(map);
		String sign = MD5.toMD5(unsign+merchant.getHuiju_md5_key(),"utf-8").toUpperCase();
		map.put("hmac", sign);
		// 还款记录表
		OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
		if(orderRepay == null){
			orderRepay = new OrderRepay();
			orderRepay.setRepayNo(repayNo);
			orderRepay.setUid(order.getUid());
			orderRepay.setOrderId(orderId);
			orderRepay.setRepayType(6);
			orderRepay.setRepayMoney(new BigDecimal(amount));
			orderRepay.setBank(cardName);
			orderRepay.setBankNo(arr[0]);
			orderRepay.setCreateTime(new Date());
			orderRepay.setUpdateTime(new Date());
			orderRepay.setRepayStatus(0);
			orderRepayService.insertSelective(orderRepay);
		}
		String response = "";
		try {
			response = HttpUtils.doPost(Constant.HUIJU_PAY_URL, map);
			Map result = JSONObject.parseObject(response);
			String returnHmac = (String) result.remove("hmac");
			String Strmap = CreateLinkStringByGet.createLinkStringByGet(result);
			if (!MD5.toMD5(Strmap+merchant.getHuiju_md5_key(),"utf-8").toUpperCase().equals(returnHmac)) {
                OrderRepay orderRepay1 = new OrderRepay();
                orderRepay1.setRepayNo(repayNo);
                orderRepay1.setRepayStatus(2);
                orderRepay1.setRemark("汇聚支付验签失败");
                orderRepayService.updateByPrimaryKeySelective(orderRepay1);
				return new ResultMessage(ResponseEnum.M4000.getCode(), "支付失败");
			}
			if ("100".equalsIgnoreCase(result.get("ra_Status").toString())) {//交易成功
                OrderRepay orderRepay1 = new OrderRepay();
                orderRepay1.setRepayNo(repayNo);
                orderRepay1.setRepayStatus(1);
                orderRepay1.setRemark(result.get("rb_Msg").toString());
                orderRepayService.updateByPrimaryKeySelective(orderRepay1);
				return new ResultMessage(ResponseEnum.M2000, order.getId());// 成功返回订单号，便于查看详情
			}
			if ("102".equalsIgnoreCase(result.get("ra_Status").toString())) {//订单已创建
                OrderRepay orderRepay1 = new OrderRepay();
                orderRepay1.setRepayNo(repayNo);
                orderRepay1.setRepayStatus(1);
                orderRepay1.setRemark(result.get("rb_Msg").toString());
                orderRepayService.updateByPrimaryKeySelective(orderRepay1);
				return new ResultMessage(ResponseEnum.M2000.getCode(), order.getId());// 处理中返回订单号，便于查看详情
			}
//			logger.info("汇聚绑卡支付受理失败，result={}", response);
			OrderRepay orderRepay1 = new OrderRepay();
			orderRepay1.setRepayNo(repayNo);
			orderRepay1.setRepayStatus(2);
			String responseMsg = result.get("rb_Msg").toString();
			if(StringUtils.isNotBlank(responseMsg) && responseMsg.length() > 30){
				responseMsg = responseMsg.substring(0,30);
			}
			orderRepay1.setRemark(responseMsg);
			orderRepayService.updateByPrimaryKeySelective(orderRepay1);
			return new ResultMessage(ResponseEnum.M4000.getCode(), responseMsg);
		} catch (Exception e) {
			logger.error("绑卡支付异常，params={}，result={}", JSON.toJSONString(map),response);
			message = new ResultMessage(ResponseEnum.M4000);
		}
		return message;
	}


	/**
	 * 异步通知
	 */
	@LoginRequired(check = false)
	@RequestMapping(value = "huiju_repay_result")
	public String huiju_repay_result(HttpServletRequest req) {
        Map<String,String> map = new HashMap<>();
        Enumeration enu=req.getParameterNames();
        while(enu.hasMoreElements()){
            String paraName=(String)enu.nextElement();
            try {
                map.put(paraName, URLDecoder.decode(req.getParameter(paraName),"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String returnHmac = map.remove("hmac");
        String Strmap = CreateLinkStringByGet.createLinkStringByGet(map);
        Merchant merchant = merchantService.findByHuijuId(map.get("r1_MerchantNo"));
        if (!MD5.toMD5(Strmap+merchant.getHuiju_md5_key(),"utf-8").equals(returnHmac)) {
            logger.error("汇聚回调验签失败，result={},hmac={}", JSON.toJSONString(map),returnHmac);
            return "fail";
        }
		// 只处理支付成功的订单
		if ("100".equals(map.get("r6_Status"))) {
			OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(map.get("r2_OrderNo"));
			if (orderRepay.getRepayStatus() == 3) {
				return "success";
			}
			Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
			if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus())
					|| OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus())
					|| OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
				logger.error("异步通知:订单{}已还款：", order.getId());
				return "success";
			}
			Order order1 = new Order();
			order1.setId(orderRepay.getOrderId());
			order1.setRealRepayTime(new Date());
			order1.setHadRepay(new BigDecimal(map.get("r3_Amount")));
			order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));

			OrderRepay orderRepay1 = new OrderRepay();
			orderRepay1.setRepayNo(map.get("r2_OrderNo"));
			orderRepay1.setUpdateTime(new Date());
			orderRepay1.setRepayStatus(3);
			orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
			return "success";
		}else if("101".equals(map.get("r6_Status"))){
//			logger.error("汇聚异步通知失败：r6_Status={},r2_OrderNo={}", map.get("r6_Status"),map.get("r2_OrderNo"));
			return "success";
		}else{
			logger.error("汇聚异步通知异常：r6_Status={},r2_OrderNo={}", map.get("r6_Status"),map.get("r2_OrderNo"));
		}
		return "fail";
	}


}
