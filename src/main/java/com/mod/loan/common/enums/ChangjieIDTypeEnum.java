package com.mod.loan.common.enums;

/**
 * 畅捷证件类型
 *
 * @author NIELIN
 * @version $Id: ChangjieIDTypeEnum.java, v 0.1 2019/6/3 16:08 NIELIN Exp $
 */
public enum ChangjieIDTypeEnum {
    ID_CARD("01", "身份证"),
    PASSPORT("02", "护照"),
    CERTIFICATE_OFFICERS("03", "军官证"),
    HK_PASSPORT("04", "港澳居民往来内地通行证"),
    TAIWAN_PASSPORT("05", "台湾居民来往大陆通行证"),
    OTHERS("06", "其他证"),;

    private String code;
    private String desc;

    ChangjieIDTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (ChangjieIDTypeEnum status : ChangjieIDTypeEnum.values()) {
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
