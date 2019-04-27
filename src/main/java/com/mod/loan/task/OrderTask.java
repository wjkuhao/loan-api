package com.mod.loan.task;

import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderRepayService;
import com.mod.loan.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderTask {

	private static final Logger logger = LoggerFactory.getLogger(OrderTask.class);
	
	private final OrderRepayService orderRepayService;
	private final OrderService orderService;
	private final MerchantService merchantService;

	@Autowired
	public OrderTask(OrderRepayService orderRepayService, OrderService orderService, MerchantService merchantService) {
		this.orderRepayService = orderRepayService;
		this.orderService = orderService;
        this.merchantService = merchantService;
    }

	//每天11点和晚上9点自动还款（当日还款、逾期、坏账的单子）
	@Scheduled(cron = "0 0 11,21 * * ?")
    public void updateOverdueInfoTask() {
		try {
            logger.info("------------------auto repay start------------------");
			List<Order> overdueOrder = orderService.findOverdueOrder();
            for (Order order : overdueOrder) {
                logger.info(String.valueOf(order.getId()));

                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());

                int bindType = merchant.getBindType();
                switch (bindType){
                    case 1://合利宝
                        orderRepayService.heliPayRepayNoSms(order.getId());
//                        break;
//                    case 2://富友
//                        break;
//                    case 3://汇聚
//                        break;
                    case 4://易宝
                        orderRepayService.yeepayRepayNoSms(order.getId());
                        break;
                    default:
                        logger.error("bindType = {} unsupport", bindType);
                        break;
                }

				Thread.sleep(100);
			}
            logger.info("------------------auto repay end--------------------");
        } catch (Exception e) {
			logger.error("自动代扣异常", e);
		}
	}

}
