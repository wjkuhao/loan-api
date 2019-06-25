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

    @Column(name = "overdue_blacklist_day")
    private String overdueBlacklistDay;

    @Column(name = "`reject_keyword`")
    private String rejectKeyword;

    @Column(name = "`ident_invalid_day`")
    private Integer identInvalidDay;

    @Column(name = "auto_apply_order")
    private Integer autoApplyOrder;

    @Column(name = "service_phone")
    private String servicePhone;

    @Column(name = "default_origin_status")
    private Integer defaultOriginStatus;

    @Column(name = "promote_quota_type")
    private Integer promoteQuotaType;

    @Column(name = "old_customer_risk")
    private Integer oldCustomerRisk;

    @Column(name = "max_overdue_fee_rate")
    private Integer maxOverdueFeeRate;

    public Integer getPromoteQuotaType() {
        return promoteQuotaType;
    }

    public void setPromoteQuotaType(Integer promoteQuotaType) {
        this.promoteQuotaType = promoteQuotaType;
    }

    public String getOverdueBlacklistDay() {
        return overdueBlacklistDay;
    }

    public void setOverdueBlacklistDay(String overdueBlacklistDay) {
        this.overdueBlacklistDay = overdueBlacklistDay;
    }

    public String getRejectKeyword() {
        return rejectKeyword;
    }

    public void setRejectKeyword(String rejectKeyword) {
        this.rejectKeyword = rejectKeyword;
    }

    public Integer getIdentInvalidDay() {
        return identInvalidDay;
    }

    public void setIdentInvalidDay(Integer identInvalidDay) {
        this.identInvalidDay = identInvalidDay;
    }

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

    public Integer getAutoApplyOrder() {
        return autoApplyOrder;
    }

    public void setAutoApplyOrder(Integer autoApplyOrder) {
        this.autoApplyOrder = autoApplyOrder;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public void setServicePhone(String servicePhone) {
        this.servicePhone = servicePhone;
    }

    public Integer getDefaultOriginStatus() {
        return defaultOriginStatus;
    }

    public void setDefaultOriginStatus(Integer defaultOriginStatus) {
        this.defaultOriginStatus = defaultOriginStatus;
    }

    public Integer getOldCustomerRisk() {
        return oldCustomerRisk;
    }

    public void setOldCustomerRisk(Integer oldCustomerRisk) {
        this.oldCustomerRisk = oldCustomerRisk;
    }

    public Integer getMaxOverdueFeeRate() {
        return maxOverdueFeeRate;
    }

    public void setMaxOverdueFeeRate(Integer maxOverdueFeeRate) {
        this.maxOverdueFeeRate = maxOverdueFeeRate;
    }
}