package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.ReportRecycleRepayStat;

public interface ReportRecycleRepayStatService extends BaseService<ReportRecycleRepayStat, Long> {

	void sendRecycleToMQ(String recycleDate, Long followUserId);
}
