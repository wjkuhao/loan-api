package com.mod.loan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.MarketProduct;

public interface MarketProductMapper extends MyBaseMapper<MarketProduct> {
	
	List<MarketProduct> selectByModule(@Param("module_id")Long module_id,@Param("type")Integer type);
}