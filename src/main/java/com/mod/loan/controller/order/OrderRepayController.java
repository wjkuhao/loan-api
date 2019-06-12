package com.mod.loan.controller.order;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.request.BindBankCard4RepayConfirmRequest;
import com.mod.loan.service.OrderChangjieRepayService;
import com.mod.loan.service.OrderHuichaoRepayService;
import com.mod.loan.service.OrderRepayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author NIELIN
 * @version $Id: OrderRepayController.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@CrossOrigin("*")
@RestController
@RequestMapping("orderRepay")
public class OrderRepayController {
    private static Logger logger = LoggerFactory.getLogger(OrderRepayController.class);

    @Autowired
    OrderChangjieRepayService orderChangjieRepayService;
    @Autowired
    OrderHuichaoRepayService orderHuichaoRepayService;
    @Autowired
    OrderRepayService orderRepayService;

    /**
     * 畅捷协议支付还款发送验证码
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepaySendMsg")
    public ResultMessage bindBankCard4RepaySendMsg(@RequestParam("orderId") Long orderId) {
        logger.info("#[畅捷协议支付还款发送验证码]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderChangjieRepayService.bindBankCard4RepaySendMsg(orderId);
        if (null == result) {
            logger.error("#[畅捷协议支付还款发送验证码]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[畅捷协议支付还款发送验证码]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 畅捷协议支付还款确认
     *
     * @param seriesNo 协议支付的流水号
     * @param smsCode  短信验证码
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepayConfirm")
    public ResultMessage bindBankCard4RepayConfirm(BindBankCard4RepayConfirmRequest request) {
        logger.info("#[畅捷协议支付还款确认]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getSmsCode())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderChangjieRepayService.bindBankCard4RepayConfirm(request.getSeriesNo(), request.getSmsCode());
        if (null == result) {
            logger.error("#[畅捷协议支付还款确认]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[畅捷协议支付还款确认]-[结束]");
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 畅捷协议支付还款结果查询
     *
     * @param repayNo 还款流水号
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepayQuery")
    public ResultMessage bindBankCard4RepayQuery(@RequestParam("repayNo") String repayNo) {
        logger.info("#[畅捷协议支付还款结果查询]-[开始]-request={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        orderChangjieRepayService.bindBankCard4RepayQuery(repayNo);
        logger.info("#[畅捷协议支付还款结果查询]-[结束]");
        return new ResultMessage(ResponseEnum.M2000);
    }

    /**
     * 汇潮支付宝还款
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "huichaoRepay4AliAppH5")
    public ResultMessage huichaoRepay4AliAppH5(@RequestParam("orderId") Long orderId) {
        logger.info("#[汇潮支付宝还款]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderHuichaoRepayService.huichaoRepay4AliAppH5(orderId);
        if (null == result) {
            logger.error("#[汇潮支付宝还款]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[汇潮支付宝还款]-[结束]-result={}", result);
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 汇潮支付宝还款/微信扫码支付结果查询
     *
     * @param repayNo 还款流水号
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "huichaoRepay4AliAppH5OrWxScanQuery")
    public ResultMessage huichaoRepay4AliAppH5OrWxScanQuery(@RequestParam("repayNo") String repayNo) {
        logger.info("#[汇潮支付宝还款/微信扫码支付结果查询]-[开始]-request={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        orderHuichaoRepayService.huichaoRepay4AliAppH5OrWxScanQuery(repayNo);
        logger.info("#[汇潮支付宝还款/微信扫码支付结果查询]-[结束]");
        return new ResultMessage(ResponseEnum.M2000);
    }

    /**
     * 汇潮微信扫码支付
     *
     * @param orderId 订单id
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "huichaoRepay4WxScan")
    public ResultMessage huichaoRepay4WxScan(@RequestParam("orderId") Long orderId) {
        logger.info("#[汇潮微信扫码支付]-[开始]-request={}", orderId);
        if (null == orderId) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = orderHuichaoRepayService.huichaoRepay4WxScan(orderId);
        if (null == result) {
            logger.error("#[汇潮微信扫码支付]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[汇潮微信扫码支付]-[结束]-result={}", result);
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * 修改还款订单状态
     *
     * @param repayNo 还款流水号
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "updateOrderRepayInfo")
    public ResultMessage updateOrderRepayInfo(@RequestParam("repayNo") String repayNo) {
        logger.info("#[修改还款订单状态]-[开始]-request={}", repayNo);
        if (StringUtils.isEmpty(repayNo)) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        //根据还款流水号查询订单还款流水记录
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
        if (null == orderRepay) {
            return new ResultMessage(ResponseEnum.M4000, "该还款流水记录不存在");
        }
        //幂等
        if (1 != orderRepay.getRepayStatus()) {
            throw new RuntimeException("该笔还款流水状态不是受理成功");
        }
        orderRepayService.updateOrderRepayInfo(orderRepay, null);
        logger.info("#[修改还款订单状态]-[结束]");
        return new ResultMessage(ResponseEnum.M2000);
    }

}
