package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import com.mod.loan.service.YeepayService;
import com.mod.loan.util.TimeUtils;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.client.YopRsaClient;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigitalEnvelopeDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Service
public class YeepayServiceImpl implements YeepayService {
    private static Logger log = LoggerFactory.getLogger(YeepayServiceImpl.class);

    @Value("${yeepay.bind.smg.url:}")
    String yeepay_bind_smg_url;

    @Value("${yeepay.bind.commit.url:}")
    String yeepay_bind_commit_url;

    @Value("${yeepay.repay.smg.url:}")
    String yeepay_repay_smg_url;

    @Value("${yeepay.repay.commit.url:}")
    String yeepay_repay_commit_url;

    @Value("${yeepay.callback.url:}")
    String yeepay_callback_url;

    @Value("${yeepay.repay.query.url:}")
    String yeepay_repay_query_url;

    @Override
    public String authBindCardRequest(String appKey, String privateKey, String requestNo, String identityId, String cardNo,
                                    String certNo, String userName,String cardPhone) {

        YopRequest yoprequest = new YopRequest(appKey, privateKey);
        yoprequest.addParam("requestno", requestNo);
        yoprequest.addParam("identityid", identityId);
        yoprequest.addParam("identitytype", "USER_ID"); //用户标识类型
        yoprequest.addParam("cardno", cardNo.trim());
        yoprequest.addParam("idcardno", certNo);
        yoprequest.addParam("idcardtype", "ID"); //固定值：ID 身份证
        yoprequest.addParam("username", userName);
        yoprequest.addParam("phone", cardPhone);
        yoprequest.addParam("avaliabletime", Constant.SMS_EXPIRATION_TIME/60); //验证码有效时间 单位：分钟
        yoprequest.addParam("issms", "true"); //是否发送短验
        yoprequest.addParam("advicesmstype", "MESSAGE"); //建议短验发送类型： MESSAGE 短信
        yoprequest.addParam("authtype", "COMMON_FOUR"); //鉴权类型： 固定值：COMMON_FOUR 验四
        yoprequest.addParam("requesttime", TimeUtils.getTime());

        log.info("authBindCardRequest userId={}, getParams={}", identityId, yoprequest.getParams().toString());

        try {
            YopResponse response = YopRsaClient.post(yeepay_bind_smg_url, yoprequest);
            log.info("send yeepay bind req :" + response);
            //TO_VALIDATE： 待短验
            return parseResult(response, "status", "TO_VALIDATE");
        } catch (Exception e) {
            log.error("send yeepay bind req has error={}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String authBindCardConfirm(String appKey, String privateKey, String requestNo, String validateCode) {
        YopRequest yoprequest = new YopRequest(appKey, privateKey);
        yoprequest.addParam("requestno", requestNo);
        yoprequest.addParam("validatecode", validateCode);

        log.info("authBindCardConfirm, getParams={}", yoprequest.getParams().toString());
        try {
            YopResponse response = YopRsaClient.post(yeepay_bind_commit_url, yoprequest);
            log.info("send yeepay bind confirm :" + response);

            return parseResult(response, "status", "BIND_SUCCESS");
        } catch (Exception e) {
            log.error("send yeepay bind confirm has error={}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String payRequest(String appKey, String privateKey, String requestNo, String identityId, String cardNo, String amount) {
        YopRequest yoprequest = new YopRequest(appKey, privateKey);
        yoprequest.addParam("requestno", requestNo);
        yoprequest.addParam("issms", "true");
        yoprequest.addParam("identityid", identityId);
        yoprequest.addParam("identitytype", "USER_ID");
        yoprequest.addParam("amount", amount);
        yoprequest.addParam("terminalno", "SQKKSCENEKJ010"); //协议支付： SQKKSCENEKJ010 代扣： SQKKSCENE10 //todo 是否支持代扣
        yoprequest.addParam("avaliabletime", Constant.SMS_EXPIRATION_TIME/60); //验证码有效时间 单位：分钟
        yoprequest.addParam("requesttime", TimeUtils.getTime());
        yoprequest.addParam("advicesmstype", "MESSAGE"); //建议短验发送类型： MESSAGE 短信
        yoprequest.addParam("productname", appKey);
        yoprequest.addParam("cardtop", cardNo.substring(0, 6));
        yoprequest.addParam("cardlast", cardNo.substring(cardNo.length() - 4));
        yoprequest.addParam("callbackurl", String.format(yeepay_callback_url, identityId));

        log.info("authBindCardConfirm, getParams={}", yoprequest.getParams().toString());

        try {
            YopResponse response = YopRsaClient.post(yeepay_repay_smg_url, yoprequest);
            log.info("send yeepay pay request :" + response);

            return parseResult(response, "status", "TO_VALIDATE");
        } catch (Exception e) {
            log.error("send yeepay pay request has error={}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String payConfirm(String appKey, String privateKey, String requestNo, String validateCode) {
        YopRequest yoprequest = new YopRequest(appKey, privateKey);
        yoprequest.addParam("requestno", requestNo);
        yoprequest.addParam("validatecode", validateCode);

        log.info("payConfirm, getParams={}", yoprequest.getParams().toString());
        try {
            YopResponse response = YopRsaClient.post(yeepay_repay_commit_url, yoprequest);
            log.info("send yeepay pay confirm :" + response);

            return parseResult(response, "status", "PROCESSING");
        } catch (Exception e) {
            log.error("send yeepay pay confirm has error={}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String parseResult(YopResponse response, String validateKey, String validateValue) {
        String stringResult = response.getStringResult();
        if (stringResult==null) {
            return response.getError().getMessage();
        }
        if (!validateValue.equals(JSONObject.parseObject(stringResult).getString(validateKey))){
            return JSONObject.parseObject(stringResult).getString("errormsg");
        }
        return null;
    }

    @Override
    public String repayCallback(String responseMsg, StringBuffer requestNo){
        DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
        dto.setCipherText(responseMsg);
        try {
            //设置商户私钥
            PrivateKey privateKey = InternalConfig.getISVPrivateKey(CertTypeEnum.RSA2048);
            //设置易宝公钥
            PublicKey publicKey = InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048);
            //解密验签
            dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);

            String stringResult = dto.getPlainText();

            //{"amount":0.01,"bankcode":"ECITIC","banksuccessdate":"2019-04-02 17:35:43",
            // "cardlast":"1675","cardtop":"621771","errorcode":"","errormsg":"","merchantno":"10025281077",
            // "requestno":"r20190402173447919646856","status":"PAY_SUCCESS",
            // "yborderid":"PONC18aebdea499140ff8785a3fe261efd5f"}
            log.info(stringResult);
            if (!"PAY_SUCCESS".equals(JSONObject.parseObject(stringResult).getString("status"))){
                return JSONObject.parseObject(stringResult).getString("errormsg");
            }
            String requestno = JSONObject.parseObject(stringResult).getString("requestno");
            requestNo.append(requestno);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("callback error={}",e.getMessage());
        }

        return null;
    }

    @Override
    public String repayCallbackMultiAcct(String strPrivateKey, String responseMsg, StringBuffer requestNo) {
        DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
        dto.setCipherText(responseMsg);
        try {
            //设置商户私钥
            PrivateKey privateKey = getPrivateKeyObject(strPrivateKey);
            //设置易宝公钥
            PublicKey publicKey = getPubKeyObject();
            //解密验签
            dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);

            String stringResult = dto.getPlainText();

            log.info(stringResult);
            if (!"PAY_SUCCESS".equals(JSONObject.parseObject(stringResult).getString("status"))){
                return JSONObject.parseObject(stringResult).getString("errormsg");
            }
            String requestno = JSONObject.parseObject(stringResult).getString("requestno");
            requestNo.append(requestno);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("callback error={}",e.getMessage());
        }

        return null;
    }

    private PublicKey getPubKeyObject() {
        PublicKey publicKey = null;
        try {
            // 直接写死 config json里面的，易宝的公钥是一样的
            String yeepay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkCQBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfjaorxsuwIDAQAB";
            java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
                    new BASE64Decoder().decodeBuffer(yeepay_public_key));
            // RSA对称加密算法
            java.security.KeyFactory keyFactory;
            keyFactory = java.security.KeyFactory.getInstance("RSA");
            // 取公钥匙对象
            publicKey = keyFactory.generatePublic(bobPubKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * 实例化私钥
     */
    private PrivateKey getPrivateKeyObject(String private_key) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec priPKCS8;
        try {
            priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(private_key));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            privateKey = keyf.generatePrivate(priPKCS8);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    @Override
    public String repayQuery(String appKey, String privateKey, String requestNo, String yborderid) {
        YopRequest yoprequest = new YopRequest(appKey, privateKey);
        yoprequest.addParam("requestno", requestNo);
        //yoprequest.addParam("yborderid", yborderid); //可以不需要

        try {
            YopResponse response = YopRsaClient.post(yeepay_repay_query_url, yoprequest);
            log.info("send yeepay repayQuery response :" + response);

            return parseResult(response, "status", "PAY_SUCCESS");
        } catch (Exception e) {
            log.error("send yeepay repayQuery has error={}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
