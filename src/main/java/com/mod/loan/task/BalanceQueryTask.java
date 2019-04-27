package com.mod.loan.task;

import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.message.QueueSmsMessage;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.model.Merchant;
import com.mod.loan.service.HeliPayService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.YeepayService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("balanceTest")
public class BalanceQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(BalanceQueryTask.class);

    private final YeepayService yeepayService;
    private final MerchantService merchantService;
    private final RabbitTemplate rabbitTemplate;
    private final HeliPayService heliPayService;

    @Autowired
    public BalanceQueryTask(YeepayService yeepayService, MerchantService merchantService, RabbitTemplate rabbitTemplate, HeliPayService heliPayService) {
        this.yeepayService = yeepayService;
        this.merchantService = merchantService;
        this.rabbitTemplate = rabbitTemplate;
        this.heliPayService = heliPayService;
    }

    //每天晚上10点查询一次余额
    //@Scheduled(cron = "0 0 22 * * ?")
    public void MerchantBalanceQueryTask() {
        try {
            logger.info("------------------balanceQueryTask start------------------");
            List<Merchant> merchantList = merchantService.selectAll();
            for (Merchant merchant : merchantList) {
                int bindType = merchant.getBindType();
                StringBuffer balance = new StringBuffer();
                String errMsg = "";
                switch (bindType) {
                    case 1://合利宝
                        errMsg = heliPayService.balanceQuery(merchant, balance);
                        break;
//                    case 2://富友
//                        break;
//                    case 3://汇聚
//                        break;
                    case 4://易宝
                        errMsg = yeepayService.balanceQuery(merchant.getYeepay_loan_appkey(), merchant.getYeepay_loan_private_key(), balance);
                        break;
                    default:
                        logger.error("bindType = {} unsupport", bindType);
                        break;
                }
                if (StringUtils.isEmpty(errMsg)) {
                    sendSmsMessage(merchant.getMerchantAlias(), balance.toString());
                } else {
                    logger.error("bindType:{} merchant:{} balanceQuery error:{}", bindType, merchant.getMerchantAlias(), errMsg);
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            logger.error("商户余额查询异常={}", e);
        }

        logger.info("------------------balanceQueryTask end--------------------");
    }

    private void sendSmsMessage(String merchant, String balance) {
        QueueSmsMessage smsMessage = new QueueSmsMessage();
        smsMessage.setClientAlias(merchant);
        smsMessage.setType("2004"); //短信类型：余额通知短信
        smsMessage.setPhone("15757127746,13979127403,18072878602");
        smsMessage.setParams(balance);
        rabbitTemplate.convertAndSend(RabbitConst.queue_sms, smsMessage);
    }

    @RequestMapping(value = "query")
    public ResultMessage balance_query_test() {
        MerchantBalanceQueryTask();
        return new ResultMessage(ResponseEnum.M2000);
    }
}
