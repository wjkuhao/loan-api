package com.mod.loan.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 续期订单表
 *
 * @author kibear
 * @since 1.8
 */
@Table(name = "tb_order_defer")
public class OrderDefer {
    //
    //`id` int(11) NOT NULL AUTO_INCREMENT,
    //`order_id` bigint(20) DEFAULT NULL COMMENT '订单号',
    //`user_name` VARCHAR(30) DEFAULT NULL COMMENT '姓名',
    //`user_phone` VARCHAR(20) DEFAULT NULL COMMENT '手机',
    //`defer_day` TINYINT(2) default NUll COMMENT '续期天数',
    //`daily_defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费',
    //`defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '续期费',
    //`defer_times` TINYINT(2) DEFAULT NULL COMMENT '当前第几次续期',
    //`pay_type` CHAR(10) DEFAULT NULL COMMENT '支付方式:线上/线下',
    //`pay_no` VARCHAR(255) DEFAULT NULL COMMENT '支付单号:线上支付',
    //`pay_status` TINYINT(1) DEFAULT 0 COMMENT '续期订单状态:0-未支付 1-已支付',
    //`pay_time` CHAR(19) DEFAULT NULL COMMENT '续期支付时间',
    //`create_time` CHAR(19) DEFAULT NULL COMMENT '续期申请时间',
    //`repay_date` CHAR(10) DEFAULT NULL COMMENT '原始到期日',
    //`defer_repay_date` CHAR(10) DEFAULT NULL COMMENT '续期到期日',
    //`remark` VARCHAR(255) DEFAULT null COMMENT '备注',
    //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long orderId;
    private String userName;
    private String userPhone;
    private Integer deferDay;
    private Double dailyDeferFee;
    private Double deferFee;
    private Integer deferTimes;
    private String payType;
    private String payNo;
    private Integer payStatus;
    private String payTime;
    private String createTime;
    private String repayDate;
    private String deferRepayDate;
    private String remark;

    public OrderDefer() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Integer getDeferDay() {
        return deferDay;
    }

    public void setDeferDay(Integer deferDay) {
        this.deferDay = deferDay;
    }

    public Double getDailyDeferFee() {
        return dailyDeferFee;
    }

    public void setDailyDeferFee(Double dailyDeferFee) {
        this.dailyDeferFee = dailyDeferFee;
    }

    public Double getDeferFee() {
        return deferFee;
    }

    public void setDeferFee(Double deferFee) {
        this.deferFee = deferFee;
    }

    public Integer getDeferTimes() {
        return deferTimes;
    }

    public void setDeferTimes(Integer deferTimes) {
        this.deferTimes = deferTimes;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getDeferRepayDate() {
        return deferRepayDate;
    }

    public void setDeferRepayDate(String deferRepayDate) {
        this.deferRepayDate = deferRepayDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
