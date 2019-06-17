package com.mod.loan.task;

import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.OrderHuichaoRepayService;
import com.mod.loan.service.OrderRepayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("online")
@Component
public class OrderHuichao4AliAppH5OrWxScanQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderHuichao4AliAppH5OrWxScanQueryTask.class);

    private final OrderHuichaoRepayService orderHuichaoRepayService;
    private final OrderRepayService orderRepayService;

    @Autowired
    public OrderHuichao4AliAppH5OrWxScanQueryTask(OrderHuichaoRepayService orderHuichaoRepayService, OrderRepayService orderRepayService) {
        this.orderHuichaoRepayService = orderHuichaoRepayService;
        this.orderRepayService = orderRepayService;
    }

    /**
     * 汇潮支付宝还款/微信扫码支付结果查询定时任务
     * 每10分钟查询一次结果并更新还款结果
     */
    //@Scheduled(cron = "0 0/10 * * * ?")
    public void huichao4AliAppH5OrWxScanQuery() {
        logger.info("#[汇潮支付宝还款/微信扫码支付结果查询定时任务]-[开始]");
        //获取还款订单列表
        List<OrderRepay> orderRepayList = orderRepayService.huichaoRepay4AliAppH5OrWxScanQuery();
        orderRepayList.stream().forEach(orderRepay -> {
            //不影响其他
            try {
                //去调畅捷代扣还款结果查询
                orderHuichaoRepayService.huichaoRepay4AliAppH5OrWxScanQuery(orderRepay.getRepayNo());
            } catch (Exception e) {
                logger.error("#[汇潮支付宝还款/微信扫码支付结果查询定时任务]-[异常]-e={}", e);
            }
        });
        logger.info("#[汇潮支付宝还款/微信扫码支付结果查询定时任务]-[结束]");
    }

}
