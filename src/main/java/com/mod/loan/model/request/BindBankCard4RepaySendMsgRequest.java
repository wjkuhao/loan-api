package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * @author NIELIN
 * @version $Id: BindBankCard4RepaySendMsgRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class BindBankCard4RepaySendMsgRequest extends BaseRequest {

    /**
     * 银行卡号
     */
    private String bankCardNo;
    /**
     * 商品名称
     */
    private String merchantName;
    /**
     * 交易金额
     */
    private BigDecimal amount;

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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
