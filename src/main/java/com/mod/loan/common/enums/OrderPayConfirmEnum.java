package com.mod.loan.common.enums;

public enum OrderPayConfirmEnum {
    NOT_CONFIRM(0, "未确认"),
    CONFIRM(1, "已确认");

    private Integer code;
    private String desc;

    OrderPayConfirmEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for (OrderPayConfirmEnum status : OrderPayConfirmEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
