package com.mod.loan.util.huichao;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author NIELIN
 * @version $Id: HuichaoUtil.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class HuichaoUtil {
    private static Logger logger = LoggerFactory.getLogger(HuichaoUtil.class);

    /**
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
     * 实现步骤: <br>
     *
     * @param paraMap    要排序的Map对象
     * @param urlEncode  是否需要URLENCODE
     * @param keyToLower 是否需要将Key转换为全小写 true:key转化成小写，false:不转化
     * @return
     */
    public static String formatUrlMap(Map<String, String> paraMap, boolean urlEncode, boolean keyToLower) {
        logger.info("#[对参数进行排序、拼接]-[开始]-paraMap={},urlEncode={},keyToLower={}", paraMap, urlEncode, keyToLower);
        String buff = "";
        Map<String, String> tmpMap = paraMap;
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(tmpMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {

                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return (o1.getKey()).toString().compareTo(o2.getKey());
                }
            });
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds) {
                if (StringUtils.isNotBlank(item.getKey())) {
                    String key = item.getKey();
                    String val = item.getValue();
                    if (urlEncode) {
                        val = URLEncoder.encode(val, "utf-8");
                    }
                    if (keyToLower) {
                        buf.append(key.toLowerCase() + "=" + val);
                    } else {
                        buf.append(key + "=" + val);
                    }
                    buf.append("&");
                }

            }
            buff = buf.toString();
            if (buff.isEmpty() == false) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            logger.error("#[对参数进行排序、拼接]-[异常]-e={}", e);
            return null;
        }
        logger.info("#[对参数进行排序、拼接]-[结束]-buff={}", buff);
        return buff;
    }

    /**
     * 对字符串md5加密
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        logger.info("#[对字符串md5加密]-[开始]-str={}", str);
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            logger.error("#[初始化MD5]-[异常]-e={}", e);
            return null;
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        logger.info("#[对字符串md5加密]-[结束]-result={}", hexValue.toString());
        return hexValue.toString();
    }

    /**
     * 接口调用 GET
     */
    public static String sendGet(String GET_URL) {
        logger.info("#[GET请求]-[开始]-url={}", GET_URL);
        String s = null;
        try {
            URL url = new URL(GET_URL);    // 把字符串转换为URL请求地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开连接
            connection.connect();// 连接会话
            // 获取输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {// 循环读取流
                sb.append(line);
            }
            br.close();// 关闭流
            connection.disconnect();// 断开连接
            s = sb.toString();
            logger.info("#[GET请求]-[结束]-s={}", s);
            return s;
        } catch (Exception e) {
            logger.error("#[GET请求]-[异常]-e={}", e);
        }
        return null;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url     发送请求的 URL
     * @param param   请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charset 发送和接收的格式
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param, String charset) {
        logger.info("#[POST请求]-[开始]-url={},param={}", url, param);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String line;
        StringBuffer sb = new StringBuffer();
        try {
            URL realUrl = new URL(url);
            //如果是https请求,忽略SSL证书
            if ("https".equalsIgnoreCase(realUrl.getProtocol())) {
                SslUtils.ignoreSsl();
            }
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 设置请求格式
            conn.setRequestProperty("contentType", charset);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            //设置超时时间
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应    设置接收格式
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            logger.error("#[POST请求]-[异常]-e={}", e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        logger.info("#[POST请求]-[结束]-result={}", result);
        return result;
    }

    /**
     * @param str：name1=value1&name2=value2类型字符串
     * @return 返回类型：Map数组
     * @author haixin.hu
     */
    @SuppressWarnings("rawtypes")
    public static Map StrToMap(String str) {
        String[] strs = str.split("&");
        Map<String, String> m = new HashMap<String, String>();
        for (String s : strs) {
            String[] ms = s.split("=");
            m.put(ms[0], ms[1]);
        }
        return m;
    }
}
