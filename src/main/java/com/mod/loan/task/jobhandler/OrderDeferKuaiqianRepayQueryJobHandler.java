package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderDeferKuaiqianRepayQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "OrderDeferKuaiqianRepayQueryJobHandler")
@Component
public class OrderDeferKuaiqianRepayQueryJobHandler extends IJobHandler {

    private final OrderDeferKuaiqianRepayQueryTask orderDeferKuaiqianRepayQueryTask;

    @Autowired
    public OrderDeferKuaiqianRepayQueryJobHandler(OrderDeferKuaiqianRepayQueryTask orderDeferKuaiqianRepayQueryTask) {
        this.orderDeferKuaiqianRepayQueryTask = orderDeferKuaiqianRepayQueryTask;
    }

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("XXL-JOB, OrderDeferKuaiqianRepayQueryJobHandler.");
        orderDeferKuaiqianRepayQueryTask.kuaiqianOrderDeferRepayQuery();
        return SUCCESS;
    }

}
