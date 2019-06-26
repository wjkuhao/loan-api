package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.MerchantEnum;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.OrderRepayMapper;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.jinyuntong.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 金运通支付调用类
 * @Author actor
 * @Date
 */
@Service
public class OrderJinYunTongRePayServiceImpl implements OrderJinYunTongRePayService {
    private static Logger log = LoggerFactory.getLogger(OrderJinYunTongRePayServiceImpl.class);
    //地址
    @Value("${jytpay.appUrl}")
    private String APP_SERVER_URL;

    @Value("${jytpay.respMsgParamSeparator}")
    private String RESP_MSG_PARAM_SEPARATOR;
    /**
     * 返回报文merchant_id字段前缀
     */
    @Value("${jytpay.respMsgParamPrefixMerchantId}")
    private String RESP_MSG_PARAM_PREFIX_MERCHANT_ID;
    /**
     * 返回报文msg_enc字段前缀
     */
    @Value("${jytpay.respMsgParamPrefixMsgEnc}")
    private String RESP_MSG_PARAM_PREFIX_MSG_ENC;
    /**
     * 返回报文key_enc字段前缀
     */
    @Value("${jytpay.respMsgParamPrefixKeyEnc}")
    private String RESP_MSG_PARAM_PREFIX_KEY_ENC;
    /**
     * 返回报文sign字段前缀
     */
    @Value("${jytpay.respMsgParamPrefixSign}")
    private String RESP_MSG_PARAM_PREFIX_SIGN;
    @Value("${jytpay.sendBindCardSmsTraceCode}")
    private String sendBindCardSmsTraceCode;
    @Value(("${jytpay.bindCardTraceCode}"))
    private String bindCardTraceCode;
    @Value("${jytpay.orderRepayTraceCode}")
    private String orderRepayTraceCode;
    @Value("${jytpay.queryRePayStatusTraceCode}")
    private String queryRePayStatusTraceCode;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepayService orderRepayService;
    @Autowired
    private OrderRepayMapper orderRepayMapper;

    /**
     * 生成实名支付交易流水号
     *
     * @return
     */
    public static String getOrderId(String merchantId) {
        return "D" + merchantId + RandomStringUtils.randomNumeric(17);
    }

    /**
     * 获取商户交易流水号
     *
     * @return
     */
    public synchronized String getTranFlowid(String merchantId) {
        //格式必须是【商户号 + XXX（可自定义，但需18位）】
        return merchantId + TimeUtils.parseTime(new Date(), TimeUtils.dateformat5) + RandomStringUtils.randomNumeric(6);
    }

    /**
     * 获得报文头字符串
     *
     * @param tranCode
     * @return
     */
    public JSONObject getMsgHeadJson(String tranCode, String merchantId) {
        JSONObject header = new JSONObject();
        header.put("version", "1.0.1");
        header.put("tranType", "01");
        header.put("merchantId", merchantId);
        header.put("tranDate", DateTimeUtils.getDateTimeToString(new Date(), DateTimeUtils.DATE_FORMAT_YYYYMMDD));
        header.put("tranTime", DateTimeUtils.getDateTimeToString(new Date(), DateTimeUtils.DATETIME_FORMAT_HHMMSS));
        header.put("tranFlowid", getTranFlowid(merchantId));
        header.put("tranCode", tranCode);

        return header;
    }

