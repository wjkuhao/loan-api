package com.mod.loan.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mod.loan.model.MarketProduct;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketModuleDTO {
    private Long id;

    private String moduleName;

	private List<MarketProduct> list=new ArrayList<>();
	
    public MarketModuleDTO() {
		super();
	}

	public MarketModuleDTO(Long id, String moduleName, List<MarketProduct> list) {
		super();
		this.id = id;
		this.moduleName = moduleName;
		this.list = list;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public List<MarketProduct> getList() {
		return list;
	}

	public void setList(List<MarketProduct> list) {
		this.list = list;
	}
    
    
    

}