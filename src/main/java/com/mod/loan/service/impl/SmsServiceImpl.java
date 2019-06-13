package com.mod.loan.service.impl;

import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.model.MerchantOrigin;
import com.mod.loan.service.OriginService;
import com.mod.loan.service.SmsService;
import com.mod.loan.util.sms.SmsMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private static Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final OriginService originService;

    @Autowired
    public SmsServiceImpl(RabbitTemplate rabbitTemplate, OriginService originService) {
        this.rabbitTemplate = rabbitTemplate;
        this.originService = originService;
    }

    public void send(String alias, String templateKey, String phone, String param, String origin){
	    try {
	        if (StringUtils.isNotEmpty(origin)){
                MerchantOrigin merchantOrigin = originService.selectByPrimaryKey(Long.valueOf(origin));
                if (merchantOrigin!=null && StringUtils.isNotEmpty(merchantOrigin.getSmsMerchant())) {
                    alias = merchantOrigin.getSmsMerchant();
                }
	        }
	    }catch (Exception e){
	        logger.error("send error alias={}, origin={}", alias, origin);
        }

        rabbitTemplate.convertAndSend(RabbitConst.queue_sms,
                        new SmsMessage(alias, templateKey, phone, param));
	}



}
