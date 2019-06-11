package com.mod.loan.util.kuaiqian.common;

import com.bill99.asap.exception.CryptoException;
import com.bill99.asap.service.ICryptoService;
import com.bill99.asap.service.impl.CryptoServiceFactory;
import com.bill99.schema.asap.commons.Mpf;
import com.bill99.schema.asap.data.SealedData;
import com.bill99.schema.asap.data.UnsealedData;
import com.mod.loan.util.XmlUtils;
import com.mod.loan.util.kuaiqian.notice.NotifyRequest;
import com.mod.loan.util.kuaiqian.notice.NotifyResponse;
import com.mod.loan.util.kuaiqian.notice.Pay2bankNotify;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

public class KuaiqianHttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(KuaiqianHttpUtil.class);
    //接口版本
    private static String VERSION = "1.0";
    //策略编码，固定值 F41
    private static String fetureCode = "F41";
    //字符编码
    private static String encoding = "UTF-8";

    public static String genPayNoticeXml(String ori, String memberCode){
        Mpf mpf = genMpf(memberCode);
        SealedData sealedData = null;
        try {
            ICryptoService service = CryptoServiceFactory.createCryptoService();
            sealedData = service.seal(mpf, ori.getBytes());
        } catch (CryptoException e) {
            System.out.println(e);
        }
        NotifyResponse response = CCSUtil.genNoticeResponse(memberCode , VERSION);
        byte[] nullbyte = {};
        byte[] byteOri = sealedData.getOriginalData() == null ? nullbyte : sealedData.getOriginalData();
        byte[] byteEnc = sealedData.getEncryptedData() == null ? nullbyte : sealedData.getEncryptedData();
        byte[] byteEnv = sealedData.getDigitalEnvelope() == null ? nullbyte : sealedData.getDigitalEnvelope();
        byte[] byteSig = sealedData.getSignedData() == null ? nullbyte : sealedData.getSignedData();
        response.getNotifyResponseBody().getSealDataType().setOriginalData(PKIUtil.byte2UTF8StringWithBase64(byteOri));
        //获取加签报文
        response.getNotifyResponseBody().getSealDataType().setSignedData(PKIUtil.byte2UTF8StringWithBase64(byteSig));
		//获取加密报文
        response.getNotifyResponseBody().getSealDataType().setEncryptedData(PKIUtil.byte2UTF8StringWithBase64(byteEnc));
		//数字信封
        response.getNotifyResponseBody().getSealDataType().setDigitalEnvelope(PKIUtil.byte2UTF8StringWithBase64(byteEnv));
        //请求报文
        String requestXml = XmlUtils.convertToXml(response, encoding);
        logger.info("请求加密报文 = " + requestXml);
        return requestXml;
    }

    /**
     * 获取请求响应的加密数据
     *
     * @param requestXml
     * @return
     * @throws Exception
     */
    public static String invokeCSSCollection(String requestXml, String url) throws Exception {
        //初始化HttpClient
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
        SSLContext.setDefault(sslContext);
        //请求服务端
        InputStream in_withcode = new ByteArrayInputStream(requestXml.getBytes(encoding));
        method.setRequestBody(in_withcode);
        // url的连接等待超时时间设置
        client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
        // 读取数据超时时间设置
        client.getHttpConnectionManager().getParams().setSoTimeout(3000);
        method.setRequestEntity(new StringRequestEntity(requestXml, "text/html", "utf-8"));
        client.executeMethod(method);
        //打印服务器返回的状态
        logger.info("服务器返回的状态 = {}", method.getStatusLine());
        //打印返回的信息
        InputStream stream = method.getResponseBodyAsStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding));
        StringBuffer buf = new StringBuffer();
        String line;
        while (null != (line = br.readLine())) {
            buf.append(line).append("\n");
        }
        //释放连接
        method.releaseConnection();
        return buf.toString();
    }


    public static Pay2bankNotify unsealOrderPayNotice(NotifyRequest request, String memberCode){
        SealedData sealedData = new SealedData();
        sealedData.setOriginalData(request.getNotifyRequestBody().getSealDataType().getOriginalData()==null?null:PKIUtil.utf8String2ByteWithBase64(request.getNotifyRequestBody().getSealDataType().getOriginalData()));
        sealedData.setSignedData(request.getNotifyRequestBody().getSealDataType().getSignedData()==null?null:PKIUtil.utf8String2ByteWithBase64(request.getNotifyRequestBody().getSealDataType().getSignedData()));
        sealedData.setEncryptedData(request.getNotifyRequestBody().getSealDataType().getEncryptedData()==null?null:PKIUtil.utf8String2ByteWithBase64(request.getNotifyRequestBody().getSealDataType().getEncryptedData()));
        sealedData.setDigitalEnvelope(request.getNotifyRequestBody().getSealDataType().getDigitalEnvelope()==null?null:PKIUtil.utf8String2ByteWithBase64(request.getNotifyRequestBody().getSealDataType().getDigitalEnvelope()));
        Mpf mpf = genMpf(memberCode);
        UnsealedData unsealedData = null;
        try {
            ICryptoService service = CryptoServiceFactory.createCryptoService();
            unsealedData = service.unseal(mpf, sealedData);
        } catch (CryptoException e) {
            System.out.println(e);
        }
        byte[] decryptedData = unsealedData.getDecryptedData();
        if (null != decryptedData) {
            String rtnString = PKIUtil.byte2UTF8String(decryptedData);
            logger.info("解密后返回报文 = " + rtnString);
            return XmlUtils.convertToJavaBean(rtnString, Pay2bankNotify.class);
        } else {
            String  rtnString = PKIUtil.byte2UTF8String(sealedData.getOriginalData());
            logger.info("解密后返回报文 = " + rtnString);
            return null;
        }
    }

    /**
     * 获取异步通知内容
     * @param httpRequest
     * @return
     */
    public static String genNoticeRequestXml(HttpServletRequest httpRequest) {
        String line = null;
        ServletInputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            is = httpRequest.getInputStream();
            isr = new InputStreamReader(is, "utf-8");
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            logger.error("genRequestXml exception", e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != isr) {
                    isr.close();
                }
                if (null != br) {
                    br.close();
                }
            } catch (Exception e) {
                logger.error("io close exception", e);
            }
        }
        return sb.toString();
    }

    public static Mpf genMpf(String memberCode) {
        Mpf mpf = new Mpf();
        mpf.setFeatureCode(fetureCode);
        mpf.setMemberCode(memberCode);
        return mpf;
    }
}
