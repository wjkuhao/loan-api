package com.mod.loan.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_market_flow")
public class MarketFlow {
    @Id
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    /**
     * 日期
     */
    @Column(name = "flow_date")
    private Date flowDate;

    /**
     * 独立访客
     */
    @Column(name = "flow_uv")
    private Long flowUv;

    @Column(name = "create_time")
    private Date createTime;

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
     * @return product_id
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * @param productId
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * 获取日期
     *
     * @return flow_date - 日期
     */
    public Date getFlowDate() {
        return flowDate;
    }

    /**
     * 设置日期
     *
     * @param flowDate 日期
     */
    public void setFlowDate(Date flowDate) {
        this.flowDate = flowDate;
    }

    /**
     * 获取独立访客
     *
     * @return flow_uv - 独立访客
     */
    public Long getFlowUv() {
        return flowUv;
    }

    /**
     * 设置独立访客
     *
     * @param flowUv 独立访客
     */
    public void setFlowUv(Long flowUv) {
        this.flowUv = flowUv;
    }

    /**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}