    /**
     * 发送交易
     *
     * @param json
     * @param sign
     * @param orderId
     * @return
     * @throws Exception
     */
    public String sendMsg(String json, String sign, String orderId, String merchantId, RSAHelper rsaHelper) {
        log.info("金运通,上送报文：" + json);
        log.info("金运通,上送签名：" + sign);
        byte[] des_key = DESHelper.generateDesKey();


        //1、组装参数
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("merchant_id", merchantId);
        paramMap.put("msg_enc", encryptMsg(json, des_key));
        paramMap.put("key_enc", encryptKey(des_key, rsaHelper));
        paramMap.put("sign", sign);
        paramMap.put("mer_order_id", orderId);

        //2、请求，并获取结果
        String res = null;
        try {
            res = HttpClient431Util.doPost(paramMap, APP_SERVER_URL);
            if (res == null) {
                log.error("金运通,服务器连接失败");
                return null;
            } else {
                log.info("金运通,连接服务器成功,返回结果={}", res);
            }
            //3、解析结果
            String[] respMsg = res.split(RESP_MSG_PARAM_SEPARATOR); //分割
            String merchantId1 = respMsg[0].substring(RESP_MSG_PARAM_PREFIX_MERCHANT_ID.length());
            String respJsonEnc = respMsg[1].substring(RESP_MSG_PARAM_PREFIX_MSG_ENC.length());
            String respKeyEnc = respMsg[2].substring(RESP_MSG_PARAM_PREFIX_KEY_ENC.length());
            String respSign = respMsg[3].substring(RESP_MSG_PARAM_PREFIX_SIGN.length());

            //4、解密密钥
            byte respKey[] = decryptKey(respKeyEnc, rsaHelper);

            //5、解密报文
            String respJson = decrytJson(respJsonEnc, respKey);
            log.info("金运通,返回报文merchantId:" + merchantId1);
            log.info("金运通,返回报文JSON:" + respJson);
            log.info("金运通,返回报文签名:" + respSign);
            //6、验签
            if (!verifyMsgSign(respJson, respSign, rsaHelper)) {
                return null;
            }
            return respJson;
        } catch (Exception e) {
            log.error("金运通服务器异常,e={}", e);
            return null;
        }
    }

    /**
     * 支付异步通知,返回
     *
     * @Author actor
     * @Date 2019/6/20 16:07
     */
    private String backRepayNotice(String json, String sign, String orderId, String merchantId, RSAHelper rsaHelper) {
        byte[] des_key = DESHelper.generateDesKey();
        //1、组装参数
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("merchant_id=");
        stringBuilder.append(merchantId);
        stringBuilder.append("&");
        stringBuilder.append("msg_enc=");
        stringBuilder.append(encryptMsg(json, des_key));
        stringBuilder.append("&");
        stringBuilder.append("key_enc=");
        stringBuilder.append(encryptKey(des_key, rsaHelper));
        stringBuilder.append("&");
        stringBuilder.append("sign=");
        stringBuilder.append(sign);
        stringBuilder.append("&");
        stringBuilder.append("mer_order_id=");
        stringBuilder.append(orderId);
        return stringBuilder.toString();
    }

