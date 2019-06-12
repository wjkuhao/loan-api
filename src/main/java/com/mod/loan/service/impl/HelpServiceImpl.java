package com.mod.loan.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.mapper.QuestionTypeMapper;
import com.mod.loan.service.HelpService;

@Service
public class HelpServiceImpl implements HelpService{

	@Autowired
	QuestionTypeMapper questionTypeMapper;
	@Override
	public Map<String, List<JSONObject>> findQuestionList(String merchant) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> questionRefList = questionTypeMapper.findQuestionRefList(merchant);
		Map<String, List<JSONObject>> keys=new LinkedHashMap<>();
		questionRefList.forEach(item->{
			String type= (String) item.get("type");
			List<JSONObject> list =keys.get(type);
			if (list == null) {//分类不已存在
				list=new  ArrayList<>();
				JSONObject obj=new JSONObject();
				obj.put("title", item.get("title"));
				obj.put("article_id", item.get("article_id"));
				list.add(obj);
				keys.put(type, list);
			}else {
				JSONObject obj=new JSONObject();
				obj.put("title", item.get("title"));
				obj.put("article_id", item.get("article_id"));
				list.add(obj);
			}
		});
		return keys;
	}

}
