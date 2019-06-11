package com.mod.loan.service;

import com.mod.loan.common.model.ResultMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface KuaiqianService {
	ResultMessage orderRepayKuaiqian(Long orderId);

	ResultMessage queryKuaiqianRepayOrder(Long orderId);

	Map queryKuaiqianRepayOrder(Long uid, Long orderId, String merchantAlias);

	void kuaiqianOrderRepayNotice(HttpServletRequest request, HttpServletResponse response);

	void kuaiqianOrderPayNotice(HttpServletRequest request, HttpServletResponse response);
}
