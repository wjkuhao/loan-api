package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: BindBankCard4QueryRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class BindBankCard4QueryRequest extends BaseRequest {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
