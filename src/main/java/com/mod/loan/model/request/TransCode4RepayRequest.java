package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * @author NIELIN
 * @version $Id: TransCode4RepayRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class TransCode4RepayRequest extends BaseRequest {

    /**
     * 银行名称
     */
    private String bankName;
    /**
     * 银行卡号
     */
    private String bankCardNo;
    /**
     * 身份证号
     */
    private String idNo;
    /**
     * 持卡人姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 交易金额
     */
    private BigDecimal amount;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
