package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderJinYunTongRepayQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "OrderJinYunTongRepayQueryJobHandler")
@Component
public class OrderJinYunTongRepayQueryJobHandler extends IJobHandler {

    private final OrderJinYunTongRepayQueryTask orderJinYunTongRepayQueryTask;

    @Autowired
    public OrderJinYunTongRepayQueryJobHandler(OrderJinYunTongRepayQueryTask orderJinYunTongRepayQueryTask) {
        this.orderJinYunTongRepayQueryTask = orderJinYunTongRepayQueryTask;
    }

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("XXL-JOB, OrderJinYunTongRepayQueryJobHandler.");
        orderJinYunTongRepayQueryTask.jinyuntongRepayQuery();
        return SUCCESS;
    }
}
