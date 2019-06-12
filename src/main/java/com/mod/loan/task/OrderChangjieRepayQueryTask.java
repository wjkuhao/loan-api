package com.mod.loan.task;

import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.OrderChangjieRepayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("online")
@Component
public class OrderChangjieRepayQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderChangjieRepayQueryTask.class);

    private final OrderChangjieRepayService orderChangjieRepayService;

    @Autowired
    public OrderChangjieRepayQueryTask(OrderChangjieRepayService orderChangjieRepayService) {
        this.orderChangjieRepayService = orderChangjieRepayService;
    }

    /**
     * 畅捷代扣还款结果查询定时任务
     * 每10分钟查询一次结果并更新还款结果
     */
    //@Scheduled(cron = "0 0/10 * * * ?")
    public void changjieRepayQuery() {
        try {
            logger.info("#[畅捷代扣还款结果查询定时任务]-[开始]");
            //获取畅捷代扣还款订单列表
            List<OrderRepay> orderRepayList = orderChangjieRepayService.changjieRepayQuery4Task();
            orderRepayList.stream().forEach(orderRepay -> {
                //不影响其他
                try {
                    //去调畅捷代扣还款结果查询
                    orderChangjieRepayService.bindBankCard4RepayQuery(orderRepay.getRepayNo());
                } catch (Exception e) {
                    logger.error("#[去调畅捷代扣还款结果查询]-[异常]-e={}", e);
                }
            });
            logger.info("#[畅捷代扣还款结果查询定时任务]-[结束]");
        } catch (Exception e) {
            logger.error("#[畅捷代扣还款结果查询定时任务]-[异常]-e={}", e);
        }
    }

}
