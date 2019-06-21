package com.mod.loan.task;

import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.OrderJInYunTongRePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("online")
@Component
public class OrderJinYunTongRepayQueryTask {
    private static final Logger logger = LoggerFactory.getLogger(OrderJinYunTongRepayQueryTask.class);
    private final OrderJInYunTongRePayService orderJInYunTongRePayService;

    @Autowired
    public OrderJinYunTongRepayQueryTask(OrderJInYunTongRePayService orderJInYunTongRePayService){
        this.orderJInYunTongRePayService=orderJInYunTongRePayService;
    }

    /**
     * 金运通代扣还款结果查询定时任务
     * 每10分钟查询一次结果并更新还款结果
     */
    //@Scheduled(cron = "0 0/10 * * * ?")
    public void jinyuntongRepayQuery() {
        logger.info("#[金运通代扣还款结果查询定时任务]-[开始]");
        //获取畅捷代扣还款订单列表
        List<OrderRepay> orderRepayList = orderJInYunTongRePayService.jinyuntongRepayQuery4Task();
        orderRepayList.stream().forEach(orderRepay -> {
            //不影响其他
            try {
                //去金运通
                orderJInYunTongRePayService.queryRePayStatus(orderRepay.getRepayNo());
            } catch (Exception e) {
                logger.error("#[去调金运通代扣还款结果查询]-[异常]-e={}", e);
            }
        });
        logger.info("#[金运通代扣还款结果查询定时任务]-[结束]");
    }
}
