package com.mod.loan.controller.user;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.mod.loan.common.annotation.Api;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.AppFeedbackMapper;
import com.mod.loan.mapper.UserDeviceMapper;
import com.mod.loan.mapper.UserMapper;
import com.mod.loan.model.AppFeedback;
import com.mod.loan.model.User;
import com.mod.loan.model.UserDevice;
import com.mod.loan.service.UserDeductionService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.CheckUtils;
import com.mod.loan.util.RandomUtils;
import com.mod.loan.util.StringReplaceUtil;
import com.mod.loan.util.jwtUtil;
import com.mod.loan.util.sms.EnumSmsTemplate;
import com.mod.loan.util.sms.SmsMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "user")
public class UserController {

	@Autowired
	private DefaultKaptcha defaultKaptcha;
	@Autowired
	private RedisMapper redisMapper;
	@Autowired
	private UserService userService;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private UserDeviceMapper userDeviceMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private AppFeedbackMapper feedbackMapper;

	@Autowired
	UserDeductionService userDeductionService;

	/**
	 * 用户额度与借款周期配置
	 * 
	 * @return
	 */
	@RequestMapping(value = "user_balance")
	@Api
	public ResultMessage user_balance() {
		Map<String, Object> data = new HashMap<>();
		data.put("limit_money", "500|10000");
		data.put("limit_money_allow", "1000");
		data.put("limit_day", "7");
		return new ResultMessage(ResponseEnum.M2000, data);
	}

	@RequestMapping(value = "user_graph_code")
	@Api
	public void user_graph_code(HttpServletRequest request, HttpServletResponse response, String phone)
			throws Exception {
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
		if (!CheckUtils.isMobiPhoneNum(phone)) {
			return;
		}
		// 生产验证码字符串并保存到session中
		String createText = defaultKaptcha.createText();
		// logger.info(createText);
		redisMapper.set(RedisConst.USER_GRAPH_CODE + phone, createText, 120);
		// request.getSession().setAttribute("vrifyCode", createText);
		// 使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
		BufferedImage challenge = defaultKaptcha.createImage(createText);
		ImageIO.write(challenge, "jpg", jpegOutputStream);
		// 定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
		byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(captchaChallengeAsJpeg);
		responseOutputStream.flush();
		responseOutputStream.close();
	}

