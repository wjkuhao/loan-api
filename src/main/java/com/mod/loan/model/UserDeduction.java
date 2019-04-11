package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_user_deduction")
public class UserDeduction {
    private Long id;

    /**
     * 注册来源
     */
    @Column(name = "user_origin")
    private String userOrigin;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;


    @Column(name = "merchant")
    private String merchant;

    @Column(name = "user_phone")
    private String userPhone;

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserOrigin() {
        return userOrigin;
    }

    public void setUserOrigin(String userOrigin) {
        this.userOrigin = userOrigin;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }
}