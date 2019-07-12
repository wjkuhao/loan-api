package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.Blacklist;
import com.mod.loan.model.Whitelist;

public interface WhitelistService extends BaseService<Whitelist,Long> {

    Whitelist getByPhone(String phone);
}
