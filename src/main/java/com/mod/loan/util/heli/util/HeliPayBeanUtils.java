package com.mod.loan.util.heli.util;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.helientrusted.HelipayConstant;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by heli50 on 2017/4/14.
 */
public class HeliPayBeanUtils extends BeanUtils {

    public static final String SPLIT = "&";

    public static final String SIGNKEY_SAME_PERSON = "";

    public static Map convertBean(Object bean, Map retMap) {
        try {
            Class clazz = bean.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
            }
            for (Field f : fields) {
                String key = f.toString().substring(f.toString().lastIndexOf(".") + 1);
                Object value = f.get(bean);
                if (value == null) {
                    value = "";
                }
                retMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retMap;
    }

    public static String getSigned(Map<String, String> map, String[] excludes) {
        StringBuffer sb = new StringBuffer();
        Set<String> excludeSet = new HashSet<String>();
        excludeSet.add("sign");
        if (excludes != null) {
            for (String exclude : excludes) {
                excludeSet.add(exclude);
            }
        }
        for (String key : map.keySet()) {
            if (!excludeSet.contains(key)) {
                String value = map.get(key);
                value = (value == null ? "" : value);
                sb.append(HelipayConstant.split);
                sb.append(value);
            }
        }
        return sb.toString();
    }

//    public static String getSigned(Map<String, String> map, String[] excludes) {
//        StringBuffer sb = new StringBuffer();
//        Set<String> excludeSet = new HashSet<String>();
//        excludeSet.add("sign");
//        if (excludes != null) {
//            for (String exclude : excludes) {
//                excludeSet.add(exclude);
//            }
//        }
//        for (String key : map.keySet()) {
//            if (!excludeSet.contains(key)) {
//                String value = map.get(key);
//                value = (value == null ? "" : value);
//                sb.append(SPLIT);
//                sb.append(value);
//            }
//        }
//        sb.append(SPLIT);
//        sb.append(SIGNKEY_SAME_PERSON);
//        return sb.toString();
//    }


    public static Map convertBean(Object bean, Map retMap, String filterStr) {
        retMap = convertBean(bean, retMap);
        String[] strs = filterStr.split(",");
        for (int i = 0; i < strs.length; i++) {
            retMap.remove(strs[i]);
        }
        return retMap;
    }

    public static String getSigned(Object bean, String[] excludes) {
        Map map = convertBean(bean, new LinkedHashMap());
        String signedStr = getSigned(map, excludes);
        return signedStr;
    }

    /**
     * 计算每期费用
     */
    public static JSONObject getOrderExtend(BigDecimal borrowMoney) {
        //借款时间
        int loanTime = 90;
        //借款利率数值
        int loanInterestRate = 18;
        //借款利率
        int periodization = 3;
        int periodizationDays = loanTime / periodization;
        //分期金额计算公式 =（借款金额+借款金额×借款年利率÷360×借款天数）÷分期数  (borrowMoney + borrowMoney*loanInterestRate/360*loanTime)/periodization;
        BigDecimal periodizationFee = borrowMoney.add(borrowMoney.multiply(new BigDecimal(loanInterestRate)).divide(new BigDecimal(100)).divide(new BigDecimal(360)).multiply(new BigDecimal(loanTime))).divide(new BigDecimal(periodization), 2, BigDecimal.ROUND_HALF_UP);

        JSONObject data = new JSONObject();
        //借款时间
        data.put("loanTime", loanTime);
        //借款时间单位
        data.put("loanTimeUnit", "D");
        //借款利率
        data.put("loanInterestRate", loanInterestRate);
        //周期化
        data.put("periodization", periodization);
        //周期天数
        data.put("periodizationDays", periodizationDays);
        //每期金额
        data.put("periodizationFee", periodizationFee);
        return data;

    }


}
