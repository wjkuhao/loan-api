package com.mod.loan.task;

import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderDeferService;
import com.mod.loan.service.OrderService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderDeferTask {

	private static final Logger logger = LoggerFactory.getLogger(OrderDeferTask.class);

	private final OrderService orderService;
	private final MerchantService merchantService;
	private final OrderDeferService orderDeferService;

	@Autowired
	public OrderDeferTask(OrderService orderService, MerchantService merchantService, OrderDeferService orderDeferService) {
		this.orderService = orderService;
        this.merchantService = merchantService;
        this.orderDeferService = orderDeferService;
    }

	//每2分钟查询一次结果并更新还款结果
    //@Scheduled(cron = "0 0/2 * * * ?")
    public void yeepayDeferQuery() {
        try {
            logger.info("------------------yeepay query defer start------------------");
            List<OrderDefer> orderDefers = orderDeferService.selectOrderDefer();

            for (OrderDefer orderDefer : orderDefers) {
                Order order = orderService.selectByPrimaryKey(orderDefer.getOrderId());
                if (41 == order.getStatus() || 42 == order.getStatus()|| 43 == order.getStatus()) {
                    logger.info("展期易宝自动查询:订单={}已还款", order.getId());
                    continue;
                }

                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
                if(merchant.getBindType().equals(MerchantEnum.yeepay.getCode())){
                    String errMsg = orderDeferService.yeepayRepayQuery(orderDefer.getPayNo(), order.getMerchant());
                    if (StringUtils.isEmpty(errMsg)) {
                        orderDefer.setRemark("展期易宝自动查询：交易成功");
                        orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
                        orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
                    }else if ("PROCESSING".equals(errMsg)) {
                        logger.info("---------yeepay query repayno= {}展期订单处理中-----------------", orderDefer.getPayNo());
                    }
                    else {
                        orderDefer.setRemark("展期易宝自动查询"+errMsg);
                        orderDefer.setPayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
                        orderDeferService.modifyOrderDeferByPayCallback(orderDefer);
                    }

                    Thread.sleep(100);
                }
            }
            logger.info("------------------yeepay query defer end--------------------");
        } catch (Exception e) {
            logger.error("展期结果查询异常={}", e);
        }
    }
}
