package com.mod.loan.mapper;

import java.util.List;
import java.util.Map;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.QuestionType;

public interface QuestionTypeMapper extends MyBaseMapper<QuestionType> {
	
	List<Map<String, Object>> findQuestionRefList(String merchant);

}