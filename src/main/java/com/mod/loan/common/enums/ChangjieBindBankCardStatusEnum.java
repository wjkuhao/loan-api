package com.mod.loan.common.enums;

/**
 * 畅捷绑卡状态
 *
 * @author NIELIN
 * @version $Id: ChangjieBindBankCardStatusEnum.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
public enum ChangjieBindBankCardStatusEnum {
    S("S", "成功"),
    F("F", "失败"),
    P("P", "处理中"),;

    private String code;
    private String desc;

    ChangjieBindBankCardStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (ChangjieBindBankCardStatusEnum status : ChangjieBindBankCardStatusEnum.values()) {
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
