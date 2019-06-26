package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_app_config")
public class AppConfig {

    @Id
    private Integer id;

    @Column(name = "merchant")
    private String merchant;

    @Column(name = "h5_url")
    private String h5Url;

    @Column(name = "youdun_callback_url")
    private String youDunCallbackUrl;

    @Column(name = "youdun_key")
    private String youDunKey;

    @Column(name = "tongdun_url")
    private String tongdunUrl;

    @Column(name = "operators_url")
    private String operatorsUrl;

    @Column(name = "taobao_url")
    private String taoBaoUrl;

    @Column(name = "service_url")
    private String serviceUrl;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

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

    public String getH5Url() {
        return h5Url;
    }

    public void setH5Url(String h5Url) {
        this.h5Url = h5Url;
    }

    public String getYouDunCallbackUrl() {
        return youDunCallbackUrl;
    }

    public void setYouDunCallbackUrl(String youDunCallbackUrl) {
        this.youDunCallbackUrl = youDunCallbackUrl;
    }

    public String getOperatorsUrl() {
        return operatorsUrl;
    }

    public void setOperatorsUrl(String operatorsUrl) {
        this.operatorsUrl = operatorsUrl;
    }

    public String getTaoBaoUrl() {
        return taoBaoUrl;
    }

    public void setTaoBaoUrl(String taoBaoUrl) {
        this.taoBaoUrl = taoBaoUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
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

    public String getYouDunKey() {
        return youDunKey;
    }

    public void setYouDunKey(String youDunKey) {
        this.youDunKey = youDunKey;
    }

    public String getTongdunUrl() {
        return tongdunUrl;
    }

    public void setTongdunUrl(String tongdunUrl) {
        this.tongdunUrl = tongdunUrl;
    }
}