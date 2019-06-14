package com.mod.loan.common.enums;

/**
 * 畅捷代付代扣查询返回状态
 *
 * @author NIELIN
 * @version $Id: ChangjiePayOrRepayOrQueryReturnCodeEnum.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
public enum ChangjiePayOrRepayOrQueryReturnCodeEnum {
    /******************** AppRetcode-S（受理状态） ********************/
    SUCCESS_00019999("00019999", "交易成功"),
    DOING_01019999("01019999", "交易处理中"),
    FAIL_11019999("11019999", "交易失败"),
    FAIL_11010003("11010003", "交易处理失败 "),
    FAIL_11010005("11010005", "卡bin机构不匹配"),
    FAIL_11010006("11010006", "卡号不可为空"),
    FAIL_11010007("11010007", "银行前置机异常"),
    FAIL_11010009("11010009", "账户余额不足"),
    FAIL_11010010("11010010", "账号或商户有误"),
    FAIL_11010011("11010011", "银行信息有误"),
    FAIL_11010014("11010014", "账户名称有误"),
    FAIL_11010018("11010018", "限额超限"),
    FAIL_11010040("11010040", "参数不正确"),
    FAIL_11010041("11010041", "查询失败"),
    FAIL_11020015("11020015", "参数不能为空"),
    FAIL_11020018("11020018", "限额超限"),
    FAIL_11020019("11020019", "不支持银行"),
    FAIL_11020020("11020020", "账号类型不支持"),
    FAIL_11020021("11020021", "订单重复"),
    FAIL_11020022("11020022", "账户状态异常"),
    FAIL_11020023("11020023", "商户状态异常"),
    FAIL_11020024("11020024", "身份验证有误"),
    FAIL_11020025("11020025", "原交易不存在"),
    FAIL_11020026("11020026", "不支持此交易"),
    FAIL_11020027("11020027", "机构状态异常"),
    FAIL_11020028("11020028", "输入密码有误"),
    FAIL_11020029("11020029", "密码输入次数超限"),
    FAIL_11020030("11020030", "报文格式错误"),
    FAIL_11020031("11020031", "验签失败"),
    FAIL_11020034("11020034", "交易超时"),
    FAIL_11020035("11020035", "解密失败"),
    FAIL_11020036("11020036", "查无此交易"),
    FAIL_11020037("11020037", "银行返回错误"),
    FAIL_11020038("11020038", "限额限次超限"),
    /******************** AppRetcode-E（受理状态） ********************/
    SUCCESS_QT000000("QT000000", "交易成功"),
    SUCCESS_QT000001("QT000001", "交易受理成功"),
    SUCCESS_QT100000("QT100000", "交易处理中"),
    FAIL_QT300008("QT300008", "此卡已绑定请勿重复绑卡"),
    /******************** PlatformRetCode-S ********************/
    SUCCESS_0000("0000", "成功（仅代表请求已成功被系统受理）"),
    DOING_2000("2000", "处理中"),
    FAIL_1000("1000", "失败"),
    FAIL_2004("2004", "失败 "),
    FAIL_2009("2009", "失败 "),
    /******************** PlatformRetCode-E ********************/
    /******************** OriginalRetCode-S ********************/
    SUCCESS_000000("000000", "成功"),
    FAIL_111111("111111", "失败"),
    DOING_000001("000001", "未知，待查询"),
    DOING_000002("000002", "已发送，待查询"),
    DOING_000004("000004", "已受理，待查询"),
    FAIL_000005("000005", "错误，支付失败"),
    FAIL_000006("000006", "未受理，支付失败"),
    DOING_000010("000010", "超时，待查询"),
    DOING_000050("000050", "重复，待查询"),
    DOING_900001("900001", "无对应批次信息，返回该类型1小时内勿重复发送相同请求。如1小时后依旧为此状态说明交易未收到，可认为交易失败。 "),
    DOING_900002("900002", "批次下无明细信息，返回该类型1小时内勿重复发送相同请求。如1小时后依旧为此状态说明交易未收到，可认为交易失败。"),
    DOING_900003("900003", "无对应明细，返回该类型1小时内勿重复发送相同请求。如1小时后依旧为此状态说明交易未收到，可认为交易失败。"),
    /******************** OriginalRetCode-E ********************/

    ;

    private String code;
    private String desc;

    ChangjiePayOrRepayOrQueryReturnCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (ChangjiePayOrRepayOrQueryReturnCodeEnum status : ChangjiePayOrRepayOrQueryReturnCodeEnum.values()) {
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