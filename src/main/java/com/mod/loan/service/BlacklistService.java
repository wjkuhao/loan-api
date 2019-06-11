package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.Blacklist;

public interface BlacklistService extends BaseService<Blacklist,Long> {

    Blacklist getByUid(Long uid);

    Blacklist getByPhone(String phone);
}
