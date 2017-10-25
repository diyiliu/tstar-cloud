package com.tiza.process.common.model;

/**
 * Description: AlarmNotice
 * Author: DIYILIU
 * Update: 2017-10-25 10:27
 */
public class AlarmNotice {
    private long alarmId;
    private long userId;
    private int sms;
    private int email;
    private int site;

    public long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(long alarmId) {
        this.alarmId = alarmId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getSms() {
        return sms;
    }

    public void setSms(int sms) {
        this.sms = sms;
    }

    public int getEmail() {
        return email;
    }

    public void setEmail(int email) {
        this.email = email;
    }

    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }
}
