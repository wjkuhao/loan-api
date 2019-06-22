package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.HelipayEntrustedService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.aliyun.OSSUtil;
import com.mod.loan.util.heli.HttpClientService;
import com.mod.loan.util.heli.util.HeliPayBeanUtils;
import com.mod.loan.util.heli.util.HeliPayUtils;
import com.mod.loan.util.helientrusted.Des3Encryption;
import com.mod.loan.util.helientrusted.HelipayConstant;
import com.mod.loan.util.helientrusted.RSA;
import com.mod.loan.util.helientrusted.vo.MerchantUserResVo;
import com.mod.loan.util.helientrusted.vo.MerchantUserUploadResVo;
import com.mod.loan.util.helientrusted.vo.MerchantUserUploadVo;
import com.mod.loan.util.helientrusted.vo.MerchantUserVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HelipayEntrustedServiceImpl implements HelipayEntrustedService {

    private static final Logger logger = LoggerFactory.getLogger(HelipayEntrustedServiceImpl.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 绑定用户到委托代付商户端
     */
    @Override
    public MerchantUserUploadResVo bindUserCard(Long uid, String merchantAlias) {
        User user = userService.selectByPrimaryKey(uid);
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        Merchant merchant = merchantService.findMerchantByAlias(merchantAlias);
        return bindUserCard(user, userBank, merchant);
    }

    /**
     * 绑定用户到委托代付商户端
     */
    @Override
    public MerchantUserUploadResVo bindUserCard(User user, UserBank userBank, Merchant merchant) {
        MerchantUserUploadResVo uploadResVo = new MerchantUserUploadResVo();
        try {
            //step 1.用户注册
            MerchantUserResVo userResVo = userRegister(user, userBank, merchant);
            if (userResVo != null && "0000".equals(userResVo.getRt2_retCode())) {
                //step 2.用户资料上传
                uploadResVo = userUpload(user, merchant, userResVo.getRt6_userId(), HeliPayUtils.getOrderId(user.getId().toString()));
                if (uploadResVo != null && "0000".equals(uploadResVo.getRt2_retCode())) {
                    //将客户cuid存入数据库
                    userBank.setHlbEntrustedCuid(userResVo.getRt6_userId());
                    userBankService.updateByPrimaryKey(userBank);
                    logger.info("bindUserCard用户注册成功:{}", user.getId().toString());
                }
            }
        } catch (Exception e) {
            uploadResVo.setRt2_retCode("-1");
            uploadResVo.setRt3_retMsg("bindUserCard失败:" + e.getMessage());
            logger.error("bindUserCard失败", e);
        }
        return uploadResVo;
    }

    /**
     * 商户用户注册
     */
    private MerchantUserResVo userRegister(User user, UserBank userBank, Merchant merchant) {
        MerchantUserResVo merchantUserResVo = new MerchantUserResVo();
        try {
            MerchantUserVo userVo = new MerchantUserVo();
            //交易类型
            userVo.setP1_bizType("MerchantUserRegister");
            //商户编号
            userVo.setP2_customerNumber(merchant.getHlb_id());
            //商户订单号
            userVo.setP3_orderId(HeliPayUtils.getOrderId(user.getId().toString()));
            //姓名
            userVo.setP4_legalPerson(user.getUserName());
            //身份证号
            userVo.setP5_legalPersonID(user.getUserCertNo());
            //手机号
            userVo.setP6_mobile(userBank.getCardPhone());
            //对公对私
            userVo.setP7_business(HelipayConstant.BUSSINESS);
            //时间戳
            userVo.setP8_timestamp(HeliPayUtils.getTimestamp());
            //信息域
            userVo.setP9_ext(new JSONObject().toJSONString());

            //信息域加密
            if (StringUtils.isNotBlank(userVo.getP5_legalPersonID())) {
                userVo.setP5_legalPersonID(Des3Encryption.encode(HelipayConstant.deskey_key, userVo.getP5_legalPersonID()));
            }
            if (StringUtils.isNotBlank(userVo.getP6_mobile())) {
                userVo.setP6_mobile(Des3Encryption.encode(HelipayConstant.deskey_key, userVo.getP6_mobile()));
            }
            Map<String, String> map = HeliPayBeanUtils.convertBean(userVo, new LinkedHashMap());
            String oriMessage = HeliPayBeanUtils.getSigned(map, null);
            logger.info("签名原文串：" + oriMessage);
            String sign = RSA.sign(oriMessage.trim(), RSA.getPrivateKey(merchant.getHlbEntrustedPrivateKey()));
            logger.info("签名串：" + sign);
            map.put("sign", sign);
            logger.info("发送参数：" + map);
            String result = HttpClientService.getHttpResp(map, Constant.HELIPAY_ENTRUSTED_URL);
            //响应结果：{"rt6_userId":"U1702451476","rt2_retCode":"0000","sign":"18884a6ddb5d0a827684db30d2de172b","rt1_bizType":"MerchantUserRegister","rt5_orderId":"p201905301446111","rt4_customerNumber":"C1800685715","rt3_retMsg":"请求成功","rt7_userStatus":"INIT"}
            logger.info("响应结果：" + result);
            merchantUserResVo = JSONObject.parseObject(result, MerchantUserResVo.class);
            if ("0000".equals(merchantUserResVo.getRt2_retCode())) {
                logger.info("用户注册成功:" + merchantUserResVo.getRt3_retMsg());
            } else {
                logger.info("用户注册失败:" + merchantUserResVo.getRt3_retMsg());
            }
        } catch (Exception e) {
            logger.error("合利宝委托代付用户注册失败:", e);
            merchantUserResVo.setRt2_retCode("-1");
            merchantUserResVo.setRt3_retMsg("合利宝委托代付用户注册失败:" + e.getMessage());
        }
        return merchantUserResVo;
    }


    /**
     * 用户资料上传
     */
    private MerchantUserUploadResVo userUpload(User user, Merchant merchant, String userRegId, String orderId) {
        //step 1.身份证正面
        MerchantUserUploadResVo resVo = userFileUpload(user.getImgCertFront(), userRegId, "FRONT_OF_ID_CARD", merchant, orderId);
        if ("0000".equals(resVo.getRt2_retCode())) {
            //step 2.身份证反面
            resVo = userFileUpload(user.getImgCertBack(), userRegId, "BACK_OF_ID_CARD", merchant, orderId);
        }
        return resVo;
    }

    /**
     * 用户资料上传
     */
    private MerchantUserUploadResVo userFileUpload(String certFile, String userRegId, String certType, Merchant merchant, String orderId) {
        MerchantUserUploadResVo uploadResVo = new MerchantUserUploadResVo();
        try {
            MerchantUserUploadVo userVo = new MerchantUserUploadVo();
            //交易类型
            userVo.setP1_bizType("UploadCredential");
            //商户编号
            userVo.setP2_customerNumber(merchant.getHlb_id());
            //商户订单号
            userVo.setP3_orderId(orderId);
            //用户编号
            userVo.setP4_userId(userRegId);
            //时间戳
            userVo.setP5_timestamp(HeliPayUtils.getTimestamp());
            //证件类型
            userVo.setP6_credentialType(certType);
            String fileName = certFile.substring(certFile.lastIndexOf("/") + 1);
            MultipartFile file = new MockMultipartFile(fileName, fileName, "", OSSUtil.getCertImage(certFile));
            File tempFile = new File(HelipayConstant.tempDir, file.getOriginalFilename());
            file.transferTo(tempFile);
            // 文件签名
            try (InputStream is = new FileInputStream(tempFile)) {
                userVo.setP7_fileSign(DigestUtils.md5DigestAsHex(is));
            }
            Map<String, String> map = HeliPayBeanUtils.convertBean(userVo, new LinkedHashMap());
            String oriMessage = HeliPayBeanUtils.getSigned(map, null);
            logger.info("签名原文串：" + oriMessage);
            String sign = RSA.sign(oriMessage.trim(), RSA.getPrivateKey(merchant.getHlbEntrustedPrivateKey()));
            logger.info("签名串：" + sign);
            map.put("sign", sign);
            logger.info("发送参数：" + map);
            String result = HttpClientService.getHttpResp(map, Constant.HELIPAY_ENTRUSTED_FILE_URL, tempFile);
            logger.info("响应结果：" + result);
            //{response={"rt6_userId":"","rt2_retCode":"1024","sign":"f4855210123cfaa21faf45d4e72cf25b","rt1_bizType":"UploadCredential","rt5_orderId":"p201905311018541","rt8_desc":"","rt4_customerNumber":"C1800685715","rt3_retMsg":"未找到该用户","rt7_credentialStatus":""}, statusCode=200}
            uploadResVo = JSONObject.parseObject(result, MerchantUserUploadResVo.class);
            String assemblyRespOriSign = HeliPayBeanUtils.getSigned(uploadResVo, null);
            logger.info("组装返回结果签名串：" + assemblyRespOriSign);
            String responseSign = uploadResVo.getSign();
            logger.info("响应签名：" + responseSign);
            if ("0000".equals(uploadResVo.getRt2_retCode())) {
                logger.info("上传资料成功:" + uploadResVo.getRt3_retMsg());
            } else {
                logger.info("上传资料失败:" + uploadResVo.getRt3_retMsg());
            }
        } catch (Exception e) {
            logger.error("合利宝委托代付用户资质认证失败:", e);
            uploadResVo.setRt2_retCode("-1");
            uploadResVo.setRt3_retMsg("合利宝委托代付用户资质认证失败:" + e.getMessage());
        }
        return uploadResVo;
    }

}

