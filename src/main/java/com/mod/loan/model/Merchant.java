package com.mod.loan.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_merchant")
public class Merchant {

    /**
     * 商户别名与app别名一致
     */
    @Id
    @Column(name = "merchant_alias")
    private String merchantAlias;

    /**
     * 商户名称
     */
    @Column(name = "merchant_name")
    private String merchantName;

    /**
     * 商户android别名
     */
    @Column(name = "merchant_app")
    private String merchantApp;

    /**
     * 商户ios别名
     */
    @Column(name = "merchant_app_ios")
    private String merchantAppIos;
    /**
     * 商户支付宝
     */
    @Column(name = "merchant_zfb")
    private String merchantZfb;

    /**
     * 商户状态
     */
    @Column(name = "merchant_status")
    private Integer merchantStatus;

    @Column(name = "merchant_channel")
    private String merchantChannel;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "hlb_id")
    private String hlb_id;

    @Column(name = "hlb_rsa_private_key")
    private String hlb_rsa_private_key;

    @Column(name = "hlb_merchant_sign")
    private String hlbMerchantSign;

    @Column(name = "hlb_entrusted_sign_key")
    private String hlbEntrustedSignKey;

    @Column(name = "hlb_entrusted_private_key")
    private String hlbEntrustedPrivateKey;

    @Column(name = "merchant_market")
    private String merchantMarket;

    @Column(name = "fuyou_merid")
    private String fuyou_merid;

    @Column(name = "fuyou_secureid")
    private String fuyou_secureid;

    @Column(name = "fuyou_h5key")
    private String fuyou_h5key;

    @Column(name = "huiju_id")
    private String huiju_id;

    @Column(name = "huiju_md5_key")
    private String huiju_md5_key;

    @Column(name = "bind_type")
    private Integer bindType;

    @Column(name = "yeepay_group_no")
    private String yeepay_group_no;

    @Column(name = "yeepay_repay_appkey")
    private String yeepay_repay_appkey;

    @Column(name = "yeepay_repay_private_key")
    private String yeepay_repay_private_key;

    @Column(name = "yeepay_loan_appkey")
    private String yeepay_loan_appkey;

    @Column(name = "yeepay_loan_private_key")
    private String yeepay_loan_private_key;

    @Column(name = "kq_merchant_id")
    private String kqMerchantId;

    @Column(name = "kq_terminal_id")
    private String kqTerminalId;

    @Column(name = "kq_cert_path")
    private String kqCertPath;

    @Column(name = "kq_cert_pwd")
    private String kqCertPwd;

    @Column(name = "kq_merchant_code")
    private String kqMerchantCode;

    /**
     * 畅捷的商户id
     */
    @Column(name = "cj_partnerId")
    private String cjPartnerId;

    /**
     * 畅捷自己的公钥
     */
    @Column(name = "cj_public_key")
    private String cjPublicKey;

    /**
     * 商户的私钥
     */
    @Column(name = "cj_merchant_private_key")
    private String cjMerchantPrivateKey;

    /**
     * 汇潮的商户id
     */
    @Column(name = "huichao_merid")
    private String huichaoMerid;

    /**
     * 汇潮自己的公钥
     */
    @Column(name = "huichao_public_key")
    private String huichaoPublicKey;

    /**
     * 汇潮商户的微信、支付宝、代扣的私钥
     */
    @Column(name = "huichao_merchant_repay_private_key")
    private String huichaoMerchantRepayPrivateKey;

    /**
     * 汇潮商户的代付的私钥
     */
    @Column(name = "huichao_merchant_pay_private_key")
    private String huichaoMerchantPayPrivateKey;
    /**
     * 金运通商户号
     */
    @Column(name = "jinyuntong_merchant_id")
    private String jinyuntongMerchantId;
    /**
     * 金运通平台公钥
     */
    @Column(name = "jinyuntong_public_key")
    private String jinyuntongPublicKey;
    /**
     * 金运通商户私钥
     */
    @Column(name = "jinyuntong_merchant_private_key")
    private String jinyuntongMerchantPrivateKey ;

    public String getJinyuntongMerchantId() {
        return jinyuntongMerchantId;
    }

    public void setJinyuntongMerchantId(String jinyuntongMerchantId) {
        this.jinyuntongMerchantId = jinyuntongMerchantId;
    }

    public String getJinyuntongPublicKey() {
        return jinyuntongPublicKey;
    }

    public void setJinyuntongPublicKey(String jinyuntongPublicKey) {
        this.jinyuntongPublicKey = jinyuntongPublicKey;
    }

    public String getJinyuntongMerchantPrivateKey() {
        return jinyuntongMerchantPrivateKey;
    }

    public void setJinyuntongMerchantPrivateKey(String jinyuntongMerchantPrivateKey) {
        this.jinyuntongMerchantPrivateKey = jinyuntongMerchantPrivateKey;
    }

    public String getYeepay_loan_appkey() {
        return yeepay_loan_appkey;
    }

    public void setYeepay_loan_appkey(String yeepay_loan_appkey) {
        this.yeepay_loan_appkey = yeepay_loan_appkey;
    }

    public String getYeepay_loan_private_key() {
        return yeepay_loan_private_key;
    }

    public void setYeepay_loan_private_key(String yeepay_loan_private_key) {
        this.yeepay_loan_private_key = yeepay_loan_private_key;
    }

    public String getYeepay_group_no() {
        return yeepay_group_no;
    }

    public void setYeepay_group_no(String yeepay_group_no) {
        this.yeepay_group_no = yeepay_group_no;
    }

    public String getYeepay_repay_appkey() {
        return yeepay_repay_appkey;
    }

    public void setYeepay_repay_appkey(String yeepay_repay_appkey) {
        this.yeepay_repay_appkey = yeepay_repay_appkey;
    }

    public String getYeepay_repay_private_key() {
        return yeepay_repay_private_key;
    }

    public void setYeepay_repay_private_key(String yeepay_repay_private_key) {
        this.yeepay_repay_private_key = yeepay_repay_private_key;
    }
    /**
     * 获取商户别名与app别名一致
     *
     * @return merchant_alias - 商户别名与app别名一致
     */
    public String getMerchantAlias() {
        return merchantAlias;
    }

    /**
     * 设置商户别名与app别名一致
     *
     * @param merchantAlias 商户别名与app别名一致
     */
    public void setMerchantAlias(String merchantAlias) {
        this.merchantAlias = merchantAlias == null ? null : merchantAlias.trim();
    }

    /**
     * 获取商户名称
     *
     * @return merchant_name - 商户名称
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * 设置商户名称
     *
     * @param merchantName 商户名称
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName == null ? null : merchantName.trim();
    }

    /**
     * 获取商户支付宝
     *
     * @return merchant_zfb - 商户支付宝
     */
    public String getMerchantZfb() {
        return merchantZfb;
    }

    /**
     * 设置商户支付宝
     *
     * @param merchantZfb 商户支付宝
     */
    public void setMerchantZfb(String merchantZfb) {
        this.merchantZfb = merchantZfb == null ? null : merchantZfb.trim();
    }

    /**
     * 获取商户状态
     *
     * @return merchant_status - 商户状态
     */
    public Integer getMerchantStatus() {
        return merchantStatus;
    }

    /**
     * 设置商户状态
     *
     * @param merchantStatus 商户状态
     */
    public void setMerchantStatus(Integer merchantStatus) {
        this.merchantStatus = merchantStatus;
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

    public String getMerchantApp() {
        return merchantApp;
    }

    public void setMerchantApp(String merchantApp) {
        this.merchantApp = merchantApp;
    }

    public String getHlb_id() {
        return hlb_id;
    }

    public void setHlb_id(String hlb_id) {
        this.hlb_id = hlb_id;
    }

    public String getHlb_rsa_private_key() {
        return hlb_rsa_private_key;
    }

    public void setHlb_rsa_private_key(String hlb_rsa_private_key) {
        this.hlb_rsa_private_key = hlb_rsa_private_key;
    }

    public String getMerchantAppIos() {
        return merchantAppIos;
    }

    public void setMerchantAppIos(String merchantAppIos) {
        this.merchantAppIos = merchantAppIos;
    }

    public String getMerchantMarket() {
        return merchantMarket;
    }

    public void setMerchantMarket(String merchantMarket) {
        this.merchantMarket = merchantMarket;
    }

    public String getFuyou_merid() {
        return fuyou_merid;
    }

    public void setFuyou_merid(String fuyou_merid) {
        this.fuyou_merid = fuyou_merid;
    }

    public String getFuyou_secureid() {
        return fuyou_secureid;
    }

    public void setFuyou_secureid(String fuyou_secureid) {
        this.fuyou_secureid = fuyou_secureid;
    }

    public String getMerchantChannel() {
        return merchantChannel;
    }

    public void setMerchantChannel(String merchantChannel) {
        this.merchantChannel = merchantChannel;
    }

    public String getFuyou_h5key() {
        return fuyou_h5key;
    }

    public void setFuyou_h5key(String fuyou_h5key) {
        this.fuyou_h5key = fuyou_h5key;
    }

    public String getHuiju_id() {
        return huiju_id;
    }

    public void setHuiju_id(String huiju_id) {
        this.huiju_id = huiju_id;
    }

    public String getHuiju_md5_key() {
        return huiju_md5_key;
    }

    public void setHuiju_md5_key(String huiju_md5_key) {
        this.huiju_md5_key = huiju_md5_key;
    }

    public Integer getBindType() {
        return bindType;
    }

    public void setBindType(Integer bindType) {
        this.bindType = bindType;
    }

    public String getHlbMerchantSign() {
        return hlbMerchantSign;
    }

    public void setHlbMerchantSign(String hlbMerchantSign) {
        this.hlbMerchantSign = hlbMerchantSign;
    }

    public String getHlbEntrustedSignKey() {
        return hlbEntrustedSignKey;
    }

    public void setHlbEntrustedSignKey(String hlbEntrustedSignKey) {
        this.hlbEntrustedSignKey = hlbEntrustedSignKey;
    }

    public String getHlbEntrustedPrivateKey() {
        return hlbEntrustedPrivateKey;
    }

    public void setHlbEntrustedPrivateKey(String hlbEntrustedPrivateKey) {
        this.hlbEntrustedPrivateKey = hlbEntrustedPrivateKey;
    }

    public String getKqMerchantId() {
        return kqMerchantId;
    }

    public void setKqMerchantId(String kqMerchantId) {
        this.kqMerchantId = kqMerchantId;
    }

    public String getKqTerminalId() {
        return kqTerminalId;
    }

    public void setKqTerminalId(String kqTerminalId) {
        this.kqTerminalId = kqTerminalId;
    }

    public String getKqCertPath() {
        return kqCertPath;
    }

    public void setKqCertPath(String kqCertPath) {
        this.kqCertPath = kqCertPath;
    }

    public String getKqCertPwd() {
        return kqCertPwd;
    }

    public void setKqCertPwd(String kqCertPwd) {
        this.kqCertPwd = kqCertPwd;
    }

    public String getKqMerchantCode() {
        return kqMerchantCode;
    }

    public void setKqMerchantCode(String kqMerchantCode) {
        this.kqMerchantCode = kqMerchantCode;
    }

    public String getCjPartnerId() {
        return cjPartnerId;
    }

    public void setCjPartnerId(String cjPartnerId) {
        this.cjPartnerId = cjPartnerId;
    }

    public String getCjPublicKey() {
        return cjPublicKey;
    }

    public void setCjPublicKey(String cjPublicKey) {
        this.cjPublicKey = cjPublicKey;
    }

    public String getCjMerchantPrivateKey() {
        return cjMerchantPrivateKey;
    }

    public void setCjMerchantPrivateKey(String cjMerchantPrivateKey) {
        this.cjMerchantPrivateKey = cjMerchantPrivateKey;
    }

    public String getHuichaoMerid() {
        return huichaoMerid;
    }

    public void setHuichaoMerid(String huichaoMerid) {
        this.huichaoMerid = huichaoMerid;
    }

    public String getHuichaoPublicKey() {
        return huichaoPublicKey;
    }

    public void setHuichaoPublicKey(String huichaoPublicKey) {
        this.huichaoPublicKey = huichaoPublicKey;
    }

    public String getHuichaoMerchantRepayPrivateKey() {
        return huichaoMerchantRepayPrivateKey;
    }

    public void setHuichaoMerchantRepayPrivateKey(String huichaoMerchantRepayPrivateKey) {
        this.huichaoMerchantRepayPrivateKey = huichaoMerchantRepayPrivateKey;
    }

    public String getHuichaoMerchantPayPrivateKey() {
        return huichaoMerchantPayPrivateKey;
    }

    public void setHuichaoMerchantPayPrivateKey(String huichaoMerchantPayPrivateKey) {
        this.huichaoMerchantPayPrivateKey = huichaoMerchantPayPrivateKey;
    }
}