package com.mod.loan.util.helientrusted;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * @Auther: wzg
 * @Date: 2019-05-29 16:50
 * @Description:
 */
public class HelipayConstant {

    protected final Log logger = LogFactory.getLog(this.getClass());

    //public static final String r1_merchantNoAppPay="C1800001110";

    //public static final String signkey="UICnjdgk6yAupbxpcBbTbC46dQKN21Jb";

    //public static final String REQUEST_URL = "http://transfer.trx.helipay.com/trx/entrustedLoan/interface.action";
    //public static final String REQUEST_FILEURL = "http://transfer.trx.helipay.com/trx/entrustedLoan/upload.action";

    public static final String signKey_private ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANZIARIGFaEkQVP3zjylP8403COQjtz0KpPjYVv3tulM1EnC1yWBbwYdVp7d76VEEWOvVSqf+2xpiKd9R7oRFU539nqVNCO9bmx7N6LYYmsXot1EpdfwS2vmZw0wMXCl8XQUEFIA86irLCIjHRoAKX/APT/wOqeCjtiDLmdjbG4jAgMBAAECgYEAlDBppRhWHwHet/45P5GcpbPCxkmzszScuXfXne2bLo72hShc99Aigt0JSYb8s5N2XzUjNf47t4bofcRTNWeZDk13VAFXqO2OkWOwTF5VXmNFLeatWJ1LS7DtmHiCRU94Y3CIEh1Wd0osNR42IrxZzO3pXgG3x9JAf+01TPw7CrkCQQD7tIw6/29cdcXvRJuzT6rFlVHd0pFcTxPCucc4Ub/ZWGmnkeIpfaIt/O75u/jANbVExezDhnunVQv79mihneANAkEA2e/84y4fbM6zWw2o15RU38LVOIB2sVXE3PU7IlbqvFrY57t2r2TfmLYrzKqAkBreyisig33pBmIK2owo+bbK7wJAaY99pByOOouV+FOgrOHDb9vAmrP9jGYJlofNOhxmxiMjJEi1PctXocvK+WdQ2mNr4yzSr+mNoE61mPTtSqJoXQJAbzwojSO7cmPF85FzPiU9dMAS6DXZXwXx5v1b41kNknmkABMUrs0DuwQwMtRLrkGouPNvOEIJr/JWq9miCGKeDQJATnXaVI6S+gghGYjZL1hmOiuNYT1N7PYgjaA4h3pFeBc8FwQ177mieZ5CCOahUqPjwUHzG8tGlH/un80cA+JFkQ==";
//	public static final String signKey_private ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAM+PcQN9JmrYJ+sKwI9kmBLAsBS6HHDT2Sgkh8BYbyTKovSMQat1II6l42HhUH6lr7bXwnRUw8I4qrNBZjz4cWZBi+vgkSL/1f1M/erW27t61DobvpgDpZQTtmQ7IDsiLuY7C7We+WwaBcddnju74ij3FPWCpgYBHGwdv5wwzRxdAgMBAAECgYBAzSreiPsujm/gDQpTeneUGz6eKgDpJOr+gnEzlyiUFwPLT+LM0hOpFZepHnxQHhB/CFu4kCJSB/kbYAa4cGSOlPo8zBLCfNajClZMLaKMAIb+0TmYNAnVcadC/4fXibzAW0zRS2/OK4H7wWUVEYyC66m+ieBaH5Jt/72+e6aYTQJBAPjjhGanLk22ml8i5+MzN94RBQStbGNxI6xtBXoKEIB2W/INPddZ877e7tknh+fVvctTZlE4Q5V1TT2ZL4wzke8CQQDVfaE9Cbc+aeg3Mb+Ap64tCK4WTHhWzHySN7VGTLdeF41ZjqTrIS7SSQyZOPOt/lMfFgXO0EnSdCqL+aexXFJzAkBeHyxi5bZNDVEzyS+IbEYkZKtRKYRj1tV2z4PSsxuqeRgsYXWRiyLye7w3wwtSUTKFQfTfojdsvf+H2/ZvPtFhAkAMygfctjZKAOIuXEaSmHjwrbJwF4il+n4D7F5ppbLeah7HnKn4g/ZgFowwqZ6/b5rfI9yZNRUXDGp4FC6di2BNAkB572zRbBT5Ot9mx9xVg6g/t0s3+LLEs1LBFEWQatRR9oC6qUzGNKTnZ/d5254ngnYXSRaQEZT698cJQV7kvmg4";

    public static final String deskey_key = "xglHJa0H1QKQi1Z9aaqnL0l2";

    public static final String split="&";

    public final static String BUSSINESS = "B2C";

    public static final String tempDir = System.getProperty("java.io.tmpdir");

    public Map<String, Object> getPostJson(HttpServletRequest request) throws IOException {
        java.io.ByteArrayOutputStream inBuffer = new java.io.ByteArrayOutputStream();
        java.io.InputStream input = request.getInputStream();
        byte[] tmp = new byte[1024];
        int len = 0;
        while ((len = input.read(tmp)) > 0) {
            inBuffer.write(tmp, 0, len);
        }
        byte[] requestData = inBuffer.toByteArray();
        String requestJsonStr = new String(requestData, "UTF-8");
        logger.info(requestJsonStr);
        JSONObject requestJson = JSON.parseObject(requestJsonStr);
        logger.info(requestJson);
        return parseJSON2Map(requestJson);
    }

    public Map<String, Object> parseJSON2Map(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (json != null) {
            for (Object k : json.keySet()) {
                Object v = json.get(k);
                // 如果内层还是数组的话，继续解析
                if (v instanceof JSONArray) {
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    Iterator<Object> it = ((JSONArray) v).iterator();
                    while (it.hasNext()) {
                        JSONObject json2 = (JSONObject) it.next();
                        list.add(parseJSON2Map(json2));
                    }
                    map.put(k.toString(), list);
                } else {
                    map.put(k.toString(), v);
                }
            }
        }
        logger.info(map);
        return map;
    }
}
