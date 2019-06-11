package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: AliAppH5RepayQueryRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class AliAppH5RepayQueryRequest extends BaseRequest {

    /**
     * 交易流水号
     */
    private String seriesNo;

    public String getSeriesNo() {
        return seriesNo;
    }

    public void setSeriesNo(String seriesNo) {
        this.seriesNo = seriesNo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
