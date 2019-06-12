package com.mod.loan.task;

import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.KuaiqianService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderRepayService;
import com.mod.loan.service.OrderService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Profile("online")
@Component
public class OrderTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTask.class);

    private final OrderRepayService orderRepayService;
    private final OrderService orderService;
    private final MerchantService merchantService;
    private final KuaiqianService kuaiqianService;

    @Autowired
    public OrderTask(OrderRepayService orderRepayService, OrderService orderService, MerchantService merchantService, KuaiqianService kuaiqianService) {
        this.orderRepayService = orderRepayService;
        this.orderService = orderService;
        this.merchantService = merchantService;
        this.kuaiqianService = kuaiqianService;
    }

    //每天11点和晚上9点自动还款（当日还款、逾期、坏账的单子）
    //@Scheduled(cron = "0 0 11,21 * * ?")
    public void autoRepayOverdueInfoTask() {
        try {
            logger.info("------------------auto repay start------------------");
            List<Order> overdueOrder = orderService.findOverdueOrder();
            for (Order order : overdueOrder) {
                logger.info(String.valueOf(order.getId()));

                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());

                int bindType = merchant.getBindType();
                switch (bindType) {
                    case 1://合利宝
                        orderRepayService.heliPayRepayNoSms(order.getId());
                        break;
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
            logger.error("自动代扣异常={}", e);
        }
    }

    //每2分钟查询一次结果并更新还款结果
    //@Scheduled(cron = "0 0/2 * * * ?")
    public void yeepayRepayQuery() {
        try {
            logger.info("------------------yeepay query repay start------------------");
            List<OrderRepay> orderRepayList = orderRepayService.selectReapyingOrder();

            for (OrderRepay orderRepay : orderRepayList) {
                Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
                if (41 == order.getStatus() || 42 == order.getStatus() || 43 == order.getStatus()) {
                    logger.info("易宝自动查询:订单={}已还款", order.getOrderNo());
                    continue;
                }

                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
                if (merchant.getBindType().equals(MerchantEnum.yeepay.getCode())) {
                    String errMsg = orderRepayService.yeepayRepayQuery(orderRepay.getRepayNo(), order.getMerchant());
                    if (StringUtils.isEmpty(errMsg)) {
                        orderRepayService.repaySuccess(orderRepay, order);
                    } else if ("PROCESSING".equals(errMsg)) {
                        logger.info("---------yeepay query repayno= {}订单处理中-----------------", orderRepay.getRepayNo());
                    } else {
                        orderRepayService.repayFailed(orderRepay, errMsg);
                    }

                    Thread.sleep(100);
                }
            }
            logger.info("------------------yeepay query repay end--------------------");
        } catch (Exception e) {
            logger.error("还款结果查询异常={}", e);
        }
    }

    //每2分钟查询一次结果并更新还款结果
    public void kuaiqianRepayQuery() {
        try {
            logger.info("------------------kuaiqian query repay start------------------");
            List<OrderRepay> orderRepayList = orderRepayService.selectReapyingOrder();

            for (OrderRepay orderRepay : orderRepayList) {
                Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
                if (41 == order.getStatus() || 42 == order.getStatus() || 43 == order.getStatus()) {
                    logger.info("快钱自动查询:订单={}已还款", order.getOrderNo());
                    continue;
                }

                Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
                if (merchant.getBindType().equals(MerchantEnum.kuaiqian.getCode())) {
                    Map respMap = kuaiqianService.queryKuaiqianRepayOrder(orderRepay.getUid(), orderRepay.getOrderId(), order.getMerchant());
                    if ("00".equals(MapUtils.getString(respMap, "responseCode"))) {
                        orderRepayService.repaySuccess(orderRepay, order);
                    } else if ("C0".equals(MapUtils.getString(respMap, "responseCode"))
                            || "68".equals(MapUtils.getString(respMap, "responseCode"))) {
                        logger.info("---------kuaiqian query repayno= {}订单处理中-----------------", orderRepay.getRepayNo());
                    } else {
                        orderRepayService.repayFailed(orderRepay, MapUtils.getString(respMap, "responseTextMessage"));
                    }

                    Thread.sleep(100);
                }
            }
            logger.info("------------------kuaiqian query repay end--------------------");
        } catch (Exception e) {
            logger.error("还款结果查询异常={}", e);
        }
    }
}
