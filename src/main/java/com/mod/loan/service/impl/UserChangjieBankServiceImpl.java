package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ChangjieBindBankCardStatusEnum;
import com.mod.loan.common.enums.ChangjiePayOrRepayOrQueryReturnCodeEnum;
import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.Bank;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.model.request.BindBankCard4ConfirmRequest;
import com.mod.loan.model.request.BindBankCard4SendMsgRequest;
import com.mod.loan.model.request.BindBankCard4UnbindRequest;
import com.mod.loan.service.ChangjieRepayService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserChangjieBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.heli.vo.request.AgreementBindCardValidateCodeVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserChangjieBankServiceImpl extends BaseServiceImpl<UserBank, Long> implements UserChangjieBankService {

    private static Logger log = LoggerFactory.getLogger(UserChangjieBankServiceImpl.class);
    @Autowired
    ChangjieRepayService changjieRepayService;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RedisMapper redisMapper;

    @Override
    public ResultMessage changjieBindBankCard4SendMsg(Long uid, String cardNo, String cardPhone, Bank bank) {
        //获取用户信息
        User user = userService.selectByPrimaryKey(uid);
        if (null == user) {
            return new ResultMessage(ResponseEnum.M4000, "用户信息不存在");
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
            return new ResultMessage(ResponseEnum.M4000, "商户信息异常");
        }
        //每次请求唯一流水号
        String seriesNo = StringUtil.getOrderNumber("c");
        BindBankCard4SendMsgRequest bindBankCard4SendMsgRequest = new BindBankCard4SendMsgRequest();
        bindBankCard4SendMsgRequest.setRequestSeriesNo(seriesNo);
        bindBankCard4SendMsgRequest.setBankCardNo(cardNo);
        bindBankCard4SendMsgRequest.setIdNo(user.getUserCertNo());
        bindBankCard4SendMsgRequest.setName(user.getUserName());
        bindBankCard4SendMsgRequest.setPhone(cardPhone);
        bindBankCard4SendMsgRequest.setPartnerId(merchant.getCjPartnerId());
        bindBankCard4SendMsgRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        bindBankCard4SendMsgRequest.setPublicKey(merchant.getCjPublicKey());
        //调畅捷鉴权绑卡发送验证码请求
        String result = changjieRepayService.bindBankCard4SendMsg(bindBankCard4SendMsgRequest);
        if (null == result) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权绑卡发送验证码请求返回为空");
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //绑卡失败
        if (ChangjieBindBankCardStatusEnum.F.getCode().equals(jsonObject.getString("Status"))) {
            //已绑定
            if (ChangjiePayOrRepayOrQueryReturnCodeEnum.FAIL_QT300008.getCode().equals(jsonObject.getString("AppRetcode"))) {
                //解绑
                BindBankCard4UnbindRequest bindBankCard4UnbindRequest = new BindBankCard4UnbindRequest();
                bindBankCard4UnbindRequest.setCardBegin(cardNo.substring(0, 6));
                bindBankCard4UnbindRequest.setCardEnd(StringUtil.bankTailNo(cardNo));
                bindBankCard4UnbindRequest.setRequestSeriesNo(StringUtil.getOrderNumber("c"));
                bindBankCard4UnbindRequest.setPartnerId(merchant.getCjPartnerId());
                bindBankCard4UnbindRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
                bindBankCard4UnbindRequest.setPublicKey(merchant.getCjPublicKey());
                String res = changjieRepayService.bindBankCard4Unbind(bindBankCard4UnbindRequest);
                if (null == res) {
                    return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权解绑失败");
                }
                JSONObject resObject = JSONObject.parseObject(res);
                if (!ChangjieBindBankCardStatusEnum.S.getCode().equals(resObject.getString("Status"))) {
                    return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权解绑失败");
                }
            }
            return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权绑卡发送验证码失败,请重新获取验证码");
        }
        //缓存绑卡数据
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
    public ResultMessage changjieBindBankCard4Confirm(String validateCode, Long uid, String bindInfo) {
        //转化
        AgreementBindCardValidateCodeVo validateCodeVo = JSON.parseObject(bindInfo, AgreementBindCardValidateCodeVo.class);
        if (validateCodeVo == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        //获取用户信息
        User user = userService.selectByPrimaryKey(uid);
        if (user == null) {
            return new ResultMessage(ResponseEnum.M4000, "用户信息不存在");
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        if (null == merchant || StringUtils.isBlank(merchant.getCjPartnerId()) || StringUtils.isBlank(merchant.getCjPublicKey()) || StringUtils.isBlank(merchant.getCjMerchantPrivateKey())) {
            return new ResultMessage(ResponseEnum.M4000, "商户信息异常");
        }
        //每次请求唯一流水号
        String seriesNo = StringUtil.getOrderNumber("c");
        BindBankCard4ConfirmRequest bindBankCard4ConfirmRequest = new BindBankCard4ConfirmRequest();
        bindBankCard4ConfirmRequest.setRequestSeriesNo(seriesNo);
        bindBankCard4ConfirmRequest.setSeriesNo(validateCodeVo.getP4_orderId());
        bindBankCard4ConfirmRequest.setSmsCode(validateCode);
        bindBankCard4ConfirmRequest.setPartnerId(merchant.getCjPartnerId());
        bindBankCard4ConfirmRequest.setPrivateKey(merchant.getCjMerchantPrivateKey());
        bindBankCard4ConfirmRequest.setPublicKey(merchant.getCjPublicKey());
        //调畅捷鉴权绑卡确认请求
        String result = changjieRepayService.bindBankCard4Confirm(bindBankCard4ConfirmRequest);
        if (null == result) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权绑卡确认请求返回为空");
        }
        //解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(result);
        //绑卡失败
        if (ChangjieBindBankCardStatusEnum.F.getCode().equals(jsonObject.getString("Status"))) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "调鉴权绑卡确认失败");
        }
        //落库
        UserBank userBank = new UserBank();
        userBank.setCardNo(validateCodeVo.getP6_cardNo());
        userBank.setCardPhone(validateCodeVo.getP7_phone());
        userBank.setCardName(validateCodeVo.getBankName());
        userBank.setCardCode(validateCodeVo.getBankCode());
        userBank.setCardStatus(1);
        userBank.setCreateTime(new Date());
        userBank.setUid(uid);
        userBank.setBindType(MerchantEnum.CHANGJIE.getCode());
        userService.insertUserBank(uid, userBank);
        //删除缓存
        redisMapper.remove(RedisConst.user_bank_bind + uid);
        return new ResultMessage(ResponseEnum.M2000);
    }

}
