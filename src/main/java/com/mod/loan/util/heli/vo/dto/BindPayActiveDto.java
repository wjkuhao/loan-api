package com.mod.loan.util.heli.vo.dto;

/**
 * @Auther: wzg
 * @Date: 2019-05-12 17:30
 * @Description:绑卡支付请求操作
 */
public class BindPayActiveDto extends BindPaySmsCodeDto {


    private String validateCode;

    private String callBackUrl;

    public BindPayActiveDto(String repayNo, String amount, String hlbId, String foreignId, String userId, String phone, String merchant, String orderId, String validateCode, String callBackUrl) {
        super(repayNo, amount, hlbId, foreignId, userId, phone, merchant, orderId);
        this.validateCode = validateCode;
        this.callBackUrl = callBackUrl;
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
