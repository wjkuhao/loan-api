package com.mod.loan.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;

@Table(name = "tb_market_module")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketModule {
    @Id
    private Long id;

    /**
     * 栏目id
     */
    @Column(name = "channel_id")
    private Long channelId;

    /**
     * 名称
     */
    @Column(name = "module_name")
    private String moduleName;

    /**
     * 状态0-关闭，1-启用
     */
    @Column(name = "module_status")
    private Integer moduleStatus;

    /**
     * 顺序,越大越靠前
     */
    @Column(name = "module_idx")
    private Integer moduleIdx;

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
     * 获取栏目id
     *
     * @return channel_id - 栏目id
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * 设置栏目id
     *
     * @param channelId 栏目id
     */
    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    /**
     * 获取名称
     *
     * @return module_name - 名称
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * 设置名称
     *
     * @param moduleName 名称
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName == null ? null : moduleName.trim();
    }

    /**
     * 获取状态0-关闭，1-启用
     *
     * @return module_status - 状态0-关闭，1-启用
     */
    public Integer getModuleStatus() {
        return moduleStatus;
    }

    /**
     * 设置状态0-关闭，1-启用
     *
     * @param moduleStatus 状态0-关闭，1-启用
     */
    public void setModuleStatus(Integer moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    /**
     * 获取顺序,越大越靠前
     *
     * @return module_idx - 顺序,越大越靠前
     */
    public Integer getModuleIdx() {
        return moduleIdx;
    }

    /**
     * 设置顺序,越大越靠前
     *
     * @param moduleIdx 顺序,越大越靠前
     */
    public void setModuleIdx(Integer moduleIdx) {
        this.moduleIdx = moduleIdx;
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