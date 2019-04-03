package com.mod.loan.controller.tongdun;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.config.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "tongdun")
public class TongdunController {

    private static final Logger logger = LoggerFactory.getLogger(TongdunController.class);

    /**
     * 获取同盾数据魔盒账号配置接口
     */
    @RequestMapping(value = "getConfig")
    public JSONObject getConfig() {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("message", "回调处理成功");
        try {
            JSONObject data = new JSONObject();
            data.put("partnerCode", Constant.MOHE_PARTNER_CODE);
            data.put("partnerKey", Constant.MOHE_PARTNER_KEY);
            data.put("tokenUrl", Constant.MOHE_TOKEN_URL);
            data.put("reportUrl", Constant.MOHE_REPORT_URL);
            data.put("loginReportUrl", Constant.MOHE_LOGIN_REPORT_URL);
            result.put("data", data);
        } catch (Exception e) {
            logger.error("获取同盾账号信息失败", e);
            result.put("code", -1);
            result.put("message", e.getMessage());
        }
        return result;
    }

}