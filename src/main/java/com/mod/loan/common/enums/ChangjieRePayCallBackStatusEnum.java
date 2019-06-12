package com.mod.loan.common.enums;

/**
 * 畅捷代扣还款异步回调状态
 *
 * @author NIELIN
 * @version $Id: ChangjieRePayCallBackStatusEnum.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
public enum ChangjieRePayCallBackStatusEnum {
    /*********************** 代扣还款-异步回调状态-S ************************/
    PAY_FINISHED("PAY_FINISHED", "买家已付款"),
    TRADE_SUCCESS("TRADE_SUCCESS", "交易成功"),
    TRADE_FINISHED("TRADE_FINISHED", "交易结束"),
    TRADE_CLOSED("TRADE_CLOSED", "交易关闭"),
    PAY_FAIL("PAY_FAIL", "支付失败"),
    /*********************** 代扣还款-异步回调状态-E ************************/
    ;

    private String code;
    private String desc;

    ChangjieRePayCallBackStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (ChangjieRePayCallBackStatusEnum status : ChangjieRePayCallBackStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
