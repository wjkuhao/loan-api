package com.mod.loan.util.changjie;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>
 * 畅捷请求工具类
 * </p>
 *
 * @author yanghta@chenjet.com
 * @version $Id: ChanPayUtil.java, v 0.1 2017-05-03 下午5:25:44
 */
public class ChanPayUtil {
    private static Logger logger = LoggerFactory.getLogger(ChanPayUtil.class);


    public static void main(String[] args) {
        String rsa_private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAO/6rPCvyCC+IMalLzTy3cVBz/+wamCFNiq9qKEilEBDTttP7Rd/GAS51lsfCrsISbg5td/w25+wulDfuMbjjlW9Afh0p7Jscmbo1skqIOIUPYfVQEL687B0EmJufMlljfu52b2efVAyWZF9QBG1vx/AJz1EVyfskMaYVqPiTesZAgMBAAECgYEAtVnkk0bjoArOTg/KquLWQRlJDFrPKP3CP25wHsU4749t6kJuU5FSH1Ao81d0Dn9m5neGQCOOdRFi23cV9gdFKYMhwPE6+nTAloxI3vb8K9NNMe0zcFksva9c9bUaMGH2p40szMoOpO6TrSHO9Hx4GJ6UfsUUqkFFlN76XprwE+ECQQD9rXwfbr9GKh9QMNvnwo9xxyVl4kI88iq0X6G4qVXo1Tv6/DBDJNkX1mbXKFYL5NOW1waZzR+Z/XcKWAmUT8J9AkEA8i0WT/ieNsF3IuFvrIYG4WUadbUqObcYP4Y7Vt836zggRbu0qvYiqAv92Leruaq3ZN1khxp6gZKl/OJHXc5xzQJACqr1AU1i9cxnrLOhS8m+xoYdaH9vUajNavBqmJ1mY3g0IYXhcbFm/72gbYPgundQ/pLkUCt0HMGv89tn67i+8QJBALV6UgkVnsIbkkKCOyRGv2syT3S7kOv1J+eamGcOGSJcSdrXwZiHoArcCZrYcIhOxOWB/m47ymfE1Dw/+QjzxlUCQCmnGFUO9zN862mKYjEkjDN65n1IUB9Fmc1msHkIZAQaQknmxmCIOHC75u4W0PGRyVzq8KkxpNBq62ICl7xmsPM=";
        try {
            System.out.println(ChanPayUtil.buildRequestByRSA(null,
                    rsa_private_key, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向测试服务器发送post请求
     *
     * @param origMap              参数map
     * @param charset              编码字符集
     * @param MERCHANT_PRIVATE_KEY 私钥
     * @param url                  请求地址
     */
    public static String sendPost(Map<String, String> origMap, String charset,
                                  String MERCHANT_PRIVATE_KEY, String url) {
        logger.info("#[发送请求]-[开始]-origMap={}", origMap);
        try {
            Map<String, String> sPara = ChanPayUtil.buildRequestPara(origMap,
                    BaseConstant.RSA, MERCHANT_PRIVATE_KEY, charset);
            String resultString = ChanPayUtil.buildRequest(sPara, BaseConstant.RSA,
                    MERCHANT_PRIVATE_KEY, charset, url);
            logger.info("#[发送请求]-[结束]-resultString={}", resultString);
            return resultString;
        } catch (Exception e) {
            logger.error("#[发送请求]-[异常]-e={}", e);
            return null;
        }
    }

    /**
     * 向测试服务器发送post请求
     *
     * @param origMap
     * @param charset
     * @param MERCHANT_PRIVATE_KEY
     * @param strParaFileName
     * @param strFilePath
     * @param downFileUrl
     * @return
     */
    public static Object sendFilePost(Map<String, String> origMap,
                                      String charset, String MERCHANT_PRIVATE_KEY,
                                      String strParaFileName, String strFilePath, String downFileUrl) {
        logger.info("#[对账文件下载发送请求]-[开始]-origMap={}", origMap);
        try {
            Map<String, String> sPara = ChanPayUtil.buildRequestPara(origMap,
                    BaseConstant.RSA, MERCHANT_PRIVATE_KEY, charset);
            Object object = ChanPayUtil.buildFileRequest(sPara, BaseConstant.RSA,
                    MERCHANT_PRIVATE_KEY, charset,
                    downFileUrl, strParaFileName,
                    strFilePath);
            logger.info("#[对账文件下载发送请求]-[结束]-object={}", JSONObject.toJSON(object));
            return object;
        } catch (Exception e) {
            logger.error("#[对账文件下载发送请求]-[异常]-e={}", e);
            return null;
        }
    }

    /**
     * 加密，部分接口，有参数需要加密
     *
     * @param src       原值
     * @param publicKey 畅捷支付发送的平台公钥
     * @param charset   UTF-8
     * @return RSA加密后的密文
     */
    public static String encrypt(String src, String publicKey, String charset) {
        try {
            byte[] bytes = RSA.encryptByPublicKey(src.getBytes(charset),
                    publicKey);
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            logger.error("#[RSA公钥加密]-[异常]-e={}", e);
        }
        return null;
    }

    /**
     * 拼接字符串
     *
     * @param params
     * @param encode
     * @return
     */
    public static String createLinkString(Map<String, String> params,
                                          boolean encode) {
        params = paraFilter(params);
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        String charset = params.get(BaseConstant.INPUT_CHARSET);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (encode) {
                try {
                    value = URLEncoder.encode(value, charset);
                } catch (UnsupportedEncodingException e) {
                    logger.error("#[拼接字符串]-[异常]-e={}", e);
                }
            }
            if (i == keys.size() - 1) {
                // 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }

    /**
     * 生成唯一流水号
     *
     * @return
     */
    public static String generateOutTradeNo() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.valueOf(new Double(
                Math.round(Math.random() * 1000000000)).longValue());
    }

    /**
     * 建立请求，以模拟远程HTTP的POST请求方式构造并获取钱包的处理结果
     * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("",
     * "",sParaTemp)
     *
     * @param strParaFileName 文件类型的参数名
     * @param strFilePath     文件路径
     * @param sParaTemp       组装后的请求参数数组
     * @return 钱包处理结果
     * @throws Exception
     */
    public static String buildRequest(Map<String, String> sParaTemp,
                                      String signType, String key, String inputCharset, String gatewayUrl)
            throws Exception {
        logger.info("#[http请求]-[开始]-sParaTemp={}", sParaTemp);
        HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler
                .getInstance();
        HttpRequest request = new HttpRequest(HttpResultType.BYTES);
        request.setCharset(inputCharset);
        request.setMethod(HttpRequest.METHOD_POST);
        request.setParameters(generatNameValuePair(sParaTemp, inputCharset));
        request.setUrl(gatewayUrl);
        HttpResponse response = httpProtocolHandler
                .execute(request, null, null);
        logger.info("#[http请求]-[返回结果]-response={}", JSONObject.toJSON(response));
        if (response == null) {
            logger.info("#[http请求]-[返回结果为空]");
            return null;
        }
        String strResult = response.getStringResult();
		/*//下载对账文件
		byte[] byteResult = response.getByteResult();
		Header[] responseHeaders = response.getResponseHeaders();
		String fileName = "";
		for(Header header : responseHeaders){
			if("content-disposition".equals(header.getName())){
				System.out.println(header.getValue());
				fileName = header.getValue();
			}
		}
		if(!"".equals(fileName)){
			File file  = new File("C:/"+fileName.substring(20));
			if(!file.exists()){
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(byteResult);
		}*/
        return strResult;
    }

    /**
     * 建立请求，以模拟远程HTTP的POST请求方式构造并获取钱包的处理结果
     * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("",
     * "",sParaTemp)
     *
     * @param strParaFileName 文件类型的参数名
     * @param strFilePath     文件路径
     * @param sParaTemp       组装后的请求参数数组
     * @return 钱包处理结果
     * @throws Exception
     */
    public static Object buildFileRequest(Map<String, String> sParaTemp,
                                          String signType, String key, String inputCharset,
                                          String downFileUrl, String strParaFileName, String strFilePath)
            throws Exception {
        logger.info("#[对账文件http请求]-[开始]-sParaTemp={}", sParaTemp);
        HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler
                .getInstance();
        HttpRequest request = new HttpRequest(HttpResultType.BYTES);
        request.setCharset(inputCharset);
        request.setMethod(HttpRequest.METHOD_POST);
        request.setParameters(generatNameValuePair(sParaTemp, inputCharset));
        request.setUrl(downFileUrl);

        HttpResponse response = httpProtocolHandler.execute(request,
                strParaFileName, strFilePath);
        logger.info("#[对账文件http请求]-[返回结果]-response={}", JSONObject.toJSON(response));
        if (response == null) {
            logger.info("#[对账文件http请求]-[返回结果为空]");
            return null;
        }
        byte[] byteResult = response.getByteResult();
        if (byteResult.length > 1024) {
            return byteResult;
        } else {
            return response.getStringResult();
        }
    }

    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("")
                    || key.equalsIgnoreCase(BaseConstant.SIGN)
                    || key.equalsIgnoreCase(BaseConstant.SIGN_TYPE)) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 生成要请求给钱包的参数数组
     *
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(
            Map<String, String> sParaTemp, String signType, String key,
            String inputCharset) throws Exception {
        logger.info("#[组装请求参数]-[开始]-sParaTemp={}", sParaTemp);
        // 除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(sParaTemp);
        logger.info("#[除去数组中的空值和签名参数]-sPara={}", sPara);
        // 生成签名结果
        String mysign = "";
        if (BaseConstant.MD5.equalsIgnoreCase(signType)) {
            mysign = buildRequestByMD5(sPara, key, inputCharset);
        } else if (BaseConstant.RSA.equalsIgnoreCase(signType)) {
            mysign = buildRequestByRSA(sPara, key, inputCharset);
        }
        // 签名结果与签名方式加入请求提交参数组中
        sPara.put(BaseConstant.SIGN, mysign);
        sPara.put(BaseConstant.SIGN_TYPE, signType);
        logger.info("#[组装请求参数]-[结束]-sPara={}", sPara);
        return sPara;
    }

    /**
     * 生成MD5签名结果
     *
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestByMD5(Map<String, String> sPara,
                                           String key, String inputCharset) throws Exception {
        // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String prestr = createLinkString(sPara, false);
        String mysign = MD5.sign(prestr, key, inputCharset);
        return mysign;
    }

    /**
     * 生成RSA签名结果
     *
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestByRSA(Map<String, String> sPara,
                                           String privateKey, String inputCharset) throws Exception {
        // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String prestr = createLinkString(sPara, false);
        String mysign = RSA.sign(prestr, privateKey, inputCharset);
        return mysign;
    }

    /**
     * MAP类型数组转换成NameValuePair类型
     *
     * @param properties MAP类型数组
     * @return NameValuePair类型数组
     */
    private static NameValuePair[] generatNameValuePair(
            Map<String, String> properties, String charset) throws Exception {
        NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            nameValuePair[i++] = new NameValuePair(entry.getKey(),
                    URLEncoder.encode(entry.getValue(), charset));
            // nameValuePair[i++] = new NameValuePair(entry.getKey(),
            // entry.getValue());
        }
        return nameValuePair;
    }
}
