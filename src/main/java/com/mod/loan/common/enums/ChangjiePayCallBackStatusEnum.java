package com.mod.loan.common.enums;

/**
 * 畅捷代付放款异步回调状态
 *
 * @author NIELIN
 * @version $Id: ChangjiePayCallBackStatusEnum.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
public enum ChangjiePayCallBackStatusEnum {
    /*********************** 代付放款-异步回调状态-S ************************/
    WITHDRAWAL_SUCCESS("WITHDRAWAL_SUCCESS", "成功"),
    WITHDRAWAL_FAIL("WITHDRAWAL_FAIL", "失败"),
    /*********************** 代付放款-异步回调状态-E ************************/

    ;

    private String code;
    private String desc;

    ChangjiePayCallBackStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (ChangjiePayCallBackStatusEnum status : ChangjiePayCallBackStatusEnum.values()) {
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
