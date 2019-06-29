package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppArticle;
import com.mod.loan.model.AppConfig;

public interface AppConfigMapper extends MyBaseMapper<AppConfig> {
    AppConfig selectByClientAlias(String clientAlias);
}