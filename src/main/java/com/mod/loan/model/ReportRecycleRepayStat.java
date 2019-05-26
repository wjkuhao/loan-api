package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "report_recycle_repay_stat")
public class ReportRecycleRepayStat {
    /**
     * 主键
     */
    @Id
    private Long id;

    private String merchant;

    @Column(name = "recycled_id")
    private Long recycledId;

    @Column(name = "recycled_name")
    private String  recycledName;

    @Column(name = "recycle_cnt")
    private Integer recycleCnt;

    @Column(name = "not_return_cnt")
    private Integer notReturnCnt;

    @Column(name = "overdue_day")
    private Integer overdueDay;

    @Column(name = "repay_1_rate")
    private Double repay_1_rate;

    @Column(name = "repay_3_rate")
    private Double repay_3_rate;

    @Column(name = "repay_7_rate")
    private Double repay_7_rate;

    @Column(name = "repay_60_rate")
    private Double repay_60_rate;

    @Column(name = "stat_date")
    private String statDate;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

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

    public Long getRecycledId() {
        return recycledId;
    }

    public void setRecycledId(Long recycledId) {
        this.recycledId = recycledId;
    }

    public String getRecycledName() {
        return recycledName;
    }

    public void setRecycledName(String recycledName) {
        this.recycledName = recycledName;
    }

    public Integer getRecycleCnt() {
        return recycleCnt;
    }

    public void setRecycleCnt(Integer recycleCnt) {
        this.recycleCnt = recycleCnt;
    }

    public Integer getNotReturnCnt() {
        return notReturnCnt;
    }

    public void setNotReturnCnt(Integer notReturnCnt) {
        this.notReturnCnt = notReturnCnt;
    }

    public Integer getOverdueDay() {
        return overdueDay;
    }

    public void setOverdueDay(Integer overdueDay) {
        this.overdueDay = overdueDay;
    }

    public Double getRepay_1_rate() {
        return repay_1_rate;
    }

    public void setRepay_1_rate(Double repay_1_rate) {
        this.repay_1_rate = repay_1_rate;
    }

    public Double getRepay_3_rate() {
        return repay_3_rate;
    }

    public void setRepay_3_rate(Double repay_3_rate) {
        this.repay_3_rate = repay_3_rate;
    }

    public Double getRepay_7_rate() {
        return repay_7_rate;
    }

    public void setRepay_7_rate(Double repay_7_rate) {
        this.repay_7_rate = repay_7_rate;
    }

    public Double getRepay_60_rate() {
        return repay_60_rate;
    }

    public void setRepay_60_rate(Double repay_60_rate) {
        this.repay_60_rate = repay_60_rate;
    }

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}