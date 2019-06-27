package com.mod.loan.controller;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.task.HelipayEntrustedBindCardTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新增合利宝委托代付绑卡测试入口
 */
@RestController
@RequestMapping("helipayTest")
public class HelipayTestController {

    private static final Logger logger = LoggerFactory.getLogger(HelipayTestController.class);

    @Autowired
    private HelipayEntrustedBindCardTask bindCardTask;

    @RequestMapping(value = "helipayEntrustedBindCard")
    public ResultMessage helipayEntrustedBindCard(String phone, String merchant) {
        try {
            bindCardTask.bindCardByPhone(phone, merchant);
        } catch (Exception e) {
            logger.error("helipayEntrustedBindCard error", e);
        }
        return new ResultMessage(ResponseEnum.M2000, new JSONObject());
    }

    @RequestMapping(value = "helipayEntrustedBindCardBatch")
    public ResultMessage helipayEntrustedBindCardBatch(String merchant) {
        try {
            bindCardTask.bindCardMerchant(merchant);
        } catch (Exception e) {
            logger.error("helipayEntrustedBindCardBatch error", e);
        }
        return new ResultMessage(ResponseEnum.M2000, new JSONObject());
    }

}
