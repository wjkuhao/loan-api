package com.mod.loan.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mod.loan.config.Constant;
import com.mod.loan.mapper.MarketFlowMapper;
import com.mod.loan.mapper.MarketModuleMapper;
import com.mod.loan.mapper.MarketProductMapper;
import com.mod.loan.model.MarketModule;
import com.mod.loan.model.MarketProduct;
import com.mod.loan.model.dto.MarketModuleDTO;
import com.mod.loan.service.MarketService;

@Service
public class MarketServiceImpl implements MarketService {

	@Autowired
	MarketModuleMapper moduleMapper;
	@Autowired
	MarketProductMapper productMapper;
	@Autowired
	MarketFlowMapper flowMapper;
	@Override
	public List<MarketModuleDTO> findModuleListByChannel(Long channel_id) {
		// TODO Auto-generated method stub
		List<MarketModuleDTO> list=new ArrayList<>(8);
		List<MarketModule> moduleList = moduleMapper.selectByChannel(channel_id);
		moduleList.forEach(item ->{
			List<MarketProduct> products = productMapper.selectByModule(item.getId(),null);
			products.forEach(p->{
				p.setProductImg(Constant.SERVER_PREFIX_URL+p.getProductImg());
			});
			list.add(new MarketModuleDTO(item.getId(), item.getModuleName(),products ));
		});
		return list;
	}
	@Override
	public void updateMarketFlow(Long productId,Date date) {
		// TODO Auto-generated method stub
		if (flowMapper.updateMarketFlow(productId, date)==0) {
			int effect_row=0;
			synchronized (this) {
				effect_row=flowMapper.insertMarketFlowNotExit(productId, date);
			}
			if (effect_row==0) {
				flowMapper.updateMarketFlow(productId, date);
			}
		}
	}
	@Override
	public List<MarketModuleDTO> findModuleProductList(Integer type) {
		// TODO Auto-generated method stub
		List<MarketModuleDTO> list=new ArrayList<>(8);
		List<MarketModule> moduleList = moduleMapper.selectAllModule();
		moduleList.forEach(item ->{
			List<MarketProduct> products = productMapper.selectByModule(item.getId(),type);
			products.forEach(p->{
				p.setProductType(null);
				p.setProductImg(Constant.SERVER_PREFIX_URL+p.getProductImg());
				if (p.getNum()==0) {
					p.setNum(10000);
				}
			});
			list.add(new MarketModuleDTO(item.getId(), item.getModuleName(),products ));
		});
		return list;
	}

}
