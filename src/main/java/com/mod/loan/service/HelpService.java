package com.mod.loan.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;



public interface HelpService {

	Map<String, List<JSONObject>> findQuestionList(String merchant);
}
