package com.mod.loan.mapper;

import java.util.List;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppNotice;

public interface AppNoticeMapper extends MyBaseMapper<AppNotice> {
	
	List<AppNotice> findNoticeListByMerchant(String merchant);
}