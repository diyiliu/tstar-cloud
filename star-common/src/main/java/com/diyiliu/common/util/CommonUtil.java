package com.diyiliu.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: CommonUtil
 * Author: DIYILIU
 * Update: 2015-09-17 9:15
 */
public class CommonUtil {


    public static boolean isEmpty(String str) {

        if (str == null || str.trim().length() < 1) {
            return true;
        }

        return false;
    }

    public static byte[] ipToBytes(String host) {

        String[] array = host.split("\\.");

        byte[] bytes = new byte[array.length];

        for (int i = 0; i < array.length; i++) {

            bytes[i] = (byte) Integer.parseInt(array[i]);
        }

        return bytes;
    }

    public static String bytesToIp(byte[] bytes) {

        if (bytes.length == 4) {

            StringBuilder builder = new StringBuilder();

            for (byte b : bytes) {

                builder.append((int) b & 0xff).append(".");
            }

            return builder.substring(0, builder.length() - 1);
        }

        return null;
    }

    public static byte[] dateToBytes(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR) - 2000;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return new byte[]{(byte) year, (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second};
    }

    /**
     * 创建时间，修改对应时间
     * @param bytes
     * @return
     */
    public static Date bytesToDate(byte[] bytes) {

        if (bytes.length == 3 || bytes.length == 6) {
            Calendar calendar = Calendar.getInstance();
            toDate(calendar, bytes);

            return calendar.getTime();
        }

        return null;
    }

    /**
     *
     * @param date
     * @param bytes
     * @return
     */
    public static Date bytesToDate(Date date, byte[] bytes) {

        if (bytes.length == 3 || bytes.length == 6) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            toDate(calendar, bytes);

            return calendar.getTime();
        }

        return null;
    }


    public static void toDate(Calendar calendar, byte[] bytes){

        calendar.set(Calendar.YEAR, 2000 + bytes[0]);
        calendar.set(Calendar.MONTH, bytes[1] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, bytes[2]);
        if (bytes.length == 6) {

            calendar.set(Calendar.HOUR_OF_DAY, bytes[3]);
            calendar.set(Calendar.MINUTE, bytes[4]);
            calendar.set(Calendar.SECOND, bytes[5]);

        } else if (bytes.length == 3) {

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
    }



    public static long bytesToLong(byte[] bytes) {

        long l = 0;
        for (int i = 0; i < bytes.length; i++) {
            l += (long) ((bytes[i] & 0xff) * Math.pow(256, bytes.length - i - 1));
        }
        return l;
    }


    public static byte[] longToBytes(long number, int length) {

        long temp = number;

        byte[] bytes = new byte[length];

        for (int i = bytes.length - 1; i > -1; i--) {

            bytes[i] = new Long(temp & 0xff).byteValue();

            temp = temp >> 8;

        }

        return bytes;
    }

    public static String bytesToStr(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (byte a : bytes) {
            buf.append(String.format("%02X", getNoSin(a)));
        }

        return buf.toString();
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static String bytesToString(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (byte a : bytes) {
            buf.append(String.format("%02X", getNoSin(a))).append(" ");
        }

        return buf.substring(0, buf.length() - 1);
    }

    public static byte[] hexStringToBytes(String hex) {

        char[] charArray = hex.toCharArray();

        if (charArray.length % 2 != 0) {
            // 无法转义
            return null;
        }

        int length = charArray.length / 2;
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {

            String b = new String(new char[]{charArray[i * 2], charArray[i * 2 + 1]});
            bytes[i] = (byte) Integer.parseInt(b, 16);
        }

        return bytes;
    }


    public static String toHex(int i) {

        return String.format("%02X", i);
    }

    public static int getNoSin(byte b) {
        if (b >= 0) {
            return b;
        } else {
            return 256 + b;
        }
    }

    public static double keepDecimal(double d, int digit) {

        BigDecimal decimal = new BigDecimal(d);
        decimal = decimal.setScale(digit, RoundingMode.HALF_UP);

        return decimal.doubleValue();
    }

    /**
     * 保留小数
     * @param num
     * @param precision
     * @param digit
     * @return
     */
    public static double keepDecimal(Number num, double precision, int digit){
        BigDecimal decimal = new BigDecimal(String.valueOf(num));
        decimal =  decimal.multiply(new BigDecimal(precision)).setScale(digit, BigDecimal.ROUND_HALF_UP);

        return  decimal.doubleValue();
    }

    public static String parseBytes(byte[] array, int offset, int lenght) {

        ByteBuf buf = Unpooled.copiedBuffer(array, offset, lenght);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        return new String(bytes);
    }

    public boolean isInnerIp(String address) {
        String ip = address.substring(0, address.indexOf(":"));
        String reg = "(127[.]0[.]0[.]1)|(localhost)|(10[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3})|(172[.]((1[6-9])|(2\\d)|(3[01]))[.]\\d{1,3}[.]\\d{1,3})|(192[.]168[.]\\d{1,3}[.]\\d{1,3})";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(ip);

        return mat.find();
    }

    public static String parseVIN(byte[] array, int offset) {

        ByteBuf buf = Unpooled.copiedBuffer(array);
        buf.readBytes(new byte[offset]);

        int len = buf.readByte();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);

        return new String(bytes);
    }

    public static byte[] restoreBinary(String content) {

        String[] array = content.split(" ");

        byte[] bytes = new byte[array.length];

        for (int i = 0; i < array.length; i++) {

            bytes[i] = Integer.valueOf(array[i], 16).byteValue();
        }

        return bytes;
    }

    public static String parseSIM(byte[] bytes) {

        Long sim = 0l;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            sim += (long) (bytes[i] & 0xff) << ((len - i - 1) * 8);
        }

        return sim.toString();
    }

    public static byte[] packSIM(String sim) {

        byte[] array = new byte[5];
        Long simL = Long.parseLong(sim);

        for (int i = 0; i < array.length; i++) {
            Long l = (simL >> (i * 8)) & 0xff;
            array[array.length - 1 - i] = l.byteValue();
        }
        return array;
    }

    public static String parseIMEI(byte[] bytes) {

        String imei = bytesToStr(bytes);

        return imei.substring(0, 15);
    }

    public static byte[] packIMEI(String imei) {

        if (imei.length() == 15) {
            imei += 0;
        }

        return hexStringToBytes(imei);
    }


    public static byte getCheck(byte[] bytes) {
        byte b = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            b ^= bytes[i];
        }

        return b;
    }

    public static int renderHeight(byte[] bytes) {

        int plus = bytes[0] & 0x80;

        bytes[0] &= 0x7F;

        if (plus == 0) {
            return (int) bytesToLong(bytes);
        }

        return 0 - (int) bytesToLong(bytes);
    }

    public static int getBits(int val, int start, int len) {
        int left = 31 - start;
        int right = 31 - len + 1;
        return (val << left) >>> right;
    }

    public static byte[] byteToByte(byte[] workParamBytes, int start, int len, String endian) {
        byte[] tempBytes = new byte[len];
        int totalLen = start + len - 1;

        if (endian.equalsIgnoreCase("little")) {
            int tempI = 0;
            for (int j = totalLen; j >= start; j--) {// 小端模式
                tempBytes[tempI] = workParamBytes[j];
                tempI++;
            }
        } else {
            int tempI = 0;
            for (int j = start; j <= totalLen; j++) {// 大端模式
                tempBytes[tempI] = workParamBytes[j];
                tempI++;
            }
        }
        return tempBytes;
    }

    public static int getNosin2int(byte[] array){
        int res =0;
        if(array.length==1){
            res =getNonSign(array[0]);
        }
        if(array.length==2){
            res =getNonSign(array[0])*256+getNonSign(array[1]);
        }

        return res;
    }
    public static int byte2int(byte[] array) {

        if (array.length < 4) {
            return byte2short(array);
        }

        int r = 0;
        for (int i = 0; i < array.length; i++) {
            r <<= 8;
            r |= array[i] & 0xFF;
        }

        return r;
    }

    public static short byte2short(byte[] array) {

        short r = 0;
        for (int i = 0; i < array.length; i++) {
            r <<= 8;
            r |= array[i] & 0xFF;
        }

        return r;
    }

    /**
     * 解析算数表达式
     *
     * @param exp
     * @return
     */
    public static String parseExp(int val, String exp, String type) throws ScriptException {

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        String retVal;
        if (type.equalsIgnoreCase("hex")) {
            retVal = String.format("%02X", val);
        } else if (type.equalsIgnoreCase("decimal")) {
            retVal = engine.eval(val + exp).toString();
        } else {
            //表达式解析会出现类型问题
            retVal = engine.eval(val + exp).toString();
        }

        return retVal;
    }

    public static void main(String[] args) {


        /**
         byte[] array = new byte[]{0x03, 0x3D, 0x55, 0x7A, 0x39};
         String sim = null;
         sim = parseSIM(array);

         System.out.println(sim);
         array =  packSIM(sim);
         sim = parseSIM(array);

         System.out.println(sim);
         */

         System.out.println(keepDecimal(12.345,0.01, 1));

    }

    /**
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 数组转对象
     *
     * @param bytes
     * @return
     */
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /**
     * 取无符号的byte
     *
     * @param Sign
     * @return
     */
    public static int getNonSign(byte Sign) {
        if (Sign < 0) {
            return (Sign + 256);
        } else {
            return Sign;
        }
    }

    public static String canSystemtime(byte[] bytes) {
        String canSystemtime = null;
        try {
            canSystemtime = String.format("20" + "%02d-%02d-%02d %02d:%02d:%02d", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], (byte) 0);
        } catch (Exception e) {
        }
        return canSystemtime;
    }

    /**
     *
     * @param val
     * @param start
     * @param len
     * @param byteslen 夸字节的长度
     * @return
     */
    public static int wirelessbits(int val, int start, int len ,int byteslen){
        int res = 0;
        String BinaryString = addZeroForNum(Integer.toBinaryString(val), byteslen);
        String subBinaryString=BinaryString.substring(start, len);
        res = BinaryString2int(subBinaryString);
        return res;
    }

    /**
     * 任意2进制字符串 转int
     *
     * @param BinaryString
     * @return
     */
    public static int BinaryString2int(String BinaryString) {
        int x = 0;
        for (char c : BinaryString.toCharArray())
            x = x * 2 + (c == '1' ? 1 : 0);
        return x;
    }

    /**
     * 自动补齐
     *
     * @param str
     * @param strLength
     * @return
     */
    public static String addZeroForNum(String str, int strLength) {

        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength * 8) {
            sb = new StringBuffer();
            sb.append("0").append(str);// 左(前)补0
            // sb.append(str).append("0");//右(后)补0
            str = sb.toString();
            strLen = str.length();
        }
        return str;

    }
}
