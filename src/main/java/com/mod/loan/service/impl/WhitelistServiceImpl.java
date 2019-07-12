package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.BlacklistMapper;
import com.mod.loan.mapper.WhitelistMapper;
import com.mod.loan.model.Blacklist;
import com.mod.loan.model.Whitelist;
import com.mod.loan.service.BlacklistService;
import com.mod.loan.service.WhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class WhitelistServiceImpl extends BaseServiceImpl<Whitelist, Long> implements WhitelistService {

	@Autowired
	WhitelistMapper whitelistMapper;

	@Override
	public Whitelist getByPhone(String phone) {
		return whitelistMapper.getByPhone(phone);
	}
}
