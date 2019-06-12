package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: BindBankCard4UnbindRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class BindBankCard4UnbindRequest extends BaseRequest {

    /**
     * 解绑卡的前6位
     */
    private String cardBegin;
    /**
     * 解绑卡的后4位
     */
    private String cardEnd;

    public String getCardBegin() {
        return cardBegin;
    }

    public void setCardBegin(String cardBegin) {
        this.cardBegin = cardBegin;
    }

    public String getCardEnd() {
        return cardEnd;
    }

    public void setCardEnd(String cardEnd) {
        this.cardEnd = cardEnd;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
