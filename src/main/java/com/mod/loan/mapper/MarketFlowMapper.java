package com.mod.loan.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MarketFlow;

public interface MarketFlowMapper extends MyBaseMapper<MarketFlow> {
	
	int updateMarketFlow(@Param("product_id")Long productId,@Param("flow_date")Date date);
	
	int insertMarketFlowNotExit(@Param("product_id")Long productId,@Param("flow_date")Date date);
}