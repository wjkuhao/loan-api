package com.mod.loan.util.heli.vo.dto;

/**
 * @Auther: wzg
 * @Date: 2019-05-12 14:56
 * @Description:绑卡支付发送短信验证码
 */
public class BindPaySmsCodeDto {

    private String repayNo;
    private String amount;
    private String hlbId;
    private String foreignId;
    private String userId;
    private String phone;
    private String merchant;
    private String orderId;

    public String getRepayNo() {
        return repayNo;
    }

    public void setRepayNo(String repayNo) {
        this.repayNo = repayNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getHlbId() {
        return hlbId;
    }

    public void setHlbId(String hlbId) {
        this.hlbId = hlbId;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BindPaySmsCodeDto(String repayNo, String amount, String hlbId, String foreignId, String userId, String phone, String merchant, String orderId) {
        this.repayNo = repayNo;
        this.amount = amount;
        this.hlbId = hlbId;
        this.foreignId = foreignId;
        this.userId = userId;
        this.phone = phone;
        this.merchant = merchant;
        this.orderId = orderId;
    }
}
