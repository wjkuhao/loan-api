package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: TransCode4QueryRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public class TransCode4QueryRequest extends BaseRequest {

    /**
     * 原交易申请时流水号
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
