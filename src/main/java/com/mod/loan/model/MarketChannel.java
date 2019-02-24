package com.mod.loan.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;

@Table(name = "tb_market_channel")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketChannel {
    @Id
    private Long id;

    /**
     * 名称
     */
    @Column(name = "channel_name")
    private String channelName;

    /**
     * 顺序,越大越靠前
     */
    @Column(name = "channel_idx")
    private Integer channelIdx;

    /**
     * 状态0-关闭，1-启用
     */
    @Column(name = "channel_status")
    private Integer channelStatus;

    /**
     * 推荐产品
     */
    @Column(name = "product_id")
    private Long productId;

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
     * 获取名称
     *
     * @return channel_name - 名称
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 设置名称
     *
     * @param channelName 名称
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    /**
     * 获取顺序,越大越靠前
     *
     * @return channel_idx - 顺序,越大越靠前
     */
    public Integer getChannelIdx() {
        return channelIdx;
    }

    /**
     * 设置顺序,越大越靠前
     *
     * @param channelIdx 顺序,越大越靠前
     */
    public void setChannelIdx(Integer channelIdx) {
        this.channelIdx = channelIdx;
    }

    /**
     * 获取状态0-关闭，1-启用
     *
     * @return channel_status - 状态0-关闭，1-启用
     */
    public Integer getChannelStatus() {
        return channelStatus;
    }

    /**
     * 设置状态0-关闭，1-启用
     *
     * @param channelStatus 状态0-关闭，1-启用
     */
    public void setChannelStatus(Integer channelStatus) {
        this.channelStatus = channelStatus;
    }

    /**
     * 获取推荐产品
     *
     * @return product_id - 推荐产品
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * 设置推荐产品
     *
     * @param productId 推荐产品
     */
    public void setProductId(Long productId) {
        this.productId = productId;
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