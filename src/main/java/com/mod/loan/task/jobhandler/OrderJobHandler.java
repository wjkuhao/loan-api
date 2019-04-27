package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value="BalanceQueryJobHandler")
@Component
public class OrderJobHandler extends IJobHandler {

    private final OrderTask orderTask;

    @Autowired
    public OrderJobHandler(OrderTask orderTask) {
        this.orderTask = orderTask;
    }

    @Override
    public ReturnT<String> execute(String param)  {
        XxlJobLogger.log("XXL-JOB, MerchantBalanceQueryTask.");
        orderTask.autoRepayOverdueInfoTask();

        return SUCCESS;
    }

}
