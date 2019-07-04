package com.mod.loan.task;

import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderDeferService;
import com.mod.loan.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("online")
@Component
public class OrderDeferChangjieRepayQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderDeferChangjieRepayQueryTask.class);

    private final OrderDeferService orderDeferService;
    private final OrderService orderService;
    private final MerchantService merchantService;

    @Autowired
    public OrderDeferChangjieRepayQueryTask(OrderDeferService orderDeferService, OrderService orderService, MerchantService merchantService) {
        this.orderDeferService = orderDeferService;
        this.orderService = orderService;
        this.merchantService = merchantService;
    }

    /**
     * 畅捷续期时协议支付还款结果查询定时任务
     * 每10分钟查询一次结果并更新还款结果
     */
    //@Scheduled(cron = "0 0/10 * * * ?")
    public void changjieOrderDeferRepayQuery() {
        logger.info("#[畅捷续期时协议支付还款结果查询定时任务]-[开始]");
        //获取畅捷代扣还款订单列表
        List<OrderDefer> orderDefers = orderDeferService.selectOrderDefer();
        orderDefers.stream().forEach(orderDefer -> {
            //不影响其他
            try {
                Merchant merchant = merchantService.findMerchantByAlias(orderDefer.getMerchant());
                if (null != merchant && MerchantEnum.CHANGJIE.getCode().equals(merchant.getBindType())) {
                    Order order = orderService.selectByPrimaryKey(orderDefer.getOrderId());
                    if (null != order) {
                        if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus()) || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus()) || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
                            logger.info("该续期订单已还款-orderId={}", order.getId());
                            return;
                        }
                        //去调畅捷续期时协议支付还款结果查询
                        orderDeferService.changjieDeferRepay4Query(orderDefer.getPayNo());
                    }
                }
            } catch (Exception e) {
                logger.error("#[去调畅捷续期时协议支付还款结果查询]-[异常]-e={}", e);
            }
        });
        logger.info("#[畅捷续期时协议支付还款结果查询定时任务]-[结束]");
    }

}
