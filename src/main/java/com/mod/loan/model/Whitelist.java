package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_whitelist")
public class Whitelist {
    @Id
    private Long id;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 身份证
     */
    @Column(name = "cert_no")
    private String certNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 创建时间
     */
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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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