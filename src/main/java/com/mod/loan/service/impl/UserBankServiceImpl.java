package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuiou.mpay.encrypt.DESCoderFUIOU;
import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.UserBankMapper;
import com.mod.loan.model.Bank;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.*;
import com.mod.loan.util.HttpUtils;
import com.mod.loan.util.MD5;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.service.YeepayService;
import com.mod.loan.util.*;
import com.mod.loan.util.fuyou.util.XMapUtil;
import com.mod.loan.util.fuyou.vo.FuyouBindVo;
import com.mod.loan.util.fuyou.vo.FuyouSmsVo;
import com.mod.loan.util.fuyou.vo.FuyouUnbindVo;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.MessageHandle;
import com.mod.loan.util.heli.vo.request.AgreementBindCardValidateCodeVo;
import com.mod.loan.util.heli.vo.request.BindCardVo;
import com.mod.loan.util.heli.vo.response.AgreementSendValidateCodeResponseVo;
import com.mod.loan.util.heli.vo.response.BindCardResponseVo;
import com.mod.loan.util.helientrusted.vo.MerchantUserUploadResVo;
import com.mod.loan.util.huiju.CreateLinkStringByGet;
import com.mod.loan.util.kuaiqian.KuaiqianPost;
import com.mod.loan.util.kuaiqian.mgw.entity.TransInfo;
import com.mod.loan.util.kuaiqian.mgw.util.ParseUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserBankServiceImpl extends BaseServiceImpl<UserBank, Long> implements UserBankService {

    private static Logger log = LoggerFactory.getLogger(UserBankServiceImpl.class);

    @Autowired
    private UserBankMapper userBankMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    YeepayService yeepayService;
    @Autowired
    private HelipayEntrustedService helipayEntrustedService;

    @Value("${helipay.path:}")
    String helipay_path;
    @Value("${helipay.pfx.pwd:}")
    String helipay_pfx_pwd;
    @Value("${helipay.url:}")
    String helipay_url;
    @Value("${fuiou.bind.smg.url:}")
    String fuiou_bind_smg_url;
    @Value("${fuiou.bind.commit.url:}")
    String fuiou_bind_commit_url;
    @Value("${fuiou.unbind.url:}")
    String fuiou_unbind_url;
    @Value("${huiju.bind.smg.url:}")
    String huiju_bind_smg_url;
    @Value("${huiju.bind.commit.url:}")
    String huiju_bind_commit_url;
    @Value("${yeepay.bind.smg.url:}")
    String yeepay_bind_smg_url;
    @Value("${yeepay.bind.commit.url:}")
    String yeepay_bind_commit_url;
    @Value("${kuaiqian.bind.smg.url:}")
    String kuaiqian_bind_smg_url;
    @Value("${kuaiqian.bind.commit.url:}")
    String kuaiqian_bind_commit_url;

    @Override
    public UserBank selectUserCurrentBankCard(Long uid) {
        return userBankMapper.selectUserCurrentBankCard(uid);
    }

    @Override
    public ResultMessage sendHeliSms(Long uid, String cardNo, String cardPhone, Bank bank) {
        ResultMessage message = null;
        AgreementBindCardValidateCodeVo requestVo = null;
        String response = "";
        try {
            User user = userService.selectByPrimaryKey(uid);
            Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
            requestVo = new AgreementBindCardValidateCodeVo();
            String bindNo = StringUtil.getOrderNumber("c");
            requestVo.setP1_bizType("AgreementPayBindCardValidateCode");
            requestVo.setP2_customerNumber(merchant.getHlb_id());
            requestVo.setP3_userId(user.getId().toString());
            requestVo.setP4_orderId(bindNo);
            requestVo.setP5_timestamp(new DateTime().toString(TimeUtils.dateformat5));
            requestVo.setP6_cardNo(cardNo);
            requestVo.setP7_phone(cardPhone);
            requestVo.setP8_idCardNo(user.getUserCertNo());
            requestVo.setP9_idCardType("IDCARD");
            requestVo.setP10_payerName(user.getUserName());
            requestVo.setP11_isEncrypt("true");
            requestVo.setP12_year("");
            requestVo.setP13_month("");
            requestVo.setP14_cvv2("");
            requestVo.setSignatureType("MD5WITHRSA");
            String pfxPath = helipay_path + merchant.getMerchantAlias() + ".pfx";
            log.info("pfxPath:{}", pfxPath);
            Map handleMap = MessageHandle.getReqestMap(requestVo, pfxPath, helipay_pfx_pwd);
            log.info("sign map:{}", handleMap);
            response = HttpClientService.getHttpResp(handleMap, helipay_url);
            log.info("response:{}", response);
            AgreementSendValidateCodeResponseVo responseVo = JSONObject.parseObject(response,
                    AgreementSendValidateCodeResponseVo.class);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                requestVo.setBankCode(bank.getCode());
                requestVo.setBankName(bank.getBankName());
                redisMapper.set(RedisConst.user_bank_bind + user.getId(), requestVo, Constant.SMS_EXPIRATION_TIME);
                return new ResultMessage(ResponseEnum.M2000);
            } else {
                log.error("合利宝鉴权绑卡短信发送失败，请求参数为={},响应参数为={}", JSON.toJSONString(requestVo), response);
                return new ResultMessage(ResponseEnum.M4000.getCode(), responseVo.getRt3_retMsg());
            }
        } catch (Exception e) {
            log.error("合利宝鉴权绑卡短信发送异常", e);
            log.error("合利宝鉴权绑卡短信发送，请求参数为={},响应参数为={}", JSON.toJSONString(requestVo), response);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    /**
     * 合利宝用户快捷支付绑卡
     */
    @Override
    public ResultMessage bindByHeliSms(String validateCode, Long uid, String bindInfo) {
        AgreementBindCardValidateCodeVo validateCodeVo = JSON.parseObject(bindInfo,
                AgreementBindCardValidateCodeVo.class);
        if (validateCodeVo == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        BindCardVo requestVo = new BindCardVo();
        requestVo.setP1_bizType("QuickPayBindCard");
        requestVo.setP2_customerNumber(validateCodeVo.getP2_customerNumber());
        requestVo.setP3_userId(validateCodeVo.getP3_userId());
        requestVo.setP4_orderId(validateCodeVo.getP4_orderId());
        requestVo.setP5_timestamp(new DateTime().toString(TimeUtils.dateformat5));
        requestVo.setP6_payerName(validateCodeVo.getP10_payerName());
        requestVo.setP7_idCardType(validateCodeVo.getP9_idCardType());
        requestVo.setP8_idCardNo(validateCodeVo.getP8_idCardNo());
        requestVo.setP9_cardNo(validateCodeVo.getP6_cardNo());
        requestVo.setP10_year("");
        requestVo.setP11_month("");
        requestVo.setP12_cvv2("");
        requestVo.setP13_phone(validateCodeVo.getP7_phone());
        requestVo.setP14_validateCode(validateCode);
        requestVo.setP15_isEncrypt("true");
        requestVo.setSignatureType("MD5WITHRSA");
        String response = "";
        try {
            String pfxPath = helipay_path + merchant.getMerchantAlias() + ".pfx";
            Map handleMap = MessageHandle.getReqestMap(requestVo, pfxPath, helipay_pfx_pwd);
            response = HttpClientService.getHttpResp(handleMap, helipay_url);
            BindCardResponseVo responseVo = JSONObject.parseObject(response, BindCardResponseVo.class);
            if ("0000".equals(responseVo.getRt2_retCode())) {
                //保存银行卡信息
                saveUserBank(validateCodeVo, uid, responseVo.getRt10_bindId());
                //委托代付发送商户用户注册mq消息
                if (StringUtils.isNotEmpty(merchant.getHlbEntrustedSignKey())
                        && StringUtils.isNotEmpty(merchant.getHlbEntrustedPrivateKey())) {
                    MerchantUserUploadResVo resVo = helipayEntrustedService.bindUserCard(uid, merchant.getMerchantAlias());
                    if (resVo != null && "0000".equals(resVo.getRt2_retCode())) {
                        redisMapper.remove(RedisConst.user_bank_bind + uid);
                        return new ResultMessage(ResponseEnum.M2000);
                    }
                    return new ResultMessage(ResponseEnum.M4000, resVo.getRt3_retMsg());
                } else {
                    redisMapper.remove(RedisConst.user_bank_bind + uid);
                    return new ResultMessage(ResponseEnum.M2000);
                }
            } else if ("00000000".equals(responseVo.getRt2_retCode())) {
                return new ResultMessage(ResponseEnum.M2000);
            } else {
                log.error("合利宝绑卡失败,params={},result={}", JSON.toJSONString(requestVo), response);
                return new ResultMessage(ResponseEnum.M4000.getCode(), responseVo.getRt3_retMsg());
            }
        } catch (Exception e) {
            log.error("合利宝绑卡异常", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 保存银行卡信息
     */
    private void saveUserBank(AgreementBindCardValidateCodeVo validateCodeVo, Long uid, String bindId) {
        UserBank userBank = new UserBank();
        userBank.setCardCode(validateCodeVo.getBankCode());
        userBank.setCardName(validateCodeVo.getBankName());
        userBank.setCardNo(validateCodeVo.getP6_cardNo());
        userBank.setCardPhone(validateCodeVo.getP7_phone());
        userBank.setCardStatus(1);
        userBank.setCreateTime(new Date());
        userBank.setForeignId(bindId);
        userBank.setUid(uid);
        userBank.setBindType(MerchantEnum.helibao.getCode());
        userService.insertUserBank(uid, userBank);
    }

    @Override
    public ResultMessage sendFuyouSms(Long uid, String cardNo, String cardPhone, Bank bank) {
        ResultMessage message = null;
        String result = "";
        Map<String, String> map = new HashMap<>();
        AgreementBindCardValidateCodeVo requestVo = null;
        try {
            User user = userService.selectByPrimaryKey(uid);
            Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
            String seriesNo = StringUtil.getOrderNumber("c");
            String unsign = "1.0" + "|" + seriesNo + "|" + merchant.getFuyou_merid() + "|" + uid + "|"
                    + user.getUserName() + "|" + cardNo + "|" + "0" + "|" + user.getUserCertNo() + "|" + cardPhone + "|"
                    + merchant.getFuyou_h5key();
            String sign = MD5.toMD5(unsign, "utf-8");
            FuyouSmsVo fuyouSmsVo = new FuyouSmsVo();
            fuyouSmsVo.setVersion("1.0");
            fuyouSmsVo.setTradeDate(new DateTime().toString(TimeUtils.dateformat4));
            fuyouSmsVo.setMchntSsn(seriesNo);
            fuyouSmsVo.setMchntCd(merchant.getFuyou_merid());
            fuyouSmsVo.setUserId(uid.toString());
            fuyouSmsVo.setIdCard(user.getUserCertNo());
            fuyouSmsVo.setIdType("0");
            fuyouSmsVo.setAccount(user.getUserName());
            fuyouSmsVo.setCardNo(cardNo);
            fuyouSmsVo.setMobileNo(cardPhone);
            fuyouSmsVo.setSign(sign);
            fuyouSmsVo.setCvn("");
            String xml = XMapUtil.toXML(fuyouSmsVo, "utf-8");
            map.put("MCHNTCD", merchant.getFuyou_merid());
            map.put("APIFMS", DESCoderFUIOU.desEncrypt(xml, DESCoderFUIOU.getKeyLength8(merchant.getFuyou_h5key())));
            result = HttpUtils.doPost(fuiou_bind_smg_url, map);
            result = DESCoderFUIOU.desDecrypt(result, DESCoderFUIOU.getKeyLength8(merchant.getFuyou_h5key()));
            Document document = DocumentHelper.parseText(result);
            String code = document.selectSingleNode("/RESPONSE/RESPONSECODE").getStringValue();
            String msg = document.selectSingleNode("/RESPONSE/RESPONSEMSG").getStringValue();

            if ("323P".equals(code)) {
                UserBank userBank = userBankMapper.selectFuyouBankCard(uid);
                ResultMessage resultMessage = unbindByFuyou(uid, userBank.getForeignId(), merchant);
                if (!"0000".equals(resultMessage.getStatus())) {
                    return new ResultMessage(ResponseEnum.M4000.getCode(), "请联系客服人员！");
                }
                return new ResultMessage(ResponseEnum.M4000.getCode(), "请重新获取验证码！");
            }

            if ("0000".equals(code)) {
                requestVo = new AgreementBindCardValidateCodeVo();
                requestVo.setP2_customerNumber(merchant.getFuyou_merid() + "|" + merchant.getFuyou_h5key());
                requestVo.setP3_userId(user.getId().toString());
                requestVo.setP4_orderId(seriesNo);
                requestVo.setP6_cardNo(cardNo);
                requestVo.setP7_phone(cardPhone);
                requestVo.setP8_idCardNo(user.getUserCertNo());
                requestVo.setP10_payerName(user.getUserName());
                requestVo.setBankName(bank.getBankName());
                requestVo.setBankCode(bank.getCode());
                redisMapper.set(RedisConst.user_bank_bind + user.getId(), requestVo, Constant.SMS_EXPIRATION_TIME);
                return new ResultMessage(ResponseEnum.M2000);
            }
            log.error("富友鉴权绑卡短信发送失败，请求参数为={},响应参数为={}", xml, result);
            message = new ResultMessage(ResponseEnum.M4000.getCode(), msg);
        } catch (Exception e) {
            log.error("富友鉴权绑卡短信发送异常", e);
            log.error("富友鉴权绑卡短信发送异常，请求参数为={},响应参数为={}", map, result);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    @Override
    public ResultMessage bindByFuyouSms(String validateCode, Long uid, String bindInfo) {
        ResultMessage message = null;
        String result = "";
        Map<String, String> map = new HashMap<>();
        AgreementBindCardValidateCodeVo validateCodeVo = JSON.parseObject(bindInfo,
                AgreementBindCardValidateCodeVo.class);
        if (validateCodeVo == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        String[] merInfo = validateCodeVo.getP2_customerNumber().split("\\|");
        try {
            String unsign = "1.0" + "|" + validateCodeVo.getP4_orderId() + "|" + merInfo[0] + "|" + uid + "|"
                    + validateCodeVo.getP10_payerName() + "|" + validateCodeVo.getP6_cardNo() + "|" + "0" + "|"
                    + validateCodeVo.getP8_idCardNo() + "|" + validateCodeVo.getP7_phone() + "|" + validateCode + "|"
                    + merInfo[1];
            String sign = MD5.toMD5(unsign, "utf-8");
            FuyouBindVo fuyouBindVo = new FuyouBindVo();
            fuyouBindVo.setVersion("1.0");
            fuyouBindVo.setTradeDate(new DateTime().toString(TimeUtils.dateformat4));
            fuyouBindVo.setMchntSsn(validateCodeVo.getP4_orderId());
            fuyouBindVo.setMchntCd(merInfo[0]);
            fuyouBindVo.setUserId(uid.toString());
            fuyouBindVo.setIdCard(validateCodeVo.getP8_idCardNo());
            fuyouBindVo.setIdType("0");
            fuyouBindVo.setAccount(validateCodeVo.getP10_payerName());
            fuyouBindVo.setCardNo(validateCodeVo.getP6_cardNo());
            fuyouBindVo.setMobileNo(validateCodeVo.getP7_phone());
            fuyouBindVo.setMsgCode(validateCode);
            fuyouBindVo.setSign(sign);
            fuyouBindVo.setCvn("");
            String xml = XMapUtil.toXML(fuyouBindVo, "utf-8");
            map.put("MCHNTCD", merInfo[0]);
            map.put("APIFMS", DESCoderFUIOU.desEncrypt(xml, DESCoderFUIOU.getKeyLength8(merInfo[1])));
            result = HttpUtils.doPost(fuiou_bind_commit_url, map);
            result = DESCoderFUIOU.desDecrypt(result, DESCoderFUIOU.getKeyLength8(merInfo[1]));
            Document document = DocumentHelper.parseText(result);
            String code = document.selectSingleNode("/RESPONSE/RESPONSECODE").getStringValue();
            String msg = document.selectSingleNode("/RESPONSE/RESPONSEMSG").getStringValue();
            String protocolNo = document.selectSingleNode("/RESPONSE/PROTOCOLNO").getStringValue();
            if ("0000".equals(code)) {
                UserBank userBank = new UserBank();
                userBank.setCardCode(validateCodeVo.getBankCode());
                userBank.setCardName(validateCodeVo.getBankName());
                userBank.setCardNo(validateCodeVo.getP6_cardNo());
                userBank.setCardPhone(validateCodeVo.getP7_phone());
                userBank.setCardStatus(1);
                userBank.setCreateTime(new Date());
                userBank.setUid(uid);
                userBank.setBindType(MerchantEnum.fuyou.getCode());
                userBank.setForeignId(protocolNo);
                userService.insertUserBank(uid, userBank);
                redisMapper.remove(RedisConst.user_bank_bind + uid);
                return new ResultMessage(ResponseEnum.M2000);
            }
            log.error("富友绑卡失败,params={},result={}", xml, result);
            message = new ResultMessage(ResponseEnum.M4000.getCode(), msg);
        } catch (Exception e) {
            log.error("富友绑卡异常", e);
            log.error("富友绑卡异常，请求参数为={},响应参数为={}", map, result);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    @Override
    public ResultMessage unbindByFuyou(Long uid, String protocolNo, Merchant merchant) {
        ResultMessage message = null;
        Map<String, String> map = new HashMap<>();
        String result = "";
        try {
            String unsign = "1.0" + "|" + merchant.getFuyou_merid() + "|" + uid + "|" + protocolNo + "|" + merchant.getFuyou_h5key();
            String sign = MD5.toMD5(unsign, "utf-8");
            FuyouUnbindVo fuyouUnbindVo = new FuyouUnbindVo();
            fuyouUnbindVo.setVersion("1.0");
            fuyouUnbindVo.setMchntCd(merchant.getFuyou_merid());
            fuyouUnbindVo.setUserId(uid.toString());
            fuyouUnbindVo.setProtocolNo(protocolNo);
            fuyouUnbindVo.setSign(sign);
            String xml = XMapUtil.toXML(fuyouUnbindVo, "utf-8");
            map.put("MCHNTCD", merchant.getFuyou_merid());
            map.put("APIFMS", DESCoderFUIOU.desEncrypt(xml, DESCoderFUIOU.getKeyLength8(merchant.getFuyou_h5key())));
            result = HttpUtils.doPost(fuiou_unbind_url, map);
            result = DESCoderFUIOU.desDecrypt(result, DESCoderFUIOU.getKeyLength8(merchant.getFuyou_h5key()));
            Document document = DocumentHelper.parseText(result);
            String code = document.selectSingleNode("/RESPONSE/RESPONSECODE").getStringValue();
            String msg = document.selectSingleNode("/RESPONSE/RESPONSEMSG").getStringValue();
            if (!"0000".equals(code)) {
                log.info("解绑失败，xml={},result={}", xml, result);
                return new ResultMessage(code, msg);
            }
            return new ResultMessage(code, msg);

        } catch (Exception e) {
            log.error("富友解绑异常", e);
            log.error("富友解绑异常，请求参数为={},响应参数为={}", map, result);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    @Override
    public ResultMessage sendHuijuSms(Long uid, String cardNo, String cardPhone, Bank bank) {
        Map<String, String> map = null;
        String response = null;
        Map result = null;
        try {
            AgreementBindCardValidateCodeVo requestVo = null;
            User user = userService.selectByPrimaryKey(uid);
            Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
            String seriesNo = StringUtil.getOrderNumber("c");
            map = new HashMap<String, String>();
            map.put("p0_Version", "2.0");
            map.put("p1_MerchantNo", merchant.getHuiju_id());
            map.put("p2_MerchantName", "汇聚");
            map.put("q1_OrderNo", seriesNo);
            map.put("q2_Amount", "0.1");
            map.put("q7_NotifyUrl", Constant.HUIJU_NOTIFY_URL);
            map.put("q8_FrpCode", "FAST");
            map.put("s1_PayerName", user.getUserName());
            map.put("s2_PayerCardType", "1");
            map.put("s3_PayerCardNo", user.getUserCertNo());
            map.put("s4_PayerBankCardNo", cardNo);
            map.put("s5_BankCardExpire", ""); /** 信用卡的有效期：必须是YYYY-MM格式 */
            map.put("s6_CVV2", ""); /** 信用卡CVV2 */
            map.put("s7_BankMobile", cardPhone);

            String unsign = CreateLinkStringByGet.createLinkStringByGet(map);
            String sign = MD5.toMD5(unsign + merchant.getHuiju_md5_key(), "utf-8").toUpperCase();
            map.put("hmac", sign);
            response = HttpUtils.doPost(huiju_bind_smg_url, map);
            result = JSONObject.parseObject(response);
            String returnHmac = (String) result.remove("hmac");
            String Strmap = CreateLinkStringByGet.createLinkStringByGet(result);
            if (!MD5.toMD5(Strmap + merchant.getHuiju_md5_key(), "utf-8").toUpperCase().equals(returnHmac)) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "发送短信失败");
            }
            if ("100".equals(result.get("ra_Status").toString())) {
                requestVo = new AgreementBindCardValidateCodeVo();
                requestVo.setP2_customerNumber(merchant.getHuiju_id() + "|" + merchant.getHuiju_md5_key());
                requestVo.setP3_userId(user.getId().toString());
                requestVo.setP4_orderId(seriesNo);
                requestVo.setP6_cardNo(cardNo);
                requestVo.setP7_phone(cardPhone);
                requestVo.setP8_idCardNo(user.getUserCertNo());
                requestVo.setP10_payerName(user.getUserName());
                requestVo.setBankName(bank.getBankName());
                requestVo.setBankCode(bank.getCode());
                redisMapper.set(RedisConst.user_bank_bind + user.getId(), requestVo, Constant.SMS_EXPIRATION_TIME);
                return new ResultMessage(ResponseEnum.M2000.getCode(), result.get("rb_Msg").toString());
            }
            log.error("汇聚鉴权绑卡短信发送失败，请求参数为={},响应参数为={}", JSON.toJSONString(map), response);
            return new ResultMessage(ResponseEnum.M4000.getCode(), result.get("rb_Msg").toString());
        } catch (Exception e) {
            log.error("汇聚鉴权绑卡短信发送异常", e);
            log.error("汇聚鉴权绑卡短信发送异常，请求参数为={},响应参数为={}", map, response);
        }
        return new ResultMessage(ResponseEnum.M4000);
    }

    @Override
    public ResultMessage bindByHuijuSms(String validateCode, Long uid, String bindInfo) {
        Map<String, String> map = null;
        String response = null;
        Map result = null;
        try {
            AgreementBindCardValidateCodeVo validateCodeVo = JSON.parseObject(bindInfo,
                    AgreementBindCardValidateCodeVo.class);
            if (validateCodeVo == null) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
            }
            String[] merInfo = validateCodeVo.getP2_customerNumber().split("\\|");
            map = new HashMap<String, String>();
            map.put("p0_Version", "2.0");
            map.put("p1_MerchantNo", merInfo[0]);
            map.put("p2_MerchantName", "汇聚");
            map.put("q1_OrderNo", validateCodeVo.getP4_orderId());
            map.put("q2_Amount", "0.1");
            map.put("s1_PayerName", validateCodeVo.getP10_payerName());
            map.put("s2_PayerCardType", "1");
            map.put("s3_PayerCardNo", validateCodeVo.getP8_idCardNo());
            map.put("s4_PayerBankCardNo", validateCodeVo.getP6_cardNo());
            map.put("s5_BankCardExpire", "");
            map.put("s6_CVV2", "");
            map.put("s7_BankMobile", validateCodeVo.getP7_phone());
            map.put("t2_SmsCode", validateCode);
            map.put("q8_FrpCode", "FAST");

            String unsign = CreateLinkStringByGet.createLinkStringByGet(map);
            String sign = MD5.toMD5(unsign + merInfo[1], "utf-8").toUpperCase();
            map.put("hmac", sign);
            response = HttpUtils.doPost(huiju_bind_commit_url, map);
            result = JSONObject.parseObject(response);
            String returnHmac = (String) result.remove("hmac");
            String Strmap = CreateLinkStringByGet.createLinkStringByGet(result);
            if (!MD5.toMD5(Strmap + merInfo[1], "utf-8").toUpperCase().equals(returnHmac)) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "绑卡失败");
            }
            if ("100".equals(result.get("ra_Status").toString())) {
                UserBank userBank = new UserBank();
                userBank.setCardCode(validateCodeVo.getBankCode());
                userBank.setCardName(validateCodeVo.getBankName());
                userBank.setCardNo(validateCodeVo.getP6_cardNo());
                userBank.setCardPhone(validateCodeVo.getP7_phone());
                userBank.setCardStatus(1);
                userBank.setCreateTime(new Date());
                userBank.setUid(uid);
                userBank.setBindType(MerchantEnum.huiju.getCode());
                userService.insertUserBank(uid, userBank);
                redisMapper.remove(RedisConst.user_bank_bind + uid);
                return new ResultMessage(ResponseEnum.M2000.getCode(), result.get("rb_Msg").toString());
            }
            log.error("汇聚绑卡失败,params={},result={}", JSON.toJSONString(map), response);
            return new ResultMessage(ResponseEnum.M4000.getCode(), result.get("rb_Msg").toString());
        } catch (Exception e) {
            log.error("汇聚绑卡异常", e);
            log.info("汇聚绑卡异常，请求参数为={},响应参数为={}", map, response);
        }
        return new ResultMessage(ResponseEnum.M4000);
    }

    @Override
    public ResultMessage sendYeepaySms(Long uid, String cardNo, String cardPhone, Bank bank) {
        User user = userService.selectByPrimaryKey(uid);
        if (user == null) {
            return new ResultMessage(ResponseEnum.M4000, "用户信息不存在");
        }

        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        String seriesNo = StringUtil.getOrderNumber("c");
        String err;
        try {
            err = yeepayService.authBindCardRequest(DesUtil.decryption(merchant.getYeepay_repay_appkey()),
                    DesUtil.decryption(merchant.getYeepay_repay_private_key()),
                    seriesNo, String.valueOf(uid), cardNo, user.getUserCertNo(), user.getUserName(), cardPhone);
        } catch (Exception e) {
            log.error("易宝绑卡发送短信异常uid={}, error={}", uid, e);
            err="sendYeepaySms Exception";
        }

        if (StringUtils.isNotEmpty(err)) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), err);
        }

        AgreementBindCardValidateCodeVo requestVo = new AgreementBindCardValidateCodeVo();
        requestVo.setP4_orderId(seriesNo);
        requestVo.setP6_cardNo(cardNo);
        requestVo.setP7_phone(cardPhone);
        requestVo.setBankName(bank.getBankName());
        requestVo.setBankCode(bank.getCode());
        redisMapper.set(RedisConst.user_bank_bind + user.getId(), requestVo, Constant.SMS_EXPIRATION_TIME);

        return new ResultMessage(ResponseEnum.M2000);
    }

    @Override
    public ResultMessage bindYeepaySms(String validateCode, Long uid, String bindInfo) {
        AgreementBindCardValidateCodeVo validateCodeVo = JSON.parseObject(bindInfo, AgreementBindCardValidateCodeVo.class);
        if (validateCodeVo == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        User user = userService.selectByPrimaryKey(uid);
        if (user == null) {
            return new ResultMessage(ResponseEnum.M4000, "用户信息不存在");
        }

		Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        String err;
        try {
            err = yeepayService.authBindCardConfirm(DesUtil.decryption(merchant.getYeepay_repay_appkey()),
                    DesUtil.decryption(merchant.getYeepay_repay_private_key()), validateCodeVo.getP4_orderId(), validateCode);
        } catch (Exception e) {
            log.error("易宝绑卡确认异常uid={}, error={}", uid, e);
            err="bindYeepaySms Exception";
        }

        if (err!=null){
            return new ResultMessage(ResponseEnum.M4000, err);
        }

        UserBank userBank = new UserBank();
        userBank.setCardNo(validateCodeVo.getP6_cardNo());
        userBank.setCardPhone(validateCodeVo.getP7_phone());
        userBank.setCardName(validateCodeVo.getBankName());
        userBank.setCardCode(validateCodeVo.getBankCode());
        userBank.setCardStatus(1);
        userBank.setCreateTime(new Date());
        userBank.setUid(uid);
        userBank.setBindType(MerchantEnum.yeepay.getCode());
        userService.insertUserBank(uid, userBank);
        redisMapper.remove(RedisConst.user_bank_bind + uid);

        return new ResultMessage(ResponseEnum.M2000);
    }

    @Override
    public ResultMessage sendKuaiqianSms(Long uid, String cardNo, String cardPhone, Bank bank) {
        ResultMessage message = null;
        StringBuffer sb = new StringBuffer();
        HashMap respMap = null;
        try {
            User user = userService.selectByPrimaryKey(uid);
            Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
            TransInfo transInfo = new TransInfo();
            //设置手机动态鉴权节点
            transInfo.setRecordeText_1("indAuthContent");
            transInfo.setRecordeText_2("ErrorMsgContent");
            //版本号
            String version = "1.0";
            //商户编号
            String merchantId = merchant.getKqMerchantId();
            //终端编号
            String terminalId = merchant.getKqTerminalId();
            //外部跟踪编号
            String externalRefNumber = StringUtil.getOrderNumber("c");
            //卡号
            String pan = cardNo;
            //手机号码
            String phoneNO = cardPhone;
            //客户号
            String customerId = phoneNO +  "_" + String.valueOf(uid);
            //持卡人户名
            String cardHolderName = user.getUserName();
            //证件号码
            String cardHolderId = user.getUserCertNo();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                    .append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">")
                    .append("<version>").append(version).append("</version>")
                    .append("<indAuthContent>")
                    .append("<merchantId>").append(merchantId).append("</merchantId>")
                    .append("<terminalId>").append(terminalId).append("</terminalId>")
                    .append("<customerId>").append(customerId).append("</customerId>")
                    .append("<externalRefNumber>").append(externalRefNumber).append("</externalRefNumber>")
                    .append("<pan>").append(pan).append("</pan>")
                    .append("<cardHolderName>").append(cardHolderName).append("</cardHolderName>")
                    .append("<idType>").append(0).append("</idType>")
                    .append("<cardHolderId>").append(cardHolderId).append("</cardHolderId>")
                    .append("<phoneNO>").append(phoneNO).append("</phoneNO>")
                    .append("<bindType>").append(0).append("</bindType>")
                    .append("</indAuthContent>")
                    .append("</MasMessage>");
            respMap = KuaiqianPost.sendPost(merchant.getKqCertPath(), merchant.getKqCertPwd(), merchantId, kuaiqian_bind_smg_url, sb.toString(), transInfo);
            if("00".equals(MapUtils.getString(respMap, "responseCode"))) {
                //请求参数格式转成map
                ParseUtil parseUtil = new ParseUtil();
                HashMap reqMap = ParseUtil.parseXML(sb.toString(), transInfo);
                reqMap.put("cardCode", bank.getCode());
                reqMap.put("cardName", bank.getBankName());
                reqMap.put("token", MapUtils.getString(respMap, "token"));
                redisMapper.set(RedisConst.user_bank_bind + user.getId(), reqMap, Constant.SMS_EXPIRATION_TIME);
                return new ResultMessage(ResponseEnum.M2000);
            } else {
                log.error("快钱鉴权绑卡短信发送失败，请求参数为={},响应参数为={}", sb.toString(), JSON.toJSON(respMap));
                return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
            }
        } catch (Exception e) {
            log.error("快钱鉴权绑卡短信发送异常", e);
            log.error("快钱鉴权绑卡短信发送，请求参数为={},响应参数为={}", sb.toString(), JSON.toJSON(respMap));
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    @Override
    public ResultMessage bindKuaiqianSms(String validateCode, Long uid, String bindInfo) {
        ResultMessage message = null;
        StringBuffer sb = new StringBuffer();
        HashMap respMap = null;
        try {
            Map reqMap = JSON.parseObject(bindInfo, Map.class);
            if (MapUtils.isEmpty(reqMap)) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
            }
            Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
            //设置手机动态鉴权节点
            TransInfo transInfo= new TransInfo();
            transInfo.setRecordeText_1("indAuthDynVerifyContent");
            transInfo.setRecordeText_2("ErrorMsgContent");
            //版本号
            String version = "1.0";
            //商户编号
            String merchantId = merchant.getKqMerchantId();
            //终端编号
            String terminalId = merchant.getKqTerminalId();
            //外部跟踪编号
            String externalRefNumber = MapUtils.getString(reqMap, "externalRefNumber");
            //卡号
            String pan = MapUtils.getString(reqMap, "pan");
            //手机号码
            String phoneNO = MapUtils.getString(reqMap, "phoneNO");
            //客户号
            String customerId = phoneNO +  "_" + String.valueOf(uid);
            //鉴权返回token
            String token = MapUtils.getString(reqMap, "token");
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                    .append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">")
                    .append("<version>").append(version).append("</version>")
                    .append("<indAuthDynVerifyContent>")
                    .append("<merchantId>").append(merchantId).append("</merchantId>")
                    .append("<terminalId>").append(terminalId).append("</terminalId>")
                    .append("<customerId>").append(customerId).append("</customerId>")
                    .append("<externalRefNumber>").append(externalRefNumber).append("</externalRefNumber>")
                    .append("<pan>").append(pan).append("</pan>")
                    .append("<validCode>").append(validateCode).append("</validCode>")
                    .append("<token>").append(token).append("</token>")
                    .append("<phoneNO>").append(phoneNO).append("</phoneNO>")
                    .append("</indAuthDynVerifyContent>")
                    .append("</MasMessage>");
            respMap = KuaiqianPost.sendPost(merchant.getKqCertPath(), merchant.getKqCertPwd(), merchantId, kuaiqian_bind_commit_url, sb.toString(), transInfo);
            if("00".equals(MapUtils.getString(respMap, "responseCode"))) {
                UserBank userBank = new UserBank();
                userBank.setCardCode(MapUtils.getString(reqMap, "cardCode"));
                userBank.setCardName(MapUtils.getString(reqMap, "cardName"));
                userBank.setCardNo(MapUtils.getString(reqMap, "pan"));
                userBank.setCardPhone(MapUtils.getString(reqMap, "phoneNO"));
                userBank.setCardStatus(1);
                userBank.setCreateTime(new Date());
                userBank.setUid(uid);
                userBank.setForeignId(MapUtils.getString(respMap, "payToken"));
                userBank.setBindType(MerchantEnum.kuaiqian.getCode());
                userService.insertUserBank(uid, userBank);
                redisMapper.remove(RedisConst.user_bank_bind + uid);
                return new ResultMessage(ResponseEnum.M2000);
            } else {
                log.error("快钱绑卡失败,params={},result={}", sb.toString(), JSON.toJSON(respMap));
                return new ResultMessage(ResponseEnum.M4000.getCode(), MapUtils.getString(respMap, "responseTextMessage"));
            }
        } catch (Exception e) {
            log.error("快钱绑卡异常", e);
            message = new ResultMessage(ResponseEnum.M4000);
        }
        return message;
    }

    @Override
    public UserBank selectUserMerchantBankCard(Long uid, Integer bindType) {
        return userBankMapper.selectUserMerchantBankCard(uid, bindType);
    }
}