    private String encryptKey(byte[] key, RSAHelper rsaHelper) {

        byte[] enc_key = null;
        try {
            enc_key = rsaHelper.encryptRSA(key, false, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return StringUtil.bytesToHexString(enc_key);
    }

    private byte[] decryptKey(String hexkey, RSAHelper rsaHelper) {
        byte[] key = null;
        byte[] enc_key = StringUtil.hexStringToBytes(hexkey);

        try {
            key = rsaHelper.decryptRSA(enc_key, false, "UTF-8");
        } catch (Exception e) {
            log.error("金运通返回decryptKey异常,e={}", e);
        }

        return key;
    }

    private String encryptMsg(String json, byte[] key) {
        String enc_msg = CryptoUtils.desEncryptToHex(json, key);
        return enc_msg;
    }

    public String decrytJson(String respJsonEnc, byte[] key) {
        String json = CryptoUtils.desDecryptFromHex(respJsonEnc, key);
        return json;
    }

    public boolean verifyMsgSign(String json, String sign, RSAHelper rsaHelper) {
        byte[] bsign = StringUtil.hexStringToBytes(sign);
        boolean ret = false;
        try {
            ret = rsaHelper.verifyRSA(json.getBytes("UTF-8"), bsign, false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException异常,e={}", e);
        } catch (Exception e) {
            log.error("金运通验签失败,e={}", e);
        }
        return ret;
    }

    /**
     * 签名
     *
     * @param json
     * @return
     */
    public String signMsg(String json, RSAHelper rsaHelper) {
        String hexSign = null;
        try {
            byte[] sign = rsaHelper.signRSA(json.getBytes("UTF-8"), false, "UTF-8");
            hexSign = StringUtil.bytesToHexString(sign);
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException异常,e={}", e);
        } catch (Exception e) {
            log.error("签名异常.e={}", e);
        }
        return hexSign;
    }

    //验签
    public boolean verifySign(byte[] plainBytes, byte[] signBytes, RSAHelper rsaHelper) {
        boolean flag = false;
        try {
            flag = rsaHelper.verifyRSA(plainBytes, signBytes, false, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public String getTranStateByRespJson(String respJson, String tranCode) {
        try {
            JSONObject json = JSON.parseObject(respJson);
            JSONObject jsonHead = json.getJSONObject("head");
            String respCode = jsonHead.getString("respCode");
            String respDesc = jsonHead.getString("respDesc");
            System.out.println(respCode + " | " + respDesc);

            JSONObject jsonBody = json.getJSONObject("body");
            if (jsonBody.isEmpty() || jsonBody == null) {
                if (!"S0000000".equals(respCode)) {
                    return "03"; //返回交易失败
                } else if ("E0000000".equals(respCode) && "TD4005".equals(tranCode)) {
                    return "02"; //返回交易处理中
                }
            }
            String tranState = jsonBody.getString("tranState");
            if ("TD2004".equals(tranCode)) {
                tranState = jsonBody.getString("bindState");
            }
            return tranState;

        } catch (Exception e) {
            log.error("解析异常,e={}", e);
        }
        return "";
    }


    public String getRespCodeByRespJson(String respJson, String tranCode) {
        try {
            JSONObject json = JSON.parseObject(respJson);
            JSONObject jsonHead = json.getJSONObject("head");
            String respCode = jsonHead.getString("respCode");
            String respDesc = jsonHead.getString("respDesc");
            System.out.println(respCode + " | " + respDesc);
            return respCode;
        } catch (Exception e) {
            log.error("解析异常,e={}", e);
        }
        return "";
    }


    public JSONObject getRespJsonHeadByRespJson(String respJson, String tranCode) {
        try {
            JSONObject json = JSON.parseObject(respJson);
            JSONObject jsonHead = json.getJSONObject("head");
            return jsonHead;
        } catch (Exception e) {
            log.error("金运通head解析异常,e={}", e);
        }
        return null;
    }

    public JSONObject getBodyByRespJson(String respJson) {
        try {
            JSONObject json = JSON.parseObject(respJson);
            JSONObject body = json.getJSONObject("body");
            return body;
        } catch (Exception e) {
            log.error("金运通body解析异常,e={}", e);
        }
        return null;
    }

    public JSONObject getHeadByRespJson(String respJson) {
        try {
            JSONObject json = JSON.parseObject(respJson);
            JSONObject head = json.getJSONObject("head");
            return head;
        } catch (Exception e) {
            log.error("解析异常,e={}", e);
        }
        return null;
    }

    /*
     * @Author actor
     * @Date 2019/6/19 15:57
     */
    public RSAHelper getRSAHelper(String merchantId, String clientPrivateKey, String serverPublicKey) {
        RSAHelper rsaHelper = new RSAHelper();
        try {
            rsaHelper.initKey(clientPrivateKey, serverPublicKey, 2048);
            return rsaHelper;
        } catch (Exception e) {
            log.error("金运通生成rsakey异常,merchantId={},e={}", merchantId, e);
            return null;
        }
    }

    /**
     * 发送绑卡短信
     *
     * @Author actor
     * @Date 2019/6/19 15:58
     */
    public ResultMessage sendBindCardSms(Long uid, String cardNo, String cardPhone, Bank bank) {
        //获取用户信息
        User user = userService.selectByPrimaryKey(uid);
        if (null == user) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "用户信息不存在");
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        if (merchant == null || StringUtils.isEmpty(merchant.getJinyuntongMerchantId()) || StringUtils.isEmpty(merchant.getJinyuntongMerchantPrivateKey()) || StringUtils.isEmpty(merchant.getJinyuntongPublicKey())) {
            log.error("商户金运通信息不全,clientAlias={}", RequestThread.getClientAlias());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户信息异常");
        }
        String custNo = String.valueOf(uid);
        String orderId = getOrderId(merchant.getJinyuntongMerchantId());

        RSAHelper rsaHelper = getRSAHelper(merchant.getJinyuntongMerchantId(), merchant.getJinyuntongMerchantPrivateKey(), merchant.getJinyuntongPublicKey());
        if (rsaHelper == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户秘钥异常");
        }
        //1、报文头
        JSONObject jsonHead = getMsgHeadJson(sendBindCardSmsTraceCode, merchant.getJinyuntongMerchantId());
        JSONObject json = new JSONObject();
        json.put("head", jsonHead);

        //2、报文头体
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("custNo", custNo);
        jsonBody.put("orderId", orderId);
        jsonBody.put("bankCardNo", cardNo);
        jsonBody.put("idCardNo", user.getUserCertNo());
        jsonBody.put("mobile", cardPhone);
        jsonBody.put("name", user.getUserName());

        json.put("body", jsonBody);

        log.info("上送报文:" + json);

        String mac = signMsg(json.toJSONString(), rsaHelper); //加签，用商户端私钥进行加签
        String respJson = sendMsg(json.toJSONString(), mac, orderId, merchant.getJinyuntongMerchantId(), rsaHelper); //接收响应报文
        if (respJson == null) {
            return new ResultMessage(ResponseEnum.M4000);
        }
//            String respCode = getRespCodeByRespJson(respJson, sendBindCardSmsTraceCode); //获取关键信息（如交易状态、响应码等）
        JSONObject headByRespJson = getHeadByRespJson(respJson);
        String respCode = headByRespJson.getString("respCode");
        String respDesc = headByRespJson.getString("respDesc");
        //看文档判断
        if ("S0000000".equals(respCode)) {
            JSONObject bodyByRespJson = getBodyByRespJson(respJson);
            String bindOrderId = bodyByRespJson.getString("bindOrderId");
            //缓存绑卡数据
            JSONObject requestVo = new JSONObject();
            requestVo.put("P4_orderId", orderId);
            requestVo.put("P6_cardNo", cardNo);
            requestVo.put("P7_phone", cardPhone);
            requestVo.put("bankName", bank.getBankName());
            requestVo.put("bankCode", bank.getCode());
            requestVo.put("bindOrderId", bindOrderId);
            redisMapper.set(RedisConst.user_bank_bind + user.getId(), requestVo, Constant.SMS_EXPIRATION_TIME);
            return new ResultMessage(ResponseEnum.M2000);
        } else {
            return new ResultMessage(ResponseEnum.M4000.getCode(), respDesc);
        }

    }

    /**
     * 绑定银行卡
     *
     * @Author actor
     * @Date 2019/6/19 17:37
     */
    public ResultMessage bindCard(String validateCode, Long uid, String bindInfo) {

        if (StringUtils.isEmpty(bindInfo)) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        Map reqMap = JSON.parseObject(bindInfo, Map.class);
        if (MapUtils.isEmpty(reqMap)) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "验证码失效,请重新获取");
        }
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        //获取商户信息
        if (merchant == null || StringUtils.isEmpty(merchant.getJinyuntongMerchantId()) || StringUtils.isEmpty(merchant.getJinyuntongMerchantPrivateKey()) || StringUtils.isEmpty(merchant.getJinyuntongPublicKey())) {
            log.error("商户金运通信息不全,clientAlias={}", RequestThread.getClientAlias());
            return new ResultMessage(ResponseEnum.M4000, "商户信息异常");
        }
        RSAHelper rsaHelper = getRSAHelper(merchant.getJinyuntongMerchantId(), merchant.getJinyuntongMerchantPrivateKey(), merchant.getJinyuntongPublicKey());
        if (rsaHelper == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户密钥异常");
        }
        JSONObject json = new JSONObject();

        //1、报文头
        JSONObject jsonHead = getMsgHeadJson(bindCardTraceCode, merchant.getJinyuntongMerchantId());
        json.put("head", jsonHead);
        String bindOrderId = MapUtils.getString(reqMap, "bindOrderId");
        //2、报文头体
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("mobile", reqMap.get("P7_phone"));
        jsonBody.put("verifyCode", validateCode);
        jsonBody.put("bindOrderId", bindOrderId);
        json.put("body", jsonBody);

        log.info("金运通绑卡,上送报文:{}", json);

        String mac = signMsg(json.toJSONString(), rsaHelper); //加签，用商户端私钥进行加签
        if (mac == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "加签异常");
        }
        try {
            String respJson = sendMsg(json.toJSONString(), mac, bindOrderId, merchant.getJinyuntongMerchantId(), rsaHelper); //接收响应报文

//            String respCode = getRespCodeByRespJson(respJson, bindCardTraceCode); //获取关键信息（如交易状态、响应码等）
            JSONObject respJsonHead = getRespJsonHeadByRespJson(respJson, bindCardTraceCode);
            String respCode = respJsonHead.getString("respCode");
            String respDesc = jsonHead.getString("respDesc");

            log.info("金运通绑卡订单号bindOrderId={},返回={}", bindOrderId, respJson);
            //看文档判断
            if ("S0000000".equals(respCode)) {
                System.out.println("短信鉴权绑卡成功");
                UserBank userBank = new UserBank();
                userBank.setCardCode(MapUtils.getString(reqMap, "bankCode"));
                userBank.setCardName(MapUtils.getString(reqMap, "bankName"));
                userBank.setCardNo(MapUtils.getString(reqMap, "P6_cardNo"));
                userBank.setCardPhone(MapUtils.getString(reqMap, "P7_phone"));
                userBank.setCardStatus(1);
                userBank.setCreateTime(new Date());
                userBank.setUid(uid);
                userBank.setBindType(MerchantEnum.jinyuntong.getCode());
                userService.insertUserBank(uid, userBank);
                redisMapper.remove(RedisConst.user_bank_bind + uid);
                return new ResultMessage(ResponseEnum.M2000);
            } else {
                log.info("金运通短信鉴权绑卡失败,uid={},bindOrderId={}", uid, bindOrderId);
                return new ResultMessage(ResponseEnum.M4000.getCode(), respDesc);
            }
        } catch (Exception e) {
            log.error("金运通绑卡异常,e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }
    }

    /**
     * 金运通还款回调处理
     *
     * @Author actor
     * @Date 2019/6/20 15:15
     */
    @Override
    public String jinyuntongOrderRepayNotice(Map map) {
        String merchantId = MapUtils.getString(map, "merchant_id");
        Merchant merchantQuery = new Merchant();
        merchantQuery.setJinyuntongMerchantId(merchantId);
        Merchant merchant = merchantService.selectOne(merchantQuery);
        if (merchant == null) {
            log.error("金运通回调商户信息不存在,merchant_id={}", merchantId);
        }
        RSAHelper rsaHelper = getRSAHelper(merchant.getJinyuntongMerchantId(), merchant.getJinyuntongMerchantPrivateKey(), merchant.getJinyuntongPublicKey());

        //4、解密密钥
        byte respKey[] = decryptKey(MapUtils.getString(map, "key_enc"), rsaHelper);

        //5、解密报文
        String respJson = decrytJson(MapUtils.getString(map, "msg_enc"), respKey);

        //6、验签
        if (!verifyMsgSign(respJson, MapUtils.getString(map, "sign"), rsaHelper)) {
            return null;
        }
        JSONObject headByRespJson = getHeadByRespJson(respJson);
        JSONObject bodyByRespJson = getBodyByRespJson(respJson);
        String tranState = bodyByRespJson.getString("tranState");
        String repayNo = bodyByRespJson.getString("orderId");
        log.info("金运通打款回调,tranState={},repayNo={}", tranState, repayNo);
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
        if (orderRepay == null) {
            log.error("还款流水为空,repayNo={}", repayNo);
        }
        Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
        if (order == null) {
            log.error("金运通还款回调,订单为空orderId={}", order.getId());
        }
        if ("00".equals(tranState)) {
            log.info("金运通异步回调还款成功,orderId={},repayNo={}", order.getId(), repayNo);
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(3);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            Order order1 = new Order();
            order1.setId(orderRepay.getOrderId());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(bodyByRespJson.getBigDecimal("tranAmt"));
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
        } else if ("03".equals(tranState)) {
            log.info("金运通异步回调还款成功,orderId={},repayNo={}", order.getId(), repayNo);
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
        }

        JSONObject json = new JSONObject();
        //请求头
        JSONObject jsonHead = getMsgHeadJson(null, merchant.getJinyuntongMerchantId());
        jsonHead.put("tranFlowid", headByRespJson.getString("tranFlowid"));
        jsonHead.put("respCode", "S0000000");
        jsonHead.put("tranType", "02");
        json.put("head", jsonHead);
        //2、报文头体
        JSONObject jsonBody = new JSONObject();
        json.put("body", jsonBody);
        String sign = signMsg(json.toJSONString(), rsaHelper);
        return backRepayNotice(json.toJSONString(), sign, headByRespJson.getString("tranFlowid"), merchant.getJinyuntongMerchantId(), rsaHelper);

    }

    /**
     * 还款
     *
     * @Author actor
     * @Date 2019/6/20 9:59
     */
    public ResultMessage orderRepay(Long orderId) {
        Long uid = RequestThread.getUid();
        if (orderId == null) {
            log.info("订单异常，uid={},订单号={}", uid, orderId);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单不存在");
        }
        //查询订单信息
        Order order = orderService.selectByPrimaryKey(orderId);
        if (order == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单为空");
        }
        if (!order.getUid().equals(uid)) {
            log.info("订单不属于登录用户，orderId={},uid={}", order.getId(), uid);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单异常");
        }
        if (order.getStatus() >= OrderEnum.NORMAL_REPAY.getCode() || order.getStatus() < OrderEnum.REPAYING.getCode()) {
            log.info("订单非还款状态，订单号为：{}", order.getId());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单状态异常");
        }
        //查询用户绑定银行卡信息
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        if (userBank == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "未绑卡");
        }
        //查询商户信息
        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        if (merchant == null || StringUtils.isEmpty(merchant.getJinyuntongMerchantId()) || StringUtils.isEmpty(merchant.getJinyuntongMerchantPrivateKey()) || StringUtils.isEmpty(merchant.getJinyuntongPublicKey())) {
            log.error("商户金运通信息不全,clientAlias={}", RequestThread.getClientAlias());
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户信息异常");
        }
        JSONObject json = new JSONObject();
        RSAHelper rsaHelper = getRSAHelper(merchant.getJinyuntongMerchantId(), merchant.getJinyuntongMerchantPrivateKey(), merchant.getJinyuntongPublicKey());
        if (rsaHelper == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "商户秘钥异常");
        }
        String repayNo = com.mod.loan.util.StringUtil.getOrderNumber("r");// 支付流水号
        BigDecimal shouldRepay = order.getShouldRepay();
        if (shouldRepay == null) {
            log.error("订单应还金额为空,orderId={}", orderId);
            return new ResultMessage(ResponseEnum.M4000.getCode(), "订单应还金额为空");
        }
        //1、报文头
        JSONObject jsonHead = getMsgHeadJson(orderRepayTraceCode, merchant.getJinyuntongMerchantId());
        json.put("head", jsonHead);

        //2、报文头体
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("custNo", uid);
        jsonBody.put("orderId", repayNo);
        jsonBody.put("bankCardNo", userBank.getCardNo());
        jsonBody.put("tranAmt", shouldRepay);
        json.put("body", jsonBody);
        String mac = signMsg(json.toJSONString(), rsaHelper); //加签，用商户端私钥进行加签
        if (mac == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "加签异常");
        }
        String respJson = null; //接收响应报文
        // 还款记录表
        OrderRepay orderRepay = new OrderRepay();
        orderRepay.setRepayNo(repayNo);
        orderRepay.setUid(order.getUid());
        orderRepay.setOrderId(orderId);
        orderRepay.setRepayType(1);
        orderRepay.setRepayMoney(shouldRepay);
        orderRepay.setBank(userBank.getCardName());
        orderRepay.setBankNo(userBank.getCardNo());
        orderRepay.setCreateTime(new Date());
        orderRepay.setUpdateTime(new Date());
        orderRepay.setRepayStatus(0);
        try {
            //流水号重复的话再试一遍,重新生成
            orderRepayService.insertSelective(orderRepay);
        } catch (Exception e) {
            repayNo = com.mod.loan.util.StringUtil.getOrderNumber("r");// 支付流水号
            orderRepay.setRepayNo(repayNo);
            try {
                orderRepayService.insertSelective(orderRepay);
            } catch (Exception e2) {
                log.error("金运通生成流水失败,e={}", e2);
                return new ResultMessage(ResponseEnum.M4000.getCode(), "金运通生成流水失败");
            }
        }
        try {
            respJson = sendMsg(json.toJSONString(), mac, repayNo, merchant.getJinyuntongMerchantId(), rsaHelper);
            log.info("金运通支付返回，orderId={},respJson={}:", orderId, respJson);
            if (respJson == null) {
                return new ResultMessage(ResponseEnum.M4000.getCode(), "金运通生成流水失败");
            }
        } catch (Exception e) {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(e.getMessage());
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            log.error("金运通还款接口异常,e={}", e);
            return new ResultMessage(ResponseEnum.M4000);
        }

        String tranState = getTranStateByRespJson(respJson, orderRepayTraceCode); //获取关键信息（如交易状态、响应码等）
        JSONObject bodyByRespJson = getBodyByRespJson(respJson);
        if (bodyByRespJson == null) {
            return new ResultMessage(ResponseEnum.M4000.getCode());
        }
        //看文档判断
        if ("00".equals(tranState)) {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(3);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            Order order1 = new Order();
            order1.setId(orderRepay.getOrderId());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(shouldRepay);
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
            ResultMessage resultMessage = new ResultMessage(ResponseEnum.M2000);
            resultMessage.setMessage("交易成功");
            return resultMessage;
        } else if ("03".equals(tranState)) {
            log.info("金运通还款失败,orderId={},repayNo={}", orderId, repayNo);
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            ResultMessage resultMessage = new ResultMessage(ResponseEnum.M4000);
            resultMessage.setMessage("交易失败");
            return resultMessage;
        } else {
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(1);
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
            //处理中返回订单号，便于查看详情
            return new ResultMessage(ResponseEnum.M2000, order.getId());
        }
    }

    /**
     * 金运通还款查询接口
     *
     * @Author actor
     * @Date 2019/6/20 11:34
     */
    public void queryRePayStatus(String repayNo) {
        JSONObject json = new JSONObject();
        //根据还款流水号查询还款流水信息
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(repayNo);
        if (null == orderRepay) {
            log.info("根据还款流水号查询还款流水信息为空,repayNo={}", repayNo);
            return;
        }
        //根据订单id查询订单信息
        Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
        if (null == order) {
            log.info("根据订单id查询订单信息为空");
            return;
        }
        log.info("#[根据订单id查询订单信息]-order={}", JSONObject.toJSON(order));

        //幂等
        if (OrderEnum.NORMAL_REPAY.getCode().equals(order.getStatus()) || OrderEnum.OVERDUE_REPAY.getCode().equals(order.getStatus()) || OrderEnum.DEFER_REPAY.getCode().equals(order.getStatus())) {
            log.info("该笔订单状态已还款");
            return;
        }
        //获取商户信息
        Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
        if (null == merchant || StringUtils.isEmpty(merchant.getJinyuntongMerchantId()) || StringUtils.isEmpty(merchant.getJinyuntongPublicKey()) || StringUtils.isEmpty(merchant.getJinyuntongMerchantPrivateKey())) {
            log.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
            return;
        }
        //1、报文头
        JSONObject jsonHead = getMsgHeadJson(queryRePayStatusTraceCode, merchant.getJinyuntongMerchantId());
        json.put("head", jsonHead);

        //2、报文头体
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("orderId", repayNo);
        json.put("body", jsonBody);
        RSAHelper rsaHelper = getRSAHelper(merchant.getJinyuntongMerchantId(), merchant.getJinyuntongMerchantPrivateKey(), merchant.getJinyuntongPublicKey());
        String mac = signMsg(json.toJSONString(), rsaHelper); //加签，用商户端私钥进行加签
        String respJson = null;
        try {
            respJson = sendMsg(json.toJSONString(), mac, repayNo, merchant.getJinyuntongMerchantId(), rsaHelper); //接收响应报文
        } catch (Exception e) {
            log.error("金运通查询退款异常,e={}", e);
            return;
        }

        String tranState = getTranStateByRespJson(respJson, queryRePayStatusTraceCode); //获取关键信息（如交易状态、响应码等）
        JSONObject bodyByRespJson = getBodyByRespJson(respJson);
        if (bodyByRespJson == null) {
            return;
        }
        //看文档判断
        if ("00".equals(tranState)) {
            log.info("金运通支付成功,orderId={},repayNo={}", order.getId(), repayNo);
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(3);
            orderRepay1.setUpdateTime(new Date());
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            Order order1 = new Order();
            order1.setId(orderRepay.getOrderId());
            order1.setRealRepayTime(new Date());
            order1.setHadRepay(bodyByRespJson.getBigDecimal("tranAmount"));
            order1.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
            orderRepayService.updateOrderRepayInfo(orderRepay1, order1);
        } else if ("01".equals(tranState)) {
            log.info("金运通平台处理中,未验证支付,orderId={},repayNo={}", order.getId(), repayNo);
        } else if ("02".equals(tranState)) {
            log.info("金运通银行处理中,已验证支付,orderId={},repayNo={}", order.getId(), repayNo);
        } else if ("03".equals(tranState)) {
            log.info("金运通支付失败,orderId={},repayNo={}", order.getId(), repayNo);
            OrderRepay orderRepay1 = new OrderRepay();
            orderRepay1.setRepayNo(repayNo);
            orderRepay1.setRepayStatus(4);
            orderRepay1.setRemark(bodyByRespJson.getString("remark"));
            orderRepayService.updateByPrimaryKeySelective(orderRepay1);
        }
    }


    @Override
    public List<OrderRepay> jinyuntongRepayQuery4Task() {
        return orderRepayMapper.jinyuntongRepayQuery4Task();
    }
}
