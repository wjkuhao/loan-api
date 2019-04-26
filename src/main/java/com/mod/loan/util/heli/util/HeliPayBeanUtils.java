package com.mod.loan.util.heli.util;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by heli50 on 2017/4/14.
 */
public class HeliPayBeanUtils extends BeanUtils {

    public static final String SPLIT = "&";

    public static final String SIGNKEY_SAME_PERSON="";

    public static Map convertBean(Object bean, Map retMap)
            throws Exception {
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
                sb.append(SPLIT);
                sb.append(value);
            }
        }
        sb.append(SPLIT);
        sb.append(SIGNKEY_SAME_PERSON);
        return sb.toString();
    }

}
