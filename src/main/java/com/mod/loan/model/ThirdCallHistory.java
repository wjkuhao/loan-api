//package com.mod.loan.model;
//
//import javax.persistence.Id;
//import javax.persistence.Table;
//import java.io.Serializable;
//import java.util.Date;
//
///**
// * tb_third_call_history
// * @author
// */
//@Table(name = "tb_third_call_history")
//public class ThirdCallHistory implements Serializable {
//    /**
//     * 主键
//     */
//    @Id
//    private Integer id;
//
//    /**
//     * 商户别名
//     */
//    private String merchant;
//
//    /**
//     * 区分不同第三方 1 有盾 2 运营商
//     */
//    private String code;
//    //日期
//    private String day;
//    //创建时间
//    private Date createTime;
//    //用户id
//    private Long uid;
//
//    private static final long serialVersionUID = 1L;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getMerchant() {
//        return merchant;
//    }
//
//    public void setMerchant(String merchant) {
//        this.merchant = merchant;
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//    public String getDay() {
//        return day;
//    }
//
//    public void setDay(String day) {
//        this.day = day;
//    }
//
//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
//
//    public Long getUid() {
//        return uid;
//    }
//
//    public void setUid(Long uid) {
//        this.uid = uid;
//    }
//
//    public static long getSerialVersionUID() {
//        return serialVersionUID;
//    }
//}