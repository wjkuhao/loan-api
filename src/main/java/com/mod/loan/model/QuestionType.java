package com.mod.loan.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_question_type")
public class QuestionType {
    @Id
    private Long id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 状态（0，隐藏；1，显示。）
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer idx;

    /**
     * 创建时间
     */
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
     * 获取类型名称
     *
     * @return name - 类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置类型名称
     *
     * @param name 类型名称
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取状态（0，隐藏；1，显示。）
     *
     * @return status - 状态（0，隐藏；1，显示。）
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态（0，隐藏；1，显示。）
     *
     * @param status 状态（0，隐藏；1，显示。）
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取排序
     *
     * @return idx - 排序
     */
    public Integer getIdx() {
        return idx;
    }

    /**
     * 设置排序
     *
     * @param idx 排序
     */
    public void setIdx(Integer idx) {
        this.idx = idx;
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
}