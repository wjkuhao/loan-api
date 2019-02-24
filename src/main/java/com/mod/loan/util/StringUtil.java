package com.mod.loan.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

public class StringUtil {

    /**
     * 根据前缀生成订单编号
     *
     * @return
     */
    public static String getOrderNumber(String prefix) {
        // TODO 目前生成规则为：日期精确到毫秒
        return prefix + TimeUtils.parseTime(new Date(), TimeUtils.dateformat6) + RandomUtils.generateRandomNum(6);
    }

    /**
     * 银行卡保留后四位
     * @param bankCard 银行卡号
     * @return
     */
    public static String bankTailNo(String bankCard) {

        String pattern = "(?<=\\d{0})\\d(?=\\d{4})";

        if (bankCard == null || bankCard.isEmpty()) {
            return null;
        } else {
            return bankCard.replaceAll(pattern, "");
        }
    }

    /**
     * 整数金额处理
     * @param money
     * @return
     */
    public static String moneyFormat(BigDecimal money){
        return new DecimalFormat(".00").format(money);
    }

}
