package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: BindBankCard4RepayConfirmRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class BindBankCard4RepayConfirmRequest extends BaseRequest {

    /**
     * 协议支付的流水号
     */
    private String seriesNo;
    /**
     * 短信验证码
     */
    private String smsCode;

    public String getSeriesNo() {
        return seriesNo;
    }

    public void setSeriesNo(String seriesNo) {
        this.seriesNo = seriesNo;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
