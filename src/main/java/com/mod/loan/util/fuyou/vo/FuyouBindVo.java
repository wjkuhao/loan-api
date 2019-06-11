package com.mod.loan.util.fuyou.vo;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "REQUEST")
public class FuyouBindVo {
    @XNode("VERSION")
    private String version;
    @XNode("TRADEDATE")
    private String tradeDate;
    @XNode("MCHNTSSN")
    private String mchntSsn;
    @XNode("MCHNTCD")
    private String mchntCd;
    @XNode("USERID")
    private String userId;
    @XNode("IDCARD")
    private String idCard;
    @XNode("IDTYPE")
    private String idType;
    @XNode("ACCOUNT")
    private String account;
    @XNode("CARDNO")
    private String cardNo;
    @XNode("MOBILENO")
    private String mobileNo;
    @XNode("MSGCODE")
    private String msgCode;
    @XNode("PROTOCOLNO")
    private String protocolNo;
    @XNode("SIGN")
    private String sign;
    @XNode("CVN")
    private String cvn;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getMchntSsn() {
        return mchntSsn;
    }

    public void setMchntSsn(String mchntSsn) {
        this.mchntSsn = mchntSsn;
    }

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public String getProtocolNo() {
        return protocolNo;
    }

    public void setProtocolNo(String protocolNo) {
        this.protocolNo = protocolNo;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getCvn() {
        return cvn;
    }

    public void setCvn(String cvn) {
        this.cvn = cvn;
    }
}
