package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderDeferChangjieRepayQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "OrderDeferChangjieRepayQueryJobHandler")
@Component
public class OrderDeferChangjieRepayQueryJobHandler extends IJobHandler {

    private final OrderDeferChangjieRepayQueryTask orderDeferChangjieRepayQueryTask;

    @Autowired
    public OrderDeferChangjieRepayQueryJobHandler(OrderDeferChangjieRepayQueryTask orderDeferChangjieRepayQueryTask) {
        this.orderDeferChangjieRepayQueryTask = orderDeferChangjieRepayQueryTask;
    }

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("XXL-JOB, OrderDeferChangjieRepayQueryJobHandler.");
        orderDeferChangjieRepayQueryTask.changjieOrderDeferRepayQuery();
        return SUCCESS;
    }

}