	@RequestMapping(value = "mobile_code")
	@Api
	public ResultMessage mobile_code(String phone, String graph_code, String sms_type) {
		if (!CheckUtils.isMobiPhoneNum(phone)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号码错误");
		}
		if (!NumberUtils.isDigits(graph_code)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码错误");
		}
		EnumSmsTemplate enumSmsType = EnumSmsTemplate.getTemplate(sms_type);
		if (enumSmsType == null) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "短信事件类型错误");
		}
		// 注册类型,判断用户是否存在
		if ("1001".equals(sms_type) && userService.selectUserByPhone(phone, RequestThread.getClientAlias()) != null) {
			return new ResultMessage(ResponseEnum.M2001);
		}
		String redis_graph_code = redisMapper.get(RedisConst.USER_GRAPH_CODE + phone);
		if (!graph_code.equals(redis_graph_code)) {
			redisMapper.remove(RedisConst.USER_GRAPH_CODE + phone);
			return new ResultMessage(ResponseEnum.M2002);
		}
		String randomNum = RandomUtils.generateRandomNum(4);
		// 发送验证码，5分钟内有效
		redisMapper.set(RedisConst.USER_PHONE_CODE + phone, randomNum, 300);
		rabbitTemplate.convertAndSend(RabbitConst.queue_sms,
				new SmsMessage(RequestThread.getClientAlias(), enumSmsType.getKey(), phone, randomNum + "|5分钟"));
		redisMapper.remove(RedisConst.USER_GRAPH_CODE + phone);
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "user_judge_register")
	@Api
	public ResultMessage user_judge_register(String phone) {
		if (!CheckUtils.isMobiPhoneNum(phone)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号码错误");
		}
		if (userService.selectUserByPhone(phone, RequestThread.getClientAlias()) != null) {
			return new ResultMessage(ResponseEnum.M2001);
		}
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "user_register")
	@Api
	public ResultMessage user_register(String phone, String password, String phone_code, String origin) {
		if (StringUtils.isBlank(origin)) {
			origin = RequestThread.getClientType();
		}
		if (!CheckUtils.isMobiPhoneNum(phone)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号码错误");
		}
		if (StringUtils.isBlank(password)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "密码不能为空");
		}
		if (password.length() < 6) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "密码至少6位");
		}
		String redis_phone_code = redisMapper.get(RedisConst.USER_PHONE_CODE + phone);
		if (redis_phone_code == null) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码错误");
		}
		if (!redis_phone_code.equals(phone_code)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码错误");
		}
		if (userService.selectUserByPhone(phone, RequestThread.getClientAlias()) != null) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号已注册");
		}

		Long uid = userService.addUser(phone, password, origin, RequestThread.getClientAlias());
		redisMapper.remove(RedisConst.USER_PHONE_CODE + phone);
		userDeductionService.addUser(uid, origin, RequestThread.getClientAlias(), phone);
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "user_login")
	@Api
	public ResultMessage user_login(String phone, String password) {
		if (StringUtils.isBlank(phone)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "用户名不能为空");
		}
		if (StringUtils.isBlank(password)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "密码不能为空");
		}
		User user = userService.selectUserByPhone(phone, RequestThread.getClientAlias());
		if (user == null) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "用户不存在");
		}
		long increment = NumberUtils.toLong(redisMapper.get(RedisConst.USER_LOGIN + user.getId()));
		if (increment > 5) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "错误次数过多，请稍后重试");
		}
		if (!password.equals(user.getUserPwd())) {
			increment = redisMapper.increment(RedisConst.USER_LOGIN + user.getId(), 1L, 3600);
			if (increment > 5) {// 一个小时登录错误次数超过5次
				return new ResultMessage(ResponseEnum.M4000.getCode(), "错误次数过多，请稍后重试");
			}
			return new ResultMessage(ResponseEnum.M4000.getCode(), "密码错误");
		}

		// 返回用户的一些信息
		String token = jwtUtil.generToken(user.getId().toString(), phone, RequestThread.getClientType(),
				RequestThread.getClientAlias(), RequestThread.getClientVersion());
		Map<String, Object> userdata = new HashMap<>();
		userdata.put("token", token);
		userdata.put("uid", user.getId().toString());
		redisMapper.set(RedisConst.USER_TOKEN_PREFIX + user.getId(), token, 3 * 86400);
		return new ResultMessage(ResponseEnum.M2000, userdata);
	}

	@RequestMapping(value = "user_loginout")
	@LoginRequired(check = true)
	@Api
	public ResultMessage user_loginout() {
		redisMapper.remove(RedisConst.USER_TOKEN_PREFIX + RequestThread.getUid());
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "user_device")
	@LoginRequired(check = true)
	@Api
	public ResultMessage user_device(String deviceid, String location, String city, String netType, String phoneBrand,
			String phoneModel, String phoneSystem, String phoneResolution, String phoneMemory, String imei, String blackBox,
			String isp) {
		UserDevice userDevice = new UserDevice();
		userDevice.setUid(RequestThread.getUid());
		userDevice.setDeviceid(deviceid);
		userDevice.setIp(RequestThread.getIp());
		userDevice.setLocation(location);
		userDevice.setCity(city);
		userDevice.setNetType(netType);
		userDevice.setPhoneBrand(phoneBrand);
		if(StringUtils.isNotBlank(phoneModel) && phoneModel.length()>16){
			phoneModel=phoneModel.substring(0,16);
		}
		userDevice.setPhoneModel(phoneModel);
		userDevice.setPhoneSystem(phoneSystem);
		userDevice.setPhoneResolution(phoneResolution);
		userDevice.setPhoneMemory(phoneMemory);
		userDevice.setIsp(isp);
		userDevice.setClientAlias(RequestThread.getClientAlias());
		userDevice.setClientVersion(RequestThread.getClientVersion());
		userDevice.setImei(imei);
		userDevice.setBlackBox(blackBox);
        userDeviceMapper.insertSelective(userDevice);
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "user_update_pwd")
	@Api
	public ResultMessage user_update_pwd(String phone, String password, String phone_code) {
		if (StringUtils.isBlank(password)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "密码不能为空");
		}
		if (StringUtils.isBlank(phone)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "手机号不能为空");
		}
		User user = userService.selectUserByPhone(phone, RequestThread.getClientAlias());
		String redis_phone_code = redisMapper.get(RedisConst.USER_PHONE_CODE + phone);
		if (redis_phone_code == null) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码错误");
		}
		if (!redis_phone_code.equals(phone_code)) {
			return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码错误");
		}
		User record = new User();
		record.setId(user.getId());
		record.setUserPwd(password);
		userMapper.updateByPrimaryKeySelective(record);
		redisMapper.remove(RedisConst.USER_TOKEN_PREFIX + user.getId());
		redisMapper.remove(RedisConst.USER_PHONE_CODE + phone);
		return new ResultMessage(ResponseEnum.M2000);
	}

	@RequestMapping(value = "feedback")
	@LoginRequired(check = true)
	@Api
	public ResultMessage feedback(String questionType, String questionDesc, String questionImg) {
		if (!StringUtils.isBlank(questionDesc)) {
			questionDesc = StringReplaceUtil.replaceInvaildString(questionDesc);
		}
		AppFeedback appFeedback = new AppFeedback();
		appFeedback.setUid(RequestThread.getUid());
		appFeedback.setQuestionType(questionType);
		appFeedback.setQuestionImg(questionImg);
		appFeedback.setQuestionDesc(questionDesc);
		appFeedback.setMerchant(RequestThread.getClientAlias());
		feedbackMapper.insertSelective(appFeedback);
		return new ResultMessage(ResponseEnum.M2000);
	}
}
