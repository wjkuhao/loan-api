package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppFeedback;

public interface AppFeedbackMapper extends MyBaseMapper<AppFeedback> {
    int selectFeedbackCount(Long uid);
}