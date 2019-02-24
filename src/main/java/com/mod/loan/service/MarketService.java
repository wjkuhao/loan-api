package com.mod.loan.service;

import java.util.Date;
import java.util.List;

import com.mod.loan.model.dto.MarketModuleDTO;

public interface MarketService {

	List<MarketModuleDTO>  findModuleListByChannel(Long channel_id);
	
	void updateMarketFlow(Long productId, Date date);
	
	List<MarketModuleDTO>  findModuleProductList(Integer type);
}
