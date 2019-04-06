package com.mod.loan.common.enums;

public enum OrderRepayStatusEnum {
        INIT(0, "初始"),
        ACCEPT_SUCCESS(1, "受理成功"),
        ACCEPT_FAILED(2, "受理失败"),
        REPAY_SUCCESS(3, "还款成功"),
        REPAY_FAILED(4, "还款失败"),
        CALLBACK_EXCEPTION(5, "回调信息异常"),
        ;

        private Integer code;
        private String desc;

    OrderRepayStatusEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(Integer code) {
            for (OrderRepayStatusEnum status : OrderRepayStatusEnum.values()) {
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
