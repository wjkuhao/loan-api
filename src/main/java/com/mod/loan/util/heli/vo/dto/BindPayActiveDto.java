package com.mod.loan.util.heli.vo.dto;

/**
 * @Auther: wzg
 * @Date: 2019-05-12 17:30
 * @Description:绑卡支付请求操作
 */
public class BindPayActiveDto extends BindPaySmsCodeDto {

    private String repayNo;

    private String validateCode;

    private String callBackUrl;

    public BindPayActiveDto(String amount, String hlbId, String foreignId, String userId, String phone, String merchant, String orderId,
                            String repayNo, String callBackUrl, String validateCode) {
        super(amount, hlbId, foreignId, userId, phone, merchant, orderId);
        this.repayNo = repayNo;
        this.callBackUrl = callBackUrl;
        this.validateCode = validateCode;
    }

    public String getRepayNo() {
        return repayNo;
    }

    public void setRepayNo(String repayNo) {
        this.repayNo = repayNo;
    }

    public String getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(String validateCode) {
        this.validateCode = validateCode;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }
}
