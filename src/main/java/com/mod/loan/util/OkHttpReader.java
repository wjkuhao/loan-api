package com.mod.loan.util;

import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpReader implements Closeable {

    private static Logger LOGGER = LoggerFactory.getLogger(OkHttpReader.class);

    public static void main(String[] args) {
        OkHttpReader okHttpReader = new OkHttpReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Mall-Token", "929dxacmcicxattzekg9j1btt3eqtfes");
        String str = okHttpReader.get(" http://credit.whtianbei.com/api/mf/get-report",
                "phone=15872773988&name=殷俊峰&idcard=421202199406011214",
                headers);
        System.out.println(str);
        okHttpReader.close();
    }

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final MediaType PLAIN_TEXT_TYPE = MediaType.parse("text/plain; charset=utf-8");

    // instance block, initial okhttp client.
    {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .build();
    }

    private OkHttpClient client;

    public String get(String url, String params, Map<String, String> headers) {
        String result = "";

        if (StringUtils.isEmpty(url)) return result;

        if (StringUtils.isNotEmpty(params)) {
            if (url.contains("?"))
                url = String.format("%s&%s", url, params);
            else
                url = String.format("%s?%s", url, params);
        }

        Request.Builder builder = new Request.Builder().url(url).get();

        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            LOGGER.error("okhttp get throw Exception, url: {}, error: {}", url, e.getMessage());
        }

        return result;
    }

    public String postForm(String url, Map<String, Object> params, Map<String, String> headers) {
        String result = "";

        if (StringUtils.isEmpty(url)) return result;


        FormBody.Builder formBuilder = new FormBody.Builder();
        if (null != params && params.size() > 0) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                formBuilder.add(param.getKey(), null == param.getValue() ? "" : param.getValue().toString());
            }
        }


        Request.Builder builder = new Request.Builder().url(url).post(formBuilder.build());
        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            LOGGER.error("okhttp postForm throw Exception, url: {}, params: {}, error: {}", url, params, e.getMessage());
        }

        return result;
    }

    public String postJson(String url, String json, Map<String, String> headers) {
        String result = "";

        if (StringUtils.isEmpty(url)) return result;

        RequestBody body = RequestBody.create(JSON_TYPE, json);

        Request.Builder builder = new Request.Builder().url(url).post(body);
        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            LOGGER.error("okhttp postJson throw Exception, url: {}, json: {}, error: {}", url, json, e.getMessage());
        }

        return result;
    }

    public String postByteArray(String url, byte[] byteArray, Map<String, String> headers) {
        String result = "";

        if (StringUtils.isEmpty(url)) return result;

        RequestBody body = RequestBody.create(PLAIN_TEXT_TYPE, byteArray);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            result = response.body().string();
        } catch (Exception e) {
            LOGGER.error("okhttp post byte array throw Exception, url: {}, byte: {}, error: {}", url, byteArray, e.getMessage());
        }

        return result;

    }

    public String encodeURL(String param) {
        String encodeParam = null;
        try {
            encodeParam = URLEncoder.encode(param, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("okhttp url encode throw Exception, param: {}, error: {}", param, e.getMessage());
        }
        return encodeParam;
    }

    @Override
    public void close() {
        client = null;
    }

}
