package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.model.request.AliAppH5RepayQueryRequest;
import com.mod.loan.model.request.AliAppH5RepayRequest;
import com.mod.loan.service.HuichaoRepayService;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.huichao.HuichaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NIELIN
 * @version $Id: HuichaoRepayServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class HuichaoRepayServiceImpl implements HuichaoRepayService {
    private static Logger logger = LoggerFactory.getLogger(HuichaoRepayServiceImpl.class);

    @Value("${huichao.aliAppH5RepayUrl}")
    String aliAppH5RepayUrl;
    @Value("${huichao.wxScanRepayUrl}")
    String wxScanRepayUrl;
    @Value("${huichao.aliAppH5OrWxScanRepayCallBackUrl}")
    String aliAppH5OrWxScanRepayCallBackUrl;
    @Value("${huichao.aliAppH5OrWxScanRepayQueryUrl}")
    String aliAppH5OrWxScanRepayQueryUrl;
    @Value("${huichao.bindBankCard4SendMsgUrl}")
    String bindBankCard4SendMsgUrl;
    @Value("${huichao.bindBankCard4ConfirmUrl}")
    String bindBankCard4ConfirmUrl;
    @Value("${huichao.repayUrl}")
    String repayUrl;
    @Value("${huichao.repayQueryUrl}")
    String repayQueryUrl;

    @Override
    public String aliAppH5Repay(AliAppH5RepayRequest request) throws Exception {
        if (null == request || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0 || StringUtils.isEmpty(request.getRequestSeriesNo())
                || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPublicKey()) || StringUtils.isEmpty(request.getPrivateKey4Repay())) {
            throw new Exception("参数为空");
        }
        //组装请求参数
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("merchantOutOrderNo", request.getRequestSeriesNo());
        paraMap.put("merid", request.getPartnerId());
        String dayStr = TimeUtils.parseTime(new Date(), TimeUtils.dateformat5);
        paraMap.put("noncestr", dayStr);
        paraMap.put("orderMoney", request.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        paraMap.put("orderTime", dayStr);
        paraMap.put("notifyUrl", aliAppH5OrWxScanRepayCallBackUrl);
        //对参数按照 key=value 的格式，并参照参数名 ASCII 码排序后得到字符串 stringA
        String stringA = HuichaoUtil.formatUrlMap(paraMap, false, false);
        //在 stringA 最后拼接上 key 得到 stringsignTemp 字符串， 并对 stringsignTemp 进行 MD5 运算，得到sign值
        String stringSignTemp = stringA + "&key=" + request.getPrivateKey4Repay();
        String sign = HuichaoUtil.getMD5(stringSignTemp);
        //对参数按照 key=value 的格式,参照参数名 ASCII 码排序,并对value做utf-8的encode编码后得到字符串 param
        String param = HuichaoUtil.formatUrlMap(paraMap, true, false);
        //将此URL送至APP前端页面或手机浏览器打开，即可自动调起支付宝(需要安装)发起支付
        String url = aliAppH5RepayUrl + "?" + param + "&sign=" + sign + "&id=" + (RequestThread.getUid());
        logger.info("#[app端支付宝还款]-[结束]-url={}", url);
        return url;
    }

    @Override
    public String aliAppH5OrWxScanRepayQuery(AliAppH5RepayQueryRequest request) throws Exception {
        if (null == request || StringUtils.isEmpty(request.getSeriesNo())
                || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPublicKey()) || StringUtils.isEmpty(request.getPrivateKey4Repay())) {
            throw new Exception("参数为空");
        }
        //拼装参数
        Map<String, String> param = new HashMap<String, String>();
        param.put("merchantOutOrderNo", request.getSeriesNo());
        param.put("merid", request.getPartnerId());
        param.put("noncestr", TimeUtils.parseTime(new Date(), TimeUtils.dateformat5));
        //将参数转换为key=value形式
        String paramStr = HuichaoUtil.formatUrlMap(param, false, false);
        //在最后拼接上密钥
        String signStr = paramStr + "&key=" + request.getPrivateKey4Repay();
        //MD5签名
        String sign = HuichaoUtil.getMD5(signStr);
        //拼接签名
        paramStr = paramStr + "&sign=" + sign;
        //发起查询
        String result = HuichaoUtil.sendPost(aliAppH5OrWxScanRepayQueryUrl, paramStr, "UTF-8");
        return result;
    }

    @Override
    public String wxScanRepay(AliAppH5RepayRequest request) throws Exception {
        if (null == request || null == request.getAmount() || request.getAmount().compareTo(BigDecimal.ZERO) <= 0 || StringUtils.isEmpty(request.getRequestSeriesNo())
                || StringUtils.isEmpty(request.getPartnerId()) || StringUtils.isEmpty(request.getPublicKey()) || StringUtils.isEmpty(request.getPrivateKey4Repay())) {
            throw new Exception("参数为空");
        }
        //组装请求参数
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("merchantOutOrderNo", request.getRequestSeriesNo());
        paraMap.put("merid", request.getPartnerId());
        String dayStr = TimeUtils.parseTime(new Date(), TimeUtils.dateformat5);
        paraMap.put("noncestr", dayStr);
        paraMap.put("orderMoney", request.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        paraMap.put("orderTime", dayStr);
        paraMap.put("notifyUrl", aliAppH5OrWxScanRepayCallBackUrl);
        //对参数按照 key=value 的格式，并参照参数名 ASCII 码排序后得到字符串 stringA
        String stringA = HuichaoUtil.formatUrlMap(paraMap, false, false);
        //在 stringA 最后拼接上 key 得到 stringsignTemp 字符串， 并对 stringsignTemp 进行 MD5 运算，得到sign值
        String stringSignTemp = stringA + "&key=" + request.getPrivateKey4Repay();
        String sign = HuichaoUtil.getMD5(stringSignTemp);
        //对参数按照 key=value 的格式,参照参数名 ASCII 码排序,并对value做utf-8的encode编码后得到字符串 param
        String param = HuichaoUtil.formatUrlMap(paraMap, true, false);
        //拼接请求url
        String url = wxScanRepayUrl + "?" + param + "&sign=" + sign + "&id=" + (RequestThread.getUid());
        logger.info("#[微信扫码支付]-[拼接请求url]-url={}", url);
        //发送请求
        String responseStr = HuichaoUtil.sendPost(url, null, "UTF-8");
        //获取微信扫码支付url
        String url2Repay = JSONObject.parseObject(responseStr).getString("url");
        return url2Repay;
    }
}
