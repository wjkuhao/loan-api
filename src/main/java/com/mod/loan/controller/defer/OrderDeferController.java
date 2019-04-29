package com.mod.loan.controller.defer;

import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.MerchantDeferConfig;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderDefer;
import com.mod.loan.model.User;
import com.mod.loan.service.MerchantDeferConfigService;
import com.mod.loan.service.OrderDeferService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 续期订单接口
 *
 * @author kibear
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/order_defer")
public class OrderDeferController {

    private final MerchantDeferConfigService merchantDeferConfigService;
    private final OrderDeferService orderDeferService;
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderDeferController(MerchantDeferConfigService merchantDeferConfigService, //
                                OrderDeferService orderDeferService, //
                                OrderService orderService, //
                                UserService userService) {
        this.merchantDeferConfigService = merchantDeferConfigService;
        this.orderDeferService = orderDeferService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/compute")
    public ResultMessage compute(@RequestParam Long orderId,
                                 @RequestParam String merchant,
                                 @RequestParam(defaultValue = "7") Integer deferDay) {
        MerchantDeferConfig condition = new MerchantDeferConfig();
        condition.setMerchant(merchant);
        MerchantDeferConfig merchantDeferConfig = merchantDeferConfigService.selectOne(condition);
        // 检查商户是否支持续期
        if (null == merchantDeferConfig || merchantDeferConfig.getStatus() < 1) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户不支持续期");
        }

        // 计算当前第几次续期
        int deferTimes = orderDeferService.selectCount(new OrderDefer(orderId)) + 1;// 当前续期个数加1
        if (null != merchantDeferConfig.getMaxDeferTimes() //
                && merchantDeferConfig.getMaxDeferTimes() > 0 //
                && deferTimes > merchantDeferConfig.getMaxDeferTimes()) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "续期次数达到最大限制");
        }
        // 计算续期费和还款时间
        Double dailyDeferFee = merchantDeferConfig.getDailyDeferFee();
        Double dailyDeferRate = merchantDeferConfig.getDailyDeferRate();
        Order order = orderService.selectByPrimaryKey(orderId);
        if (null != dailyDeferRate && dailyDeferRate > 0D) {
            // 如果设置了续期费率
            dailyDeferFee = order.getBorrowMoney().doubleValue() * dailyDeferRate / 100D;
        }
        double deferFee = dailyDeferFee * deferDay;
        // 计算还款时间
        String deferReapyDate = TimeUtil.datePlusDays(order.getRepayTime(), deferDay);

        //
        OrderDefer orderDefer = new OrderDefer();
        orderDefer.setOrderId(orderId);
        orderDefer.setDeferTimes(deferTimes);
        orderDefer.setDeferDay(deferDay);
        orderDefer.setDailyDeferFee(dailyDeferFee);
        orderDefer.setDeferFee(deferFee);
        orderDefer.setRepayDate(TimeUtil.dateFormat(order.getRepayTime()));
        orderDefer.setDeferRepayDate(deferReapyDate);
        //
        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
    }

    @PostMapping("/create")
    public ResultMessage create(@RequestBody OrderDefer orderDefer) {
        //
        Order order = orderService.selectByPrimaryKey(orderDefer.getOrderId());
        User user = userService.selectByPrimaryKey(order.getUid());
        orderDefer.setUserName(user.getUserName());
        orderDefer.setUserPhone(user.getUserPhone());
        orderDefer.setCreateTime(TimeUtil.nowTime());
        //
        orderDeferService.insertSelective(orderDefer);
        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
    }

    @GetMapping("/find_last_valid")
    public ResultMessage findByOrderId(@RequestParam Long orderId) {
        OrderDefer orderDefer = orderDeferService.findLastValidByOrderId(orderId);
        if (null == orderDefer) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "找不到有效的续期单");
        }
        return new ResultMessage(ResponseEnum.M2000.getCode(), orderDefer);
    }

}
