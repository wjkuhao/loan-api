package com.mod.loan.controller.user;

import com.mod.loan.common.annotation.Api;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "customer_service")
public class CustomerServiceController {

	/**
	 * 客服信息查询
	 */
	@Api
	@LoginRequired(check = true)
	@RequestMapping(value = "/info")
	public ResultMessage getInfo() {
		Map<String, Object> data = new HashMap<>();
		//data.put("qq", "2513171881");
		data.put("tel","13282833651");
		return new ResultMessage(ResponseEnum.M2000, data);
	}


}
