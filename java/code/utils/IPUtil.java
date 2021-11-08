package com.technology.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP工具类
 */
public class IPUtil {

    /**
     * 通过request文件头拿到真实IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip)) {
            return request.getRemoteAddr();
        }
        ip = ip.split(", ")[0].trim();
        if ("127.0.0.1".equals(ip) || !validate(ip)) {
            return request.getRemoteAddr();
        }
        return ip;
    }

    static String regex = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
    static Pattern p = Pattern.compile(regex);

    public static boolean validate(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        Matcher m = p.matcher(ip);
        return m.find();
    }


    public static long getWannetLen(String wannet){
        int maskLen = Integer.parseInt(wannet.split("/")[1]);
        return 1<<(32 - maskLen);
    }

    public static long getWannetValue(String wannet){
        String value = wannet.split("/")[0];
        String[] split = value.split("\\.");
        long sum = 0;
        for(int i=0; i<split.length; i++){
            long slice = Long.parseLong(split[3-i])<<(8*i);
            sum = sum | slice;
        }
        return sum;
    }

    public static boolean overlaps(String wannet1, String wannet2){
        long wannet1Len = getWannetLen(wannet1);
        long wannet1Value = getWannetValue(wannet1);
        long wannet2Len = getWannetLen(wannet2);
        long wannet2Value = getWannetValue(wannet2);
        if((wannet2Value >= wannet1Value) && (wannet2Value < (wannet1Value + wannet1Len))){
            return true;
        } else if((wannet1Value >= wannet2Value) && (wannet1Value < (wannet2Value + wannet2Len))){
            return true;
        } else{
            return false;
        }
    }

}
