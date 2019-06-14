package com.mod.loan.controller.order;

import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.model.request.*;
import com.mod.loan.service.ChangjieRepayCallBackService;
import com.mod.loan.service.ChangjieRepayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * 畅捷支付
 *
 * @author NIELIN
 * @version $Id: ChangjieRepayController.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
@CrossOrigin("*")
@RestController
@RequestMapping("changjieRepay")
public class ChangjieRepayController {
    private static Logger logger = LoggerFactory.getLogger(ChangjieRepayController.class);

    @Autowired
    ChangjieRepayService changjieRepayService;
    @Autowired
    ChangjieRepayCallBackService changjieRepayCallBackService;

    /**
     * 鉴权绑卡请求（API）
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4SendMsg")
    public ResultMessage bindBankCard4SendMsg(@RequestBody BindBankCard4SendMsgRequest request) {
        logger.info("#[鉴权绑卡请求（API）]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getPhone()) || StringUtils.isEmpty(request.getName())
                || StringUtils.isEmpty(request.getBankCardNo()) || StringUtils.isEmpty(request.getIdNo())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4SendMsg(request);
            logger.info("#[鉴权绑卡请求（API）]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[鉴权绑卡请求（API）]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 鉴权绑卡确认接口（API）
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4Confirm")
    public ResultMessage bindBankCard4Confirm(@RequestBody BindBankCard4ConfirmRequest request) {
        logger.info("#[鉴权绑卡确认接口（API）]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getSmsCode())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4Confirm(request);
            logger.info("#[鉴权绑卡确认接口（API）]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[鉴权绑卡确认接口（API）]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 鉴权解绑接口（API）
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4Unbind")
    public ResultMessage bindBankCard4Unbind(@RequestBody BindBankCard4UnbindRequest request) {
        logger.info("#[鉴权解绑接口（API）]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getCardBegin()) || StringUtils.isEmpty(request.getCardEnd())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4Unbind(request);
            logger.info("#[鉴权解绑接口（API）]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[鉴权解绑接口（API）]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 绑卡查询接口
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4Query")
    public ResultMessage bindBankCard4Query(@RequestBody BindBankCard4QueryRequest request) {
        logger.info("#[绑卡查询接口]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4Query(request);
            logger.info("#[绑卡查询接口]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[绑卡查询接口]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 短信验证码重发接口
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4ResendMsg")
    public ResultMessage bindBankCard4ResendMsg(@RequestBody BindBankCard4ResendMsgRequest request) {
        logger.info("#[短信验证码重发接口]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4ResendMsg(request);
            logger.info("#[短信验证码重发接口]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[短信验证码重发接口]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 协议支付还款发送验证码
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepaySendMsg")
    public ResultMessage bindBankCard4RepaySendMsg(@RequestBody BindBankCard4RepaySendMsgRequest request) {
        logger.info("#[协议支付还款发送验证码]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getMerchantName())
                || StringUtils.isEmpty(request.getBankCardNo()) || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4RepaySendMsg(request);
            logger.info("#[协议支付还款发送验证码]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[协议支付还款发送验证码]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 协议支付还款确认
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepayConfirm")
    public ResultMessage bindBankCard4RepayConfirm(@RequestBody BindBankCard4RepayConfirmRequest request) {
        logger.info("#[协议支付还款确认]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getSmsCode())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4RepayConfirm(request);
            logger.info("#[协议支付还款确认]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[协议支付还款确认]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 协议支付还款结果查询
     *
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "bindBankCard4RepayQuery")
    public ResultMessage bindBankCard4RepayQuery(@RequestBody TransCode4QueryRequest request) {
        logger.info("#[协议支付还款结果查询]-[开始]-request={}", request);
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo())) {
            return new ResultMessage(ResponseEnum.M5000);
        }
        try {
            String sd = changjieRepayService.bindBankCard4RepayQuery(request);
            logger.info("#[协议支付还款结果查询]-[结束]-sd={}", sd);
            return new ResultMessage(ResponseEnum.M2000, sd);
        } catch (Exception e) {
            logger.error("#[协议支付还款结果查询]-[异常]-e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 单笔代扣还款异步回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "repay_callback")
    public String repayCallback(HttpServletRequest request, HttpServletResponse response) {
        logger.info("#[单笔代扣还款异步回调]-[开始]");
        try {
            changjieRepayCallBackService.repayCallback(request);
            logger.info("#[单笔代扣还款异步回调]-[结束]-request={}", request);
        } catch (Exception e) {
            logger.error("#[单笔代扣还款异步回调]-[异常]-e={}", e);
        }
        return "success";
    }


}
