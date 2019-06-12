package com.mod.loan.controller.order;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.request.AliAppH5RepayQueryRequest;
import com.mod.loan.model.request.AliAppH5RepayRequest;
import com.mod.loan.service.HuichaoRepayCallBackService;
import com.mod.loan.service.HuichaoRepayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * @author NIELIN
 * @version $Id: HuichaoRepayController.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@CrossOrigin("*")
@RestController
@RequestMapping("huichaoRepay")
public class HuichaoRepayController {
    private static Logger logger = LoggerFactory.getLogger(HuichaoRepayController.class);

    @Autowired
    HuichaoRepayService huichaoRepayService;
    @Autowired
    HuichaoRepayCallBackService huichaoRepayCallBackService;

    /**
     * app端支付宝还款
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "aliAppH5RepayUrl")
    public ResultMessage aliAppH5RepayUrl(AliAppH5RepayRequest request) {
        logger.info("#[app端支付宝还款]-[开始]-request={}", request);
        if (null == request || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String aliAppH5RepayUrl = huichaoRepayService.aliAppH5RepayUrl(request);
        if (null == aliAppH5RepayUrl) {
            logger.error("#[app端支付宝还款]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[app端支付宝还款]-[结束]-aliAppH5RepayUrl={}", aliAppH5RepayUrl);
        return new ResultMessage(ResponseEnum.M2000, aliAppH5RepayUrl);
    }

    /**
     * app端支付宝还款/微信扫码支付结果查询--没有推送异步通知的订单需要商户去主动查询订单结果（ 建议创建订单30分钟后再做主动查询）
     * "payResult":0——未付。重新生成订单号，发起交易；
     * "payResult":1——成功。更新订单状态为成功；
     * "payResult":3——处理中。 周期为从订单发起开始的创建时间+6小时 （这个商户可以根据自己的订单数据的整体回调时间做个评估，合理缩短周期时间），超过这个范围一律按失败处理。
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "aliAppH5OrWxScanRepayQuery")
    public ResultMessage aliAppH5OrWxScanRepayQuery(AliAppH5RepayQueryRequest request) {
        logger.info("#[app端支付宝还款/微信扫码支付结果查询]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getSeriesNo())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = huichaoRepayService.aliAppH5OrWxScanRepayQuery(request);
        if (null == result) {
            logger.error("#[app端支付宝还款/微信扫码支付结果查询]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[app端支付宝还款/微信扫码支付结果查询]-[结束]-result={}", result);
        return new ResultMessage(ResponseEnum.M2000, result);
    }

    /**
     * app端支付宝还款/微信扫码支付异步回调--商户请以异步通知的订单状态为主（ 只有交易成功的订单才会推送异步通知）
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "aliAppH5OrWxScan_repay_callback")
    public String aliAppH5OrWxScanRepayCallback(HttpServletRequest request, HttpServletResponse response) {
        logger.info("#[app端支付宝还款/微信扫码支付异步回调-]-[开始]");
        huichaoRepayCallBackService.aliAppH5OrWxScanRepayCallback(request);
        logger.info("#[app端支付宝还款/微信扫码支付异步回调-]-[结束]");
        return "success";
    }

    /**
     * 微信扫码支付
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "wxScanRepay")
    public ResultMessage wxScanRepay(AliAppH5RepayRequest request) {
        logger.info("#[微信扫码支付]-[开始]-request={}", request);
        if (null == request || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        String result = huichaoRepayService.wxScanRepay(request);
        if (null == result) {
            logger.error("#[app端支付宝还款/微信扫码支付结果查询]-[异常]");
            return new ResultMessage(ResponseEnum.M4000);
        }
        logger.info("#[微信扫码支付]-[结束]-result={}", result);
        return new ResultMessage(ResponseEnum.M2000, result);
    }

}
