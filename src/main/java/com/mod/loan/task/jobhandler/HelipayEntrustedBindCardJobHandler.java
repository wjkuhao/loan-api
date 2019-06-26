package com.mod.loan.task.jobhandler;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.task.HelipayEntrustedBindCardTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "HelipayEntrustedBindCardJobHandler")
@Component
public class HelipayEntrustedBindCardJobHandler extends IJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelipayEntrustedBindCardJobHandler.class);

    private final HelipayEntrustedBindCardTask bindCardTask;

    @Autowired
    public HelipayEntrustedBindCardJobHandler(HelipayEntrustedBindCardTask bindCardTask) {
        this.bindCardTask = bindCardTask;
    }

    @Override
    public ReturnT<String> execute(String params) {
        try {
            XxlJobLogger.log("XXL-JOB, HelipayEntrustedBindCardJobHandler.");
            logger.info("HelipayEntrustedBindCardJobHandler params:{}", params);
            JSONObject data = JSONObject.parseObject(params);
            String phone = data.getString("phone");
            String merchant = data.getString("merchant");
            if (StringUtils.isEmpty(merchant)) {
                return ReturnT.FAIL;
            }
            if (StringUtils.isNotEmpty(phone)) {
                bindCardTask.bindCard(phone, merchant);
            } else {
                bindCardTask.bindCardBatch(merchant);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return SUCCESS;
    }

}
