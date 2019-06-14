package com.mod.loan.util.changjie;

import com.mod.loan.util.TimeUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NIELIN
 * @version $Id: BaseParameter.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class BaseParameter {
    /**
     * 封装公共请求参数
     *
     * @param service   服务名称
     * @param partnerId 商户id
     * @param version   版本
     * @return Map<String   ,   String>   返回类型
     * @Title: requestBaseParam
     */
    public static Map<String, String> requestBaseParameter(String service, String version, String partnerId) {
        Map<String, String> origMap = new HashMap<String, String>();
        origMap.put(BaseConstant.SERVICE, service);
        origMap.put(BaseConstant.VERSION, version);
        origMap.put(BaseConstant.PARTNER_ID, partnerId);
        origMap.put(BaseConstant.TRADE_DATE, TimeUtils.parseTime(new Date(), TimeUtils.dateformat4));
        origMap.put(BaseConstant.TRADE_TIME, TimeUtils.parseTime(new Date(), TimeUtils.dateformat8));
        origMap.put(BaseConstant.INPUT_CHARSET, BaseConstant.CHARSET);
        origMap.put(BaseConstant.MEMO, "");
        return origMap;
    }
}
