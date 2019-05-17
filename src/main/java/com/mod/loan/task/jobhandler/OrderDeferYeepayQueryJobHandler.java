package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderDeferTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value="OrderDeferYeepayQueryJobHandler")
@Component
public class OrderDeferYeepayQueryJobHandler extends IJobHandler {

    private final OrderDeferTask orderDeferTask;

    @Autowired
    public OrderDeferYeepayQueryJobHandler(OrderDeferTask orderDeferTask) {
        this.orderDeferTask = orderDeferTask;
    }

    @Override
    public ReturnT<String> execute(String param)  {
        XxlJobLogger.log("XXL-JOB, OrderDeferYeepayQueryJobHandler.");
        orderDeferTask.yeepayDeferQuery();

        return SUCCESS;
    }

}
