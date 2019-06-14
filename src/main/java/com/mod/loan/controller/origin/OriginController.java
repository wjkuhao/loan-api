package com.mod.loan.controller.origin;


import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.MerchantOrigin;
import com.mod.loan.service.OriginService;
import com.mod.loan.util.Base64ToMultipartFileUtil;
import com.mod.loan.util.DesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "origin")
public class OriginController {
    private static Logger logger = LoggerFactory.getLogger(OriginController.class);

    private final OriginService originService;

    @Autowired
    public OriginController(OriginService originService) {
        this.originService = originService;
    }

    @RequestMapping(value = "status")
    public ResultMessage origin_status(String id, String merchant) {
        try {
            if ("haitun".equals(merchant)) {
                id = Base64ToMultipartFileUtil.decodeOrigin(id);
            } else if ("huijie".equals(merchant)) {
                id = DesUtil.decryption(id, null);
            } else if ("mx".equals(merchant)){
                // mx没有加密
            }
            else {
                id = DesUtil.decryption(id, null);
            }

            MerchantOrigin merchantOrigin = originService.selectByPrimaryKey(Long.valueOf(id));
            if ((merchantOrigin != null) && merchantOrigin.getMerchant().equals(merchant)) {
                return new ResultMessage(ResponseEnum.M2000, merchantOrigin.getStatus());
            }
            return new ResultMessage(ResponseEnum.M2000, 1); //1表示停用
        } catch (Exception e) {
            logger.error("origin/status error id={}, merchant={}", id, merchant);
            return new ResultMessage(ResponseEnum.M2000, 1); //1表示停用
        }

    }


}
