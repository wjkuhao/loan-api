package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.service.KuaiqianService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class KuaiqianRepayController {
    private static Logger logger = LoggerFactory.getLogger(KuaiqianRepayController.class);
    @Autowired
    KuaiqianService kuaiqianService;

    /**
     * 快钱支付还款--以同步返回结果为准，受理中订单主动查询
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "order_repay_kuaiqian")
    public ResultMessage orderRepayKuaiqian(@RequestParam(required = true) Long orderId) {
        logger.info("#[快钱支付还款]-[请求参数]-request={}", orderId);
        ResultMessage resultMessage = kuaiqianService.orderRepayKuaiqian(orderId);
        logger.info("#[快钱支付还款]-[结束]-result={}", JSONObject.toJSON(resultMessage));
        return resultMessage;
    }

    /**
     * 查询快钱还款支付订单状态
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "query_kuaiqian_repay_order")
    public ResultMessage queryKuaiqianRepayOrder(@RequestParam(required = true) Long orderId) {
        logger.info("#[查询快钱支付订单状态]-[请求参数]-request={}", orderId);
        ResultMessage resultMessage = kuaiqianService.queryKuaiqianRepayOrder(orderId);
        logger.info("#[查询快钱支付订单状态]-[结束]-result={}", JSONObject.toJSON(resultMessage));
        return resultMessage;
    }
}
