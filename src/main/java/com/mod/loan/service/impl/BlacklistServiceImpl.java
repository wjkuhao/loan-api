package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.BlacklistMapper;
import com.mod.loan.model.Blacklist;
import com.mod.loan.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class BlacklistServiceImpl extends BaseServiceImpl<Blacklist, Long> implements BlacklistService {

	@Autowired
	BlacklistMapper blacklistMapper;

	@Override
	public Blacklist getByUid(Long uid) {
		return blacklistMapper.getByUid(uid);
	}

	@Override
	public Blacklist getByPhone(String phone) {
		return blacklistMapper.getByPhone(phone);
	}
}
