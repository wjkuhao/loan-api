package com.mod.loan.common.enums;

public enum MerchantEnum {
        helibao(1, "合利宝"),
        fuyou(2, "富友"),
        huiju(3, "汇聚"),
        yeepay(4, "易宝"),
    CHANGJIE(5, "畅捷"),
        kuaiqian(6, "快钱"),
        jinyuntong(8,"金运通");
    private Integer code;
    private String desc;

    MerchantEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for (MerchantEnum status : MerchantEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
