package com.mod.loan.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;

@Table(name = "tb_market_product")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketProduct {
    @Id
    private Long id;

    @Column(name = "module_id")
    private Long moduleId;

    /**
     * 名称
     */
    @Column(name = "product_name")
    private String productName;

    /**
     * 图片
     */
    @Column(name = "product_img")
    private String productImg;

    /**
     * 链接地址
     */
    @Column(name = "product_url")
    private String productUrl;

    /**
     * 标语
     */
    @Column(name = "product_slogan")
    private String productSlogan;

    /**
     * 类型 1:hot 2:new 3:活动 4:普通
     */
    @Column(name = "product_type")
    private Integer productType;

    /**
     * 状态，  0：下线  1：上线
     */
    @Column(name = "product_status")
    private Integer productStatus;

    /**
     * 排序
     */
    @Column(name = "product_idx")
    private Integer productIdx;

    /**
     * 额度下限
     */
    @Column(name = "loan_min")
    private BigDecimal loanMin;

    /**
     * 额度上限
     */
    @Column(name = "loan_max")
    private BigDecimal loanMax;

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


    private Integer num;
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
     * @return module_id
     */
    public Long getModuleId() {
        return moduleId;
    }

    /**
     * @param moduleId
     */
    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * 获取名称
     *
     * @return product_name - 名称
     */
    public String getProductName() {
        return productName;
    }

    /**
     * 设置名称
     *
     * @param productName 名称
     */
    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    /**
     * 获取图片
     *
     * @return product_img - 图片
     */
    public String getProductImg() {
        return productImg;
    }

    /**
     * 设置图片
     *
     * @param productImg 图片
     */
    public void setProductImg(String productImg) {
        this.productImg = productImg == null ? null : productImg.trim();
    }

    /**
     * 获取链接地址
     *
     * @return product_url - 链接地址
     */
    public String getProductUrl() {
        return productUrl;
    }

    /**
     * 设置链接地址
     *
     * @param productUrl 链接地址
     */
    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl == null ? null : productUrl.trim();
    }

    /**
     * 获取标语
     *
     * @return product_slogan - 标语
     */
    public String getProductSlogan() {
        return productSlogan;
    }

    /**
     * 设置标语
     *
     * @param productSlogan 标语
     */
    public void setProductSlogan(String productSlogan) {
        this.productSlogan = productSlogan == null ? null : productSlogan.trim();
    }

    /**
     * 获取类型 1:hot 2:new 3:活动 4:普通
     *
     * @return product_type - 类型 1:hot 2:new 3:活动 4:普通
     */
    public Integer getProductType() {
        return productType;
    }

    /**
     * 设置类型 1:hot 2:new 3:活动 4:普通
     *
     * @param productType 类型 1:hot 2:new 3:活动 4:普通
     */
    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    /**
     * 获取状态，  0：下线  1：上线
     *
     * @return product_status - 状态，  0：下线  1：上线
     */
    public Integer getProductStatus() {
        return productStatus;
    }

    /**
     * 设置状态，  0：下线  1：上线
     *
     * @param productStatus 状态，  0：下线  1：上线
     */
    public void setProductStatus(Integer productStatus) {
        this.productStatus = productStatus;
    }

    /**
     * 获取排序
     *
     * @return product_idx - 排序
     */
    public Integer getProductIdx() {
        return productIdx;
    }

    /**
     * 设置排序
     *
     * @param productIdx 排序
     */
    public void setProductIdx(Integer productIdx) {
        this.productIdx = productIdx;
    }

    /**
     * 获取额度下限
     *
     * @return loan_min - 额度下限
     */
    public BigDecimal getLoanMin() {
        return loanMin;
    }

    /**
     * 设置额度下限
     *
     * @param loanMin 额度下限
     */
    public void setLoanMin(BigDecimal loanMin) {
        this.loanMin = loanMin;
    }

    /**
     * 获取额度上限
     *
     * @return loan_max - 额度上限
     */
    public BigDecimal getLoanMax() {
        return loanMax;
    }

    /**
     * 设置额度上限
     *
     * @param loanMax 额度上限
     */
    public void setLoanMax(BigDecimal loanMax) {
        this.loanMax = loanMax;
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

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}
    
    
}