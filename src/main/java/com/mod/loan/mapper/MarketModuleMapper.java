package com.mod.loan.mapper;

import java.util.List;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MarketModule;

public interface MarketModuleMapper extends MyBaseMapper<MarketModule> {

	List<MarketModule> selectByChannel(Long channel_id);

	List<MarketModule> selectAllModule();
}