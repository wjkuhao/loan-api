package com.mod.loan.mapper;

import java.util.List;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MarketChannel;

public interface MarketChannelMapper extends MyBaseMapper<MarketChannel> {
	
	List<MarketChannel> selectMarketChannelList();
}