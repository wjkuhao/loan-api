package com.mod.loan.controller.order;

import com.mod.loan.service.ChangjiePayCallBackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 畅捷支付
 *
 * @author NIELIN
 * @version $Id: ChangjiePayController.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@RestController
@RequestMapping("changjiePay")
public class ChangjiePayController {
    private static Logger logger = LoggerFactory.getLogger(ChangjiePayController.class);

    @Autowired
    ChangjiePayCallBackService changjiePayCallBackService;

    /**
     * 单笔代付放款异步回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "pay_callback")
    public String payCallback(HttpServletRequest request, HttpServletResponse response) {
        logger.info("#[单笔代付放款异步回调]-[开始]");
        changjiePayCallBackService.payCallback(request);
        logger.info("#[单笔代付放款异步回调]-[结束]");
        return "success";
    }

}
