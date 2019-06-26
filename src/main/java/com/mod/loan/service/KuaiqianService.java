package com.mod.loan.service;

import com.mod.loan.common.model.ResultMessage;

import java.util.Map;

public interface KuaiqianService {
	ResultMessage orderRepayKuaiqian(Long orderId);

	ResultMessage queryKuaiqianRepayOrder(Long orderId);

	Map queryKuaiqianRepayOrder(Long uid, Long orderId, String merchantAlias);
}
