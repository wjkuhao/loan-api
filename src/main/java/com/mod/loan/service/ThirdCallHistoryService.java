package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.ThirdCallHistory;

/**
 * @author actor
 * @date 2019/7/5 11:46
 */
public interface ThirdCallHistoryService extends BaseService<ThirdCallHistory,Integer> {
    void addCount(String merchant, String code, Long uid);
}
