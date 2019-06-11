package com.mod.loan.util.heli;

import okhttp3.*;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpClientService {

    private static Logger logger = LoggerFactory.getLogger(HttpClientService.class);

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

    public static String getHttpResp(Map<String, String> reqMap, String httpUrl) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(httpUrl);
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(300000));
        String response = "";
        try {
            NameValuePair[] nvps = getNameValuePair(reqMap);
            method.setRequestBody(nvps);
            int rescode = client.executeMethod(method);
            if (rescode == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
                String curline = "";
                while ((curline = reader.readLine()) != null) {
                    response += curline;
                }
            }
        } catch (Exception e) {
            logger.error("httpUrl={}, reqMap={},error={}",httpUrl, reqMap.toString(), e.getStackTrace());
        } finally {
            method.releaseConnection();
        }
        return response;
    }

    public static NameValuePair[] getNameValuePair(Map<String, String> bean) {
        List<NameValuePair> x = new ArrayList<NameValuePair>();
        for (Iterator<String> iterator = bean.keySet().iterator(); iterator.hasNext(); ) {
            String type = (String) iterator.next();
            x.add(new NameValuePair(type, String.valueOf(bean.get(type))));
        }
        Object[] y = x.toArray();
        NameValuePair[] n = new NameValuePair[y.length];
        System.arraycopy(y, 0, n, 0, y.length);
        return n;
    }

    public static String getHttpResp(Map<String, String> reqMap, String httpUrl, File file) {
        RequestBody requestBody = null;
        if (null == file) {
            FormBody.Builder builder = new FormBody.Builder();
            for (Iterator<String> iterator = reqMap.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                builder.add(key, reqMap.get(key));
            }
            requestBody = builder.build();
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            for (Iterator<String> iterator = reqMap.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                builder.addFormDataPart(key, reqMap.get(key));
            }
            requestBody = builder.setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPG, file))
                    .build();
        }

        Request request = new Request.Builder() // okHttp post
                .url(httpUrl)
                .post(requestBody)
                .build();

        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                ResponseBody body = response.body();
                if (body == null) {
                    throw new RuntimeException("响应 body 体为空");
                }
                return body.string();
            }
        } catch (IOException e) {
            throw new RuntimeException("请求出错", e);
        }
        return "";
    }
}
