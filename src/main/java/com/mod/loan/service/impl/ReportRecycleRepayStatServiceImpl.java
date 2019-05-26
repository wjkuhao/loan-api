package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.model.ReportRecycleRepayStat;
import com.mod.loan.service.ReportRecycleRepayStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportRecycleRepayStatServiceImpl extends BaseServiceImpl<ReportRecycleRepayStat, Long> implements ReportRecycleRepayStatService {
	private static Logger logger = LoggerFactory.getLogger(ReportRecycleRepayStatServiceImpl.class);

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public ReportRecycleRepayStatServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendRecycleToMQ(String recycleDate, Long recycledId) {
	    if (recycledId!=null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("recycleDate", recycleDate);
            jsonObject.put("recycledId", recycledId);
            rabbitTemplate.convertAndSend(RabbitConst.QUEUE_RECYCLE_REPAY_STAT, jsonObject);
        }
	}
}
