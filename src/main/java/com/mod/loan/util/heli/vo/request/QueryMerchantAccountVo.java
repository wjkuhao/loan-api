package com.mod.loan.util.heli.vo.request;

/**
 * Created by heli50 on 2017/4/14.
 */
public class QueryMerchantAccountVo {
    private String P1_bizType;
    private String P2_customerNumber;
    private String P3_timestamp;

    public String getP1_bizType() {
        return P1_bizType;
    }

    public void setP1_bizType(String p1_bizType) {
        P1_bizType = p1_bizType;
    }

    public String getP2_customerNumber() {
        return P2_customerNumber;
    }

    public void setP2_customerNumber(String p2_customerNumber) {
        P2_customerNumber = p2_customerNumber;
    }

    public String getP3_timestamp() {
        return P3_timestamp;
    }

    public void setP3_timestamp(String p3_timestamp) {
        P3_timestamp = p3_timestamp;
    }
}
