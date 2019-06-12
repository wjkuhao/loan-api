package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderChangjieRepayQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "OrderChangjieRepayQueryJobHandler")
@Component
public class OrderChangjieRepayQueryJobHandler extends IJobHandler {

    private final OrderChangjieRepayQueryTask orderChangjieRepayQueryTask;

    @Autowired
    public OrderChangjieRepayQueryJobHandler(OrderChangjieRepayQueryTask orderChangjieRepayQueryTask) {
        this.orderChangjieRepayQueryTask = orderChangjieRepayQueryTask;
    }

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("XXL-JOB, OrderChangjieRepayQueryJobHandler.");
        orderChangjieRepayQueryTask.changjieRepayQuery();
        return SUCCESS;
    }

}
