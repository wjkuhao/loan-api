package com.mod.loan.util.huiju;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateLinkStringByGet {

    public static String createLinkStringByGet(Map<String, String> params){
        // TODO Auto-generated method stub
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String str1 ="";
        for(int i=0;i<keys.size();i++) {
            String key = keys.get(i);
            Object value = params.get(key);//(String) 强制类型转换
            if(value instanceof Integer) {
                value = (Integer)value;
            }
            if(i==keys.size()-1) {
                str1 = str1+value.toString();
            }else {
                str1 = str1+value;
            }
        }
        return str1;
    }

}
