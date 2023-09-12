package com.svgtopng.utility;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Utility {

    /**
     * 判断文件夹是否存在，不存在就创建
     * 
     * @param file
     */
    public static void createFolder(File file) {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }
    }

    public static String getYear() {
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    public static String getMonth() {
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        Integer month = cal.get(Calendar.MONTH) + 1;
        if (month < 10) {
            return "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
        } else {
            return String.valueOf(cal.get(Calendar.MONTH) + 1);
        }
    }

    public static String getDay() {
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        Integer day = cal.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            return "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        } else {
            return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        }
    }

    public static Date getDateFromTodayByYear(Integer yearNumber) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, yearNumber);
        return calendar.getTime();
    }

    public static Date getDateFromTodayByMonth(Integer monthNumber) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, monthNumber);
        return calendar.getTime();
    }

    public static String generateEntityId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (int) (Math.random() * 1000);
        String tail = "";
        for (int i = 0; i < 4 - Integer.valueOf(num).toString().length(); i++) {
            tail = "0" + tail;
        }
        return sdf.format(new Date()).replaceAll("\\D", "") + tail + num;
    }

    public static LocalDateTime DateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public static Date getDateByCycleAndUnit(Date start, Integer cycle, String unit) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(start);
        if ("月".equals(unit)) {
            calendar.add(Calendar.MONTH, cycle);
        } else if ("周".equals(unit)) {
            calendar.add(Calendar.DATE, cycle * 7);
        } else {
            calendar.add(Calendar.DATE, cycle);
        }
        return calendar.getTime();
    }
}
