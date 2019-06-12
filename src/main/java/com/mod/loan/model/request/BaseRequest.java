package com.mod.loan.model.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author NIELIN
 * @version $Id: BaseRequest.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public abstract class BaseRequest {

    /**
     * 商户生成公私钥，公钥上传给第三方平台，私钥代码加密
     * 第三方平台的公钥进行验签
     */

    /**
     * 公钥--第三方平台的自己的公钥
     */
    private String publicKey;
    /**
     * 私钥--商户生成的私钥（代付代扣都可用）
     */
    private String privateKey;
    /**
     * 商户id
     */
    private String partnerId;
    /**
     * 每次请求唯一流水号
     */
    private String requestSeriesNo;
    /**
     * 私钥--商户生成的私钥（代扣）
     */
    private String privateKey4Repay;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getRequestSeriesNo() {
        return requestSeriesNo;
    }

    public void setRequestSeriesNo(String requestSeriesNo) {
        this.requestSeriesNo = requestSeriesNo;
    }

    public String getPrivateKey4Repay() {
        return privateKey4Repay;
    }

    public void setPrivateKey4Repay(String privateKey4Repay) {
        this.privateKey4Repay = privateKey4Repay;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
