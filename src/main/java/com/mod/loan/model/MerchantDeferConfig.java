package com.mod.loan.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商户续期功能相关配置
 *
 * @author kibear
 */
@Table(name = "tb_merchant_defer_config")
public class MerchantDeferConfig {
    //
    //`id` int(11) NOT NULL AUTO_INCREMENT,
    //`merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
    //`status` TINYINT(1) DEFAULT 0 COMMENT '续期开关: 0-禁用 1-启用',
    //`daily_defer_rate` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费率:百分比',
    //`daily_defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费',
    //`daily_other_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日额外费',
    //`max_defer_times` TINYINT(2) DEFAULT NULL COMMENT '最大续期次数',
    //`create_time` CHAR(19) DEFAULT NULL COMMENT '插入时间',
    //`update_time` CHAR(19) DEFAULT NULL COMMENT '更新时间',
    //`remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String merchant;
    private Integer status;
    private Double dailyDeferRate;
    private Double dailyDeferFee;
    private Double dailyOtherFee;
    private Integer maxDeferTimes;
    private String createTime;
    private String updateTime;
    private String remark;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getDailyDeferRate() {
        return dailyDeferRate;
    }

    public void setDailyDeferRate(Double dailyDeferRate) {
        this.dailyDeferRate = dailyDeferRate;
    }

    public Double getDailyDeferFee() {
        return dailyDeferFee;
    }

    public void setDailyDeferFee(Double dailyDeferFee) {
        this.dailyDeferFee = dailyDeferFee;
    }

    public Double getDailyOtherFee() {
        return dailyOtherFee;
    }

    public void setDailyOtherFee(Double dailyOtherFee) {
        this.dailyOtherFee = dailyOtherFee;
    }

    public Integer getMaxDeferTimes() {
        return maxDeferTimes;
    }

    public void setMaxDeferTimes(Integer maxDeferTimes) {
        this.maxDeferTimes = maxDeferTimes;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
