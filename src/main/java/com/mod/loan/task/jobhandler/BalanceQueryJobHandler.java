package com.mod.loan.task.jobhandler;

import com.mod.loan.task.BalanceQueryTask;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务Handler示例（Bean模式）
 *
 * 开发步骤：
 * 1、继承"IJobHandler"：“com.xxl.job.core.handler.IJobHandler”；
 * 2、注册到Spring容器：添加“@Component”注解，被Spring容器扫描为Bean实例；
 * 3、注册到执行器工厂：添加“@JobHandler(value="自定义jobhandler名称")”注解，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 4、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 *
 * @author xuxueli 2015-12-19 19:43:36
 */
@JobHandler(value="BalanceQueryJobHandler")
@Component
public class BalanceQueryJobHandler extends IJobHandler {

	private final BalanceQueryTask balanceQueryTask;

	@Autowired
	public BalanceQueryJobHandler(BalanceQueryTask balanceQueryTask) {
		this.balanceQueryTask = balanceQueryTask;
	}

	@Override
	public ReturnT<String> execute(String param)   {
		XxlJobLogger.log("XXL-JOB, MerchantBalanceQueryTask.");
		balanceQueryTask.MerchantBalanceQueryTask();

		return SUCCESS;
	}
}
