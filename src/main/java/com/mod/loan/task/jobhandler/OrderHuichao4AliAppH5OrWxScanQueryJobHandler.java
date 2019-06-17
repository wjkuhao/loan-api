package com.mod.loan.task.jobhandler;

import com.mod.loan.task.OrderHuichao4AliAppH5OrWxScanQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("online")
@JobHandler(value = "OrderHuichao4AliAppH5OrWxScanQueryJobHandler")
@Component
public class OrderHuichao4AliAppH5OrWxScanQueryJobHandler extends IJobHandler {

    private final OrderHuichao4AliAppH5OrWxScanQueryTask orderHuichao4AliAppH5OrWxScanQueryTask;

    @Autowired
    public OrderHuichao4AliAppH5OrWxScanQueryJobHandler(OrderHuichao4AliAppH5OrWxScanQueryTask orderHuichao4AliAppH5OrWxScanQueryTask) {
        this.orderHuichao4AliAppH5OrWxScanQueryTask = orderHuichao4AliAppH5OrWxScanQueryTask;
    }

    @Override
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("XXL-JOB, OrderHuichao4AliAppH5OrWxScanQueryJobHandler.");
        orderHuichao4AliAppH5OrWxScanQueryTask.huichao4AliAppH5OrWxScanQuery();
        return SUCCESS;
    }

}
