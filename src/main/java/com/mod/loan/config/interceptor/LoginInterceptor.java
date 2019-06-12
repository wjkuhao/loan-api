package com.mod.loan.config.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.Api;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.HttpUtils;
import com.mod.loan.util.jwtUtil;

import io.jsonwebtoken.Claims;

/**
 * 
 * @author wgy 2017年8月24日 上午10:48:12
 */
public class LoginInterceptor implements HandlerInterceptor {
	public static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

	@Autowired
	RedisMapper redisMapper;
	@Autowired
	UserService userService;
	@Autowired
	MerchantService merchantService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		RequestThread.remove();//移除本地线程变量
		String ip = HttpUtils.getIpAddr(request, ".");
		String clientVersion = request.getParameter("version");
		String clientType = request.getParameter("type");
		String clientAlias = request.getParameter("alias");
		RequestThread.setClientVersion(clientVersion);
		RequestThread.setClientType(clientType);
		RequestThread.setClientAlias(clientAlias);
		RequestThread.setIp(ip);
		RequestThread.setRequestTime(System.currentTimeMillis());
		HandlerMethod hm = (HandlerMethod) handler;

		//logger.info("ip={},version={},type={},alias={}, hm={}", ip,clientVersion,clientType,clientAlias,hm);

		Api api = hm.getMethodAnnotation(Api.class);
		if (api!=null) {
			if (StringUtils.isEmpty(clientType)||! ("android".equals(clientType)|| "ios".equals(clientType))) {
				printMessage(response,  new ResultMessage(ResponseEnum.M4000.getCode(), "无效的type"));
				return false;
			}
			if (StringUtils.isEmpty(clientVersion)) {
				printMessage(response,  new ResultMessage(ResponseEnum.M4000.getCode(), "无效的version"));
				return false;
			}
			if (StringUtils.isEmpty(clientAlias)) {
				printMessage(response,  new ResultMessage(ResponseEnum.M4000.getCode(), "无效的alias"));
				return false;
			}
			if (merchantService.findMerchantByAlias(clientAlias)==null) {
				printMessage(response,  new ResultMessage(ResponseEnum.M4000.getCode(), "无效的alias"));
				return false;
			}
		}
		LoginRequired lr = hm.getMethodAnnotation(LoginRequired.class);
		if(lr != null && lr.check() && !isLogin(request)){
			printMessage(response, new ResultMessage(ResponseEnum.M4002));
			return false;
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		RequestThread.remove();// 移除本地线程变量
	}

	private boolean isLogin(HttpServletRequest request){
		String token = request.getParameter("token");
		//logger.info("isLogin token:{}", token);
		if(StringUtils.isBlank(token)){
				return false;
		}
		Claims verifyToken = jwtUtil.ParseJwt(token);
		//logger.info("isLogin verifyToken:{}", verifyToken);
		if (verifyToken == null) {
			return false;
		}
		String uid = String.valueOf(verifyToken.get("uid"));
		String clientType = String.valueOf(verifyToken.get("clientType"));
		String clientVersion = String.valueOf(verifyToken.get("clientVersion"));
		String clientAlias = String.valueOf(verifyToken.get("clientAlias"));
		String token_redis =redisMapper.get(RedisConst.USER_TOKEN_PREFIX + uid);
		//logger.info("isLogin token_redis:{}", token_redis);
		if(!token.equals(token_redis)){
			return false;
		}
		redisMapper.set(RedisConst.USER_TOKEN_PREFIX + uid, token_redis,3*86400);
		RequestThread.setUid(Long.parseLong(uid));
		RequestThread.setClientVersion(clientVersion);
		RequestThread.setClientType(clientType);
		RequestThread.setClientAlias(clientAlias);
		return true;
	}
	
	private void printMessage(HttpServletResponse response,ResultMessage message) throws IOException {
		response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
		response.getWriter().write(JSONObject.toJSONString(message));
	}
}
