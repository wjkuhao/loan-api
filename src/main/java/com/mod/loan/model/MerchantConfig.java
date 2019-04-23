package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_merchant_config")
public class MerchantConfig {

    @Id
    private Integer id;

    @Column(name = "merchant")
    private String merchant;

    @Column(name = "mx_risk_token")
    private String mxRiskToken;

    @Column(name = "mx_risk_renew_token")
    private String mxRiskRenewToken;

    @Column(name = "h5_url")
    private String h5Url;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getMxRiskToken() {
        return mxRiskToken;
    }

    public void setMxRiskToken(String mxRiskToken) {
        this.mxRiskToken = mxRiskToken;
    }

    public String getMxRiskRenewToken() {
        return mxRiskRenewToken;
    }

    public void setMxRiskRenewToken(String mxRiskRenewToken) {
        this.mxRiskRenewToken = mxRiskRenewToken;
    }

    public String getH5Url() {
        return h5Url;
    }

    public void setH5Url(String h5Url) {
        this.h5Url = h5Url;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}