package com.mod.loan.controller.order;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.service.HelipayRepayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单还款确认，回收和贷款文案通用
 *
 * @author yhx 2018年5月3日 上午9:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class HelipayRepayController {

    @Autowired
    private HelipayRepayService helipayRepayService;

    /**
     * h5 借款详情 线上主动还款 绑卡支付短信
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "repay_text")
    public ResultMessage repay_text(@RequestParam(required = true) String orderId) {
        return helipayRepayService.bindPaySmsProcess(orderId, "order");
    }

    /**
     * h5 回购详情
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "repay_info")
    public ResultMessage repay_info(@RequestParam(required = true) String repayNo) {
        return helipayRepayService.repayInfo(repayNo, "order");
    }

    /**
     * 绑卡支付
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "repay_active")
    public ResultMessage repay_active(@RequestParam(required = true) String repayNo,
                                      @RequestParam(required = true) String validateCode) {
        return helipayRepayService.repayActive(repayNo, validateCode, "order");
    }

    /**
     * 订单异步通知
     */
    @LoginRequired(check = false)
    @RequestMapping(value = "repay_result")
    public String repay_result(@RequestParam(required = true) String rt2_retCode,
                               @RequestParam(required = true) String rt9_orderStatus, @RequestParam(required = true) String rt5_orderId) {
        helipayRepayService.repayResult(rt2_retCode,
                rt9_orderStatus, rt5_orderId);
        return "success";
    }

    /**
     * h5 借款详情 线上续期付款 绑卡支付短信
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "defer_repay_text")
    public ResultMessage defer_repay_text(@RequestParam(required = true) String orderId) {
        return helipayRepayService.bindPaySmsProcess(orderId, "orderDefer");
    }


    /**
     * h5 续期回购详情
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "defer_repay_info")
    public ResultMessage defer_repay_info(@RequestParam(required = true) String repayNo) {
        return helipayRepayService.repayInfo(repayNo, "orderDefer");
    }

    /**
     * 续期绑卡支付
     */
    @LoginRequired(check = true)
    @RequestMapping(value = "defer_repay_active")
    public ResultMessage defer_repay_active(@RequestParam(required = true) String repayNo,
                                            @RequestParam(required = true) String validateCode) {
        return helipayRepayService.repayActive(repayNo, validateCode, "orderDefer");
    }

    /**
     * 续期订单异步通知
     */
    @LoginRequired(check = false)
    @RequestMapping(value = "defer_repay_result")
    public String defer_repay_result(@RequestParam(required = true) String rt2_retCode,
                                     @RequestParam(required = true) String rt9_orderStatus, @RequestParam(required = true) String rt5_orderId) {
        helipayRepayService.deferRepayResult(rt2_retCode,
                rt9_orderStatus, rt5_orderId);
        return "success";

    }

}
