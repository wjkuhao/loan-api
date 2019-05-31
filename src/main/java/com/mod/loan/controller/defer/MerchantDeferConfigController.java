package com.mod.loan.controller.defer;

import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.Blacklist;
import com.mod.loan.model.MerchantDeferConfig;
import com.mod.loan.model.Order;
import com.mod.loan.model.User;
import com.mod.loan.service.BlacklistService;
import com.mod.loan.service.MerchantDeferConfigService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 续期配置接口
 *
 * @author kibear
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/merchant_defer_config")
public class MerchantDeferConfigController {

    private final MerchantDeferConfigService merchantDeferConfigService;
    private final OrderService orderService;
    private final UserService userService;
    private final BlacklistService blacklistService;

    @Autowired
    public MerchantDeferConfigController(MerchantDeferConfigService merchantDeferConfigService, OrderService orderService, UserService userService, BlacklistService blacklistService) {
        this.merchantDeferConfigService = merchantDeferConfigService;
        this.orderService = orderService;
        this.userService = userService;
        this.blacklistService = blacklistService;
    }

    /**
     * 查询商户是否支持续期
     *
     * @param merchant merchant_alias
     * @return 4000-不支持 2000-支持
     */
    @GetMapping("/status")
    public ResultMessage status(@RequestParam String merchant, @RequestParam String orderId) {
        MerchantDeferConfig condition = new MerchantDeferConfig();
        condition.setMerchant(merchant);
        MerchantDeferConfig merchantDeferConfig = merchantDeferConfigService.selectOne(condition);
        if (null == merchantDeferConfig || merchantDeferConfig.getStatus() < 1) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户不支持续期");
        }

        Order order = orderService.selectByPrimaryKey(Long.valueOf(orderId));
        User user = userService.selectByPrimaryKey(order.getUid());
        Blacklist blacklist = blacklistService.getByPhone(user.getUserPhone());
        if (blacklist!=null){
            return new ResultMessage(ResponseEnum.M4000.getCode(), "客户不支持续期");
        }
        return new ResultMessage(ResponseEnum.M2000, merchantDeferConfig);
    }

}
