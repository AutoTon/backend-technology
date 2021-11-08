package com.technology.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 日期工具类
 */
public class DateUtil {

    public static Date getNextNthDay(Integer nth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + nth);
        return c.getTime();
    }

    /**
     * 字符串日期根据指定格式转换成Date
     *
     * @param dateString   字符串日期
     * @param formatString 转换格式
     * @return 转换后的Date
     */
    public static Date str2Date(String dateString, String formatString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("时间转化格式错误!" + "[dateString=" + dateString + "]" + "[FORMAT_STRING=" + formatString + "]");
        }
    }

    public static String addDay(String dateStr, int amount) {
        if (amount != 0) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = formatter.parse(dateStr);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DATE, amount);
                date = calendar.getTime();

                return formatter.format(date);
            } catch (ParseException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            return dateStr;
        }
    }

    public static Date addDay(Date date, int amount) {
        if (amount != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, amount);
            return calendar.getTime();
        } else {
            return date;
        }
    }

    public static String addDay(String dateStr, int amount, String format) {
        if (amount != 0) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                Date date = formatter.parse(dateStr);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DATE, amount);
                date = calendar.getTime();

                return formatter.format(date);
            } catch (ParseException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            return dateStr;
        }
    }

    public static Date addMonth(Date date, int amount) {
        if (amount == 0) {
            return date;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, amount);
        return calendar.getTime();
    }

    public static String addHour(String dateTimeStr, int amount) {
        if (amount != 0) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = formatter.parse(dateTimeStr);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.HOUR_OF_DAY, amount);
                date = calendar.getTime();

                return formatter.format(date);
            } catch (ParseException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            return dateTimeStr;
        }
    }


    public static long addMiute(Date date, int amount) {
        if (amount == 0) {
            return date.getTime();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, amount);
        return calendar.getTime().getTime();
    }

    public static Date addMiuteToDate(Date date, int amount) {
        if (amount == 0) {
            return date;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, amount);
        return calendar.getTime();
    }


    public static int getDiffMonth(String date1, String date2) {
        return getDiffMonth(date1, date2, "yyyy-MM");
    }

    public static int getDiffMonth(String date1, String date2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(sdf.parse(date1));
            c2.setTime(sdf.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't parse date", e);
        }
        int result = 0;
        int flag = 0;
        if (c1.equals(c2)) {
            return 0;
        }
        if (c1.after(c2)) {
            Calendar temp = c1;
            c1 = c2;
            c2 = temp;
        }
        if (c2.get(Calendar.DAY_OF_MONTH) < c1.get(Calendar.DAY_OF_MONTH)) {
            flag = 1;
        }

        if (c2.get(Calendar.YEAR) > c1.get(Calendar.YEAR)) {
            result = ((c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12 + c2.get(Calendar.MONTH) - flag) - c1.get(Calendar.MONTH);
        } else {
            result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH) - flag;
        }
        return result;
    }

    public static int getDiffMonthWithNegative(String date1, String date2) {
        return getDiffMonthWithNegative(date1, date2, "yyyy-MM");
    }

    public static int getDiffMonthWithNegative(String date1, String date2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(sdf.parse(date1));
            c2.setTime(sdf.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't parse date", e);
        }
        int result = 0;
        int flag = 0;
        if (c1.equals(c2)) {
            return 0;
        }
        if (c2.get(Calendar.DAY_OF_MONTH) < c1.get(Calendar.DAY_OF_MONTH)) {
            flag = 1;
        }

        if (c2.get(Calendar.YEAR) > c1.get(Calendar.YEAR)) {
            result = ((c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12 + c2.get(Calendar.MONTH) - flag) - c1.get(Calendar.MONTH);
        } else {
            result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH) - flag;
        }
        return result;
    }

    public static long diffDay(Timestamp t1, Timestamp t2) {
        String t1Str = new SimpleDateFormat("yyyy-MM-dd").format(t1);
        String t2Str = new SimpleDateFormat("yyyy-MM-dd").format(t2);
        return diffDay(t1Str, t2Str);
    }

    public static String timestampToStr(Timestamp t) {
        if(t == null){
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t);
    }


    public static long diffDay(String date1, String date2) {
        return diffDay(date1, date2, "yyyy-MM-dd");
    }

    public static long diffDay(String date1, String date2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            long diff;
            if (d2.after(d1)) {
                diff = d2.getTime() - d1.getTime();
            } else {
                diff = d1.getTime() - d2.getTime();
            }
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't parse date", e);
        }
    }

    public static long diffDay(Date date1, Date date2) {
        long diff;
        if (date2.after(date1)) {
            diff = date2.getTime() - date1.getTime();
        } else {
            diff = date1.getTime() - date2.getTime();
        }
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static long diffDayWithNegative(String date1, String date2) {
        return diffDayWithNegative(date1, date2, "yyyy-MM-dd");
    }

    public static long diffDayWithNegative(String date1, String date2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            long diff = d1.getTime() - d2.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't parse date", e);
        }
    }

    public static long diffHours(String datetime1, String datetime2) {
        return diffHours(datetime1, datetime2, "yyyy-MM-dd HH:mm:ss");
    }

    public static long diffHours(String datetime1, String datetime2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date dt1 = sdf.parse(datetime1);
            Date dt2 = sdf.parse(datetime2);
            long diff;
            if (dt2.after(dt1)) {
                diff = dt2.getTime() - dt1.getTime();
            } else {
                diff = dt1.getTime() - dt2.getTime();
            }
            return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't parse date", e);
        }
    }

    public static long diffMins(Date datetime1, Date datetime2) {
        long diff;
        if (datetime2.after(datetime1)) {
            diff = datetime2.getTime() - datetime1.getTime();
        } else {
            diff = datetime1.getTime() - datetime2.getTime();
        }
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static long diffMins(String datetime1, String datetime2) {
        return diffMins(datetime1, datetime2, "yyyy-MM-dd HH:mm:ss");
    }

    public static long diffMins(String datetime1, String datetime2, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date dt1 = sdf.parse(datetime1);
            Date dt2 = sdf.parse(datetime2);
            long diff;
            if (dt2.after(dt1)) {
                diff = dt2.getTime() - dt1.getTime();
            } else {
                diff = dt1.getTime() - dt2.getTime();
            }
            return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            throw new RuntimeException("Can't parse date", e);
        }
    }

    public static int dateOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static Date addSecond(Date date, int amount) {
        if (amount == 0) {
            return date;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, amount);
        return calendar.getTime();
    }

    /**
     * 获取指定日期之前的第几个工作日（不含当天）
     * @param startDate 指定日期
     * @param workDay 工作日数量
     * @return 指定日期之前的第几个工作日
     */
    @SuppressWarnings("PMD")
    public static Date getWorkDay(Date startDate, int workDay) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startDate);
        for (int i = 0; i < workDay; i++) {
            c1.set(Calendar.DATE, c1.get(Calendar.DATE) - 1);
            if (Calendar.SATURDAY == c1.get(Calendar.SATURDAY) || Calendar.SUNDAY == c1.get(Calendar.SUNDAY)) {
                workDay = workDay + 1;
                c1.set(Calendar.DATE, c1.get(Calendar.DATE) - 1);
            }
        }
        return c1.getTime();
    }

    public static int getHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

}
