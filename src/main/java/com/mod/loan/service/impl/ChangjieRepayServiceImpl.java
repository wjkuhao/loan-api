package com.mod.loan.service.impl;

import com.mod.loan.common.enums.ChangjieIDTypeEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.model.request.*;
import com.mod.loan.service.ChangjieRepayService;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.changjie.BaseConstant;
import com.mod.loan.util.changjie.BaseParameter;
import com.mod.loan.util.changjie.ChanPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author NIELIN
 * @version $Id: ChangjieRepayServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class ChangjieRepayServiceImpl implements ChangjieRepayService {
    private static Logger logger = LoggerFactory.getLogger(ChangjieRepayServiceImpl.class);

    @Value("${changjie.url}")
    String changjieUrl;
    @Value("${changjie.Version}")
    String changjieVersion;
    @Value("${changjie.bindBankCard4SendMsg}")
    String changjieBindBankCard4SendMsg;
    @Value("${changjie.bindBankCard4Confirm}")
    String changjieBindBankCard4Confirm;
    @Value("${changjie.bindBankCard4Unbind}")
    String changjieBindBankCard4Unbind;
    @Value("${changjie.bindBankCard4RepaySendMsg}")
    String changjieBindBankCard4RepaySendMsg;
    @Value("${changjie.bindBankCard4RepayConfirm}")
    String changjieBindBankCard4RepayConfirm;
    @Value("${changjie.bindBankCard4RepayQuery}")
    String changjieBindBankCard4RepayQuery;
    @Value("${changjie.bindBankCard4Query}")
    String changjieBindBankCard4Query;
    @Value("${changjie.bindBankCard4ResendMsg}")
    String changjieBindBankCard4ResendMsg;
    @Value("${changjie.repayCallbackUrl}")
    String changjieRepayCallbackUrl;
    @Value("${changjie.deferRepayCallbackUrl}")
    String changjieDeferRepayCallbackUrl;

    @Override
    public String bindBankCard4SendMsg(BindBankCard4SendMsgRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getPhone()) || StringUtils.isEmpty(request.getName())
                || StringUtils.isEmpty(request.getBankCardNo()) || StringUtils.isEmpty(request.getIdNo()) || StringUtils.isEmpty(request.getPartnerId())
                || StringUtils.isEmpty(request.getPublicKey()) || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4SendMsg, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //订单有效期
        origMap.put("ExpiredTime", "20m");
        //用户标识
        origMap.put("MerUserId", String.valueOf(RequestThread.getUid()));
        //卡类型（00 – 银行贷记卡;01 – 银行借记卡;）
        origMap.put("BkAcctTp", "01");
        //卡号
        origMap.put("BkAcctNo", ChanPayUtil.encrypt(request.getBankCardNo(), request.getPublicKey(), BaseConstant.CHARSET));
        //证件类型 （目前只支持身份证 01：身份证）
        origMap.put("IDTp", ChangjieIDTypeEnum.ID_CARD.getCode());
        //证件号
        origMap.put("IDNo", ChanPayUtil.encrypt(request.getIdNo(), request.getPublicKey(), BaseConstant.CHARSET));
        //持卡人姓名
        origMap.put("CstmrNm", ChanPayUtil.encrypt(request.getName(), request.getPublicKey(), BaseConstant.CHARSET));
        //银行预留手机号
        origMap.put("MobNo", ChanPayUtil.encrypt(request.getPhone(), request.getPublicKey(), BaseConstant.CHARSET));
        //发短信验证码
        origMap.put("SmsFlag", "1");
        //扩展字段
        origMap.put("Extension", "");
        logger.info("#[准备调畅捷鉴权绑卡发短信验证码的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷鉴权绑卡发短信验证码的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4Confirm(BindBankCard4ConfirmRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getSmsCode()) || StringUtils.isEmpty(request.getPartnerId())
                || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4Confirm, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //原鉴权绑卡订单号
        origMap.put("OriAuthTrxId", request.getSeriesNo());
        //鉴权短信验证码
        origMap.put("SmsCode", request.getSmsCode());
        logger.info("#[准备调畅捷鉴权绑卡确认的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷鉴权绑卡确认的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4Unbind(BindBankCard4UnbindRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getCardBegin()) || StringUtils.isEmpty(request.getCardEnd())
                || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4Unbind, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //用户标识
        origMap.put("MerUserId", String.valueOf(RequestThread.getUid()));
        //解绑模式。0为物理解绑，1为逻辑解绑
        origMap.put("UnbindType", "1");
        //卡号前6位
        origMap.put("CardBegin", request.getCardBegin());
        //卡号后4位
        origMap.put("CardEnd", request.getCardEnd());
        //扩展字段
        origMap.put("Extension", "");
        logger.info("#[准备调畅捷鉴权解绑的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷鉴权解绑的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4Query(BindBankCard4QueryRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4Query, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //用户标识
        origMap.put("MerUserId", String.valueOf(RequestThread.getUid()));
        //卡类型(00 – 银行贷记卡;01 – 银行借记卡)
        origMap.put("BkAcctTp", "01");
        logger.info("#[准备调畅捷鉴权绑卡查询的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷鉴权绑卡查询的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4ResendMsg(BindBankCard4ResendMsgRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo())
                || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4ResendMsg, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //原业务请求订单号
        origMap.put("OriTrxId", request.getSeriesNo());
        //原业务订单类型(鉴权订单：auth_order 支付订单:pay _order)
        origMap.put("TradeType", "auth_order");
        logger.info("#[准备调畅捷鉴权绑卡重发短信验证码的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷鉴权绑卡重发短信验证码的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4RepaySendMsg(BindBankCard4RepaySendMsgRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getMerchantName())
                || StringUtils.isEmpty(request.getBankCardNo()) || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0 || StringUtils.isEmpty(request.getPartnerId())
                || StringUtils.isEmpty(request.getPublicKey()) || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4RepaySendMsg, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //商品名称
        origMap.put("OrdrName", request.getMerchantName());
        //用户标识
        origMap.put("MerUserId", String.valueOf(RequestThread.getUid()));
        //子账户号
        origMap.put("SellerId", request.getPartnerId());
        //子商户号
        origMap.put("SubMerchantNo", "");
        //订单有效期
        origMap.put("ExpiredTime", "40m");
        //卡号前6位
        origMap.put("CardBegin", request.getBankCardNo().substring(0, 6));
        //卡号后4位
        origMap.put("CardEnd", StringUtil.bankTailNo(request.getBankCardNo()));
        //交易金额
        origMap.put("TrxAmt", request.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        //交易类型 （交易类型（即时 11 担保 12））
        origMap.put("TradeType", "11");
        //回调地址
        if (request.getRequestSeriesNo().substring(0, 1).equals("r")) {
            origMap.put("NotifyUrl", changjieRepayCallbackUrl);
        }
        //续期
        else if (request.getRequestSeriesNo().substring(0, 1).equals("d")) {
            origMap.put("NotifyUrl", changjieDeferRepayCallbackUrl);
        }
        //发短信（0：不发送短信1：发送短信）
        origMap.put("SmsFlag", "1");
        logger.info("#[准备调畅捷协议支付还款发送验证码的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷协议支付还款发送验证码的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4RepayConfirm(BindBankCard4RepayConfirmRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getSmsCode()) || StringUtils.isEmpty(request.getPartnerId())
                || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4RepayConfirm, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //原鉴权绑卡订单号
        origMap.put("OriPayTrxId", request.getSeriesNo());
        //鉴权短信验证码
        origMap.put("SmsCode", request.getSmsCode());
        logger.info("#[准备调畅捷协议支付还款确认的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷协议支付还款确认的返回结果]-result={}", result);
        return result;
    }

    @Override
    public String bindBankCard4RepayQuery(TransCode4QueryRequest request) {
        if (null == request || StringUtils.isEmpty(request.getRequestSeriesNo()) || StringUtils.isEmpty(request.getSeriesNo()) || StringUtils.isEmpty(request.getPartnerId())
                || StringUtils.isEmpty(request.getPrivateKey())) {
            logger.info("参数为空");
            return null;
        }
        //组装公共请求参数
        Map<String, String> origMap = BaseParameter.requestBaseParameter(changjieBindBankCard4RepayQuery, changjieVersion, request.getPartnerId());
        //组装业务参数
        //String trxId = StringUtil.getOrderNumber("c");
        //订单号
        origMap.put("TrxId", request.getRequestSeriesNo());
        //原订单号
        origMap.put("OrderTrxId", request.getSeriesNo());
        //原业务订单类型（auth_order：鉴权订单 pay_order   ：支付订单refund_order：退款订单）
        origMap.put("TradeType", "pay_order");
        logger.info("#[准备调畅捷协议支付还款结果查询的请求参数]-origMap={}", origMap);
        String result = ChanPayUtil.sendPost(origMap, BaseConstant.CHARSET, request.getPrivateKey(), changjieUrl);
        logger.info("#[准备调畅捷协议支付还款结果查询的返回结果]-result={}", result);
        return result;
    }
}
