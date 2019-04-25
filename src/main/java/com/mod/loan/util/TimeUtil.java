package com.mod.loan.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@SuppressWarnings({"unused"})
public final class TimeUtil {

    public static String dateFormat(Date date) {
        if (null == date)
            return LocalDate.now().format(DATE_FORMATTER);
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().format(DATE_FORMATTER);
    }

    public static String timeFormat(Date date) {
        if (null == date)
            return LocalDateTime.now().format(TIME_FORMATTER);
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime().format(TIME_FORMATTER);
    }

    public static String timePlusMinute(String time, int minutes) {
        return LocalDateTime.parse(time, TIME_FORMATTER).plusMinutes(minutes).format(TIME_FORMATTER);
    }

    public static String nowTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(TIME_FORMATTER);
    }

    public static String nowDate() {
        return LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DATE_FORMATTER);
    }

    public static int nowDateMinusDate(Date otherDate) {
        if (null == otherDate)
            return 0;
        LocalDate nowDate = LocalDate.now();
        Instant instant = otherDate.toInstant();
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")).toLocalDate();
        return Period.between(localDate, nowDate).getDays();
    }

    public static String timePlusDays(Date date, int days) {
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().plusDays(days).format(TIME_FORMATTER);
    }

    public static String datePlusDays(Date date, int days) {
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate().plusDays(days).format(DATE_FORMATTER);
    }

    public static String nowDatePlus(int year, int month, int day) {
        return LocalDate.now(ZoneId.of("Asia/Shanghai")).plusYears(year).plusMonths(month).plusDays(day).format(DATE_FORMATTER);
    }

    public static String nowDatePlusDay(int day) {
        return LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(day).format(DATE_FORMATTER);
    }

    public static String nowTimePlus(int year, int month, int day) {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).plusYears(year).plusMonths(month).plusDays(day).format(TIME_FORMATTER);
    }

    public static String nowTimePlusDay(int day) {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).plusDays(day).format(TIME_FORMATTER);
    }

    public static LocalDate parseLocalDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static LocalDateTime parseLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, TIME_FORMATTER);
    }

    public static DateTimeFormatter dateFormatter() {
        return DATE_FORMATTER;
    }

    public static DateTimeFormatter timeFormatter() {
        return TIME_FORMATTER;
    }

    public static String getOneDayStartTime(String currentDate) {
        return currentDate + " 00:00:00";
    }

    public static String getOneDayEndTime(String currentDate) {
        return currentDate + " 23:59:59";
    }

    public static String yyyyMMdd() {
        return LocalDateTime.now().format(YYYYMMDD_FORMATTER);
    }

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Shanghai"));
    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
    private static DateTimeFormatter YYYYMMDDHHMMSS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Shanghai"));
    private static DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Asia/Shanghai"));

    //	private static String STD_DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
//	private static String STD_TIME_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
}
