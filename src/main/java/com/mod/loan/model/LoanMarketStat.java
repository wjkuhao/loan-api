package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_loan_market_stat")
public class LoanMarketStat {
    @Id
    private Long id;
    /**
     * 商户名称
     */
    @Column(name = "merchant")
    private String merchant;
    /**
     * 贷超链接
     */
    @Column(name = "loan_market_url")
    private String loanMarketUrl;
    /**
     * pv统计
     */
    @Column(name = "loan_market_pv")
    private Integer loanMarketPv;
    /**
     * uv统计
     */
    @Column(name = "loan_market_uv")
    private Integer loanMarketUv;
    /**
     * 统计日期
     */
    @Column(name = "stat_date")
    private String statDate;
    /**
     * 更新时
     */
    @Column(name = "update_time")
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getLoanMarketUrl() {
        return loanMarketUrl;
    }

    public void setLoanMarketUrl(String loanMarketUrl) {
        this.loanMarketUrl = loanMarketUrl;
    }

    public Integer getLoanMarketPv() {
        return loanMarketPv;
    }

    public void setLoanMarketPv(Integer loanMarketPv) {
        this.loanMarketPv = loanMarketPv;
    }

    public Integer getLoanMarketUv() {
        return loanMarketUv;
    }

    public void setLoanMarketUv(Integer loanMarketUv) {
        this.loanMarketUv = loanMarketUv;
    }

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}