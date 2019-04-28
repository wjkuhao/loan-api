package com.mod.loan.controller.defer;

import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.MerchantDeferConfig;
import com.mod.loan.service.MerchantDeferConfigService;
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

    @Autowired
    private MerchantDeferConfigService merchantDeferConfigService;

    /**
     * 查询商户是否支持续期
     *
     * @param merchant merchant_alias
     * @return 4000-不支持 2000-支持
     */
    @GetMapping("/status")
    public ResultMessage status(@RequestParam String merchant) {
        MerchantDeferConfig condition = new MerchantDeferConfig();
        condition.setMerchant(merchant);
        MerchantDeferConfig merchantDeferConfig = merchantDeferConfigService.selectOne(condition);
        if (null == merchantDeferConfig || merchantDeferConfig.getStatus() < 1) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户不支持续期");
        }
        return new ResultMessage(ResponseEnum.M2000, merchantDeferConfig);
    }

}
