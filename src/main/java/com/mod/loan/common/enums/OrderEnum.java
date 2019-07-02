package com.mod.loan.common.enums;

public enum OrderEnum {
        DAI_FUKUAN(11, "待付款"),
        WAIT_AUDIT(12, "待复核"),
        WAIT_LOAN(21, "待放款"),
        LOANING(22, "放款中"),
        LOAN_FAILED(23, "放款失败"),
        WAIT_CONFIRM_LOAN(24, "待确认放款"),
        REPAYING(31, "还款中"),
        REPAY_CONFIRMING(32, "还款确认中"),
        OVERDUE(33, "逾期"),
        BAD_DEBTS(34, "坏账"),
        DEFER(35, "展期"),
        OVERDUE_DEFER(36, "逾期后展期"),
        DEFER_OVERDUE(37, "展期后逾期"),
        DEFER_BAD_DEBTS(38, "展期后坏账"),
        NORMAL_REPAY(41, "正常还款"),
        OVERDUE_REPAY(42, "逾期还款"),
        DEFER_REPAY(43, "展期还款"),
        AUTO_AUDIT_REFUSE(51, "自动审核失败"),
        AUDIT_REFUSE(52, "复审失败"),
        CANCEL(53, "交易取消"),
        ;

        private Integer code;
        private String desc;

    OrderEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDesc(Integer code) {
            for (OrderEnum status : OrderEnum.values()) {
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
