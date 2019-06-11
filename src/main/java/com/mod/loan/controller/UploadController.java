package com.mod.loan.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.util.aliyun.OSSUtil;

@RestController
public class UploadController {

	@RequestMapping(value = "upload")
	@LoginRequired(check = true)
	public ResultMessage upload(@RequestParam("file") MultipartFile file) {
		Map<String, String> data = new HashMap<>();
		data.put("url", OSSUtil.upload(file));
		return new ResultMessage(ResponseEnum.M2000, data);
	}

}
