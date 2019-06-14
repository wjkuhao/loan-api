package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_merchant_origin")
public class MerchantOrigin {
    @Id
    private Long id;

    /**
     * 所属商户
     */
    private String merchant;

    /**
     * 渠道别名
     */
    @Column(name = "origin_name")
    private String originName;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "deduction_rate")
    private Integer deductionRate;

    private Integer status;

    private String mxRiskToken;

    @Column(name = "check_repay")
    private Integer checkRepay;

    @Column(name = "check_overdue")
    private Integer checkOverdue;

    @Column(name = "check_blacklist")
    private Integer checkBlacklist;

    @Column(name = "sms_merchant")
    private String smsMerchant;

    public String getSmsMerchant() {
        return smsMerchant;
    }

    public void setSmsMerchant(String smsMerchant) {
        this.smsMerchant = smsMerchant;
    }

    public String getMxRiskToken() {
        return mxRiskToken;
    }

    public void setMxRiskToken(String mxRiskToken) {
        this.mxRiskToken = mxRiskToken;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getDeductionRate() {
        return deductionRate;
    }

    public void setDeductionRate(Integer deductionRate) {
        this.deductionRate = deductionRate;
    }

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * 获取所属商户
     *
     * @return merchant - 所属商户
     */
    public String getMerchant() {
        return merchant;
    }

    /**
     * 设置所属商户
     *
     * @param merchant 所属商户
     */
    public void setMerchant(String merchant) {
        this.merchant = merchant == null ? null : merchant.trim();
    }

    /**
     * 获取渠道别名
     *
     * @return origin_name - 渠道别名
     */
    public String getOriginName() {
        return originName;
    }

    /**
     * 设置渠道别名
     *
     * @param originName 渠道别名
     */
    public void setOriginName(String originName) {
        this.originName = originName == null ? null : originName.trim();
    }

	/**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCheckRepay() {
        return checkRepay;
    }

    public void setCheckRepay(Integer checkRepay) {
        this.checkRepay = checkRepay;
    }

    public Integer getCheckOverdue() {
        return checkOverdue;
    }

    public void setCheckOverdue(Integer checkOverdue) {
        this.checkOverdue = checkOverdue;
    }

    public Integer getCheckBlacklist() {
        return checkBlacklist;
    }

    public void setCheckBlacklist(Integer checkBlacklist) {
        this.checkBlacklist = checkBlacklist;
    }
}