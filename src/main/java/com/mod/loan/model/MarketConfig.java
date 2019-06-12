package com.mod.loan.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "tb_market_config")
public class MarketConfig {
    @Id
    private Long id;

    /**
     * 渠道别名
     */
    private String name;

    /**
     * 渠道code
     */
    private String code;

    /**
     * 状态0-关闭，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

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
     * 获取渠道别名
     *
     * @return name - 渠道别名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置渠道别名
     *
     * @param name 渠道别名
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取渠道code
     *
     * @return code - 渠道code
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置渠道code
     *
     * @param code 渠道code
     */
    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    /**
     * 获取状态0-关闭，1-启用
     *
     * @return status - 状态0-关闭，1-启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态0-关闭，1-启用
     *
     * @param status 状态0-关闭，1-启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取修改时间
     *
     * @return update_time - 修改时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置修改时间
     *
     * @param updateTime 修改时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}