package com.tiza.process.common.model;

/**
 * Description: AlarmStrategy
 * Author: DIYILIU
 * Update: 2017-10-25 09:43
 */
public class AlarmStrategy {

    private long vehicleId;
    private long id;
    private String name;
    private int alarmLevel;
    private double maxVoltage;
    private double minVoltage;
    private int maxTemperature;
    private int minTemperature;

    /** 通知策略*/
    private AlarmNotice alarmNotice;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public double getMaxVoltage() {
        return maxVoltage;
    }

    public void setMaxVoltage(double maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public double getMinVoltage() {
        return minVoltage;
    }

    public void setMinVoltage(double minVoltage) {
        this.minVoltage = minVoltage;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public AlarmNotice getAlarmNotice() {
        return alarmNotice;
    }

    public void setAlarmNotice(AlarmNotice alarmNotice) {
        this.alarmNotice = alarmNotice;
    }
}
