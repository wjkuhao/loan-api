package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * @author NIELIN
 * @version $Id: AliAppH5RepayRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class AliAppH5RepayRequest extends BaseRequest {

    /**
     * 交易金额
     */
    private BigDecimal amount;

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
