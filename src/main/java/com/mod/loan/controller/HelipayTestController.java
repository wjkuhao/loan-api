//package com.mod.loan.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.mod.loan.common.enums.ResponseEnum;
//import com.mod.loan.common.model.ResultMessage;
//import com.mod.loan.task.HelipayEntrustedBindCardTask;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * 新增合利宝委托代付绑卡测试入口
// * */
//@RestController
//@RequestMapping("helipayTest")
//public class HelipayTestController {
//
//    @Autowired
//    private HelipayEntrustedBindCardTask bindCardTask;
//
//    @RequestMapping(value = "helipayEntrustedBindCard")
//    public ResultMessage helipayEntrustedBindCard(String phone, String merchant) {
//        bindCardTask.bindCard(phone, merchant);
//        return new ResultMessage(ResponseEnum.M2000, new JSONObject());
//    }
//
//    @RequestMapping(value = "helipayEntrustedBindCardBatch")
//    public ResultMessage helipayEntrustedBindCardBatch(String merchant) {
//        bindCardTask.bindCardBatch(merchant);
//        return new ResultMessage(ResponseEnum.M2000, new JSONObject());
//    }
//
//}
