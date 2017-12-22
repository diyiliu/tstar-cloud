package com.tiza.process.common.model;

import java.util.Date;

/**
 * Description: VehicleFault
 * Author: DIYILIU
 * Update: 2017-10-12 14:34
 */
public class VehicleFault {

    private Long vehicleId;
    private String faultUnit;
    private String faultValue;
    private Date startTime;
    private Date endTime;

    /** 是否解除故障 (false:报警中;true:解除报警。)*/
    private boolean isOver = false;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getFaultUnit() {
        return faultUnit;
    }

    public void setFaultUnit(String faultUnit) {
        this.faultUnit = faultUnit;
    }

    public String getFaultValue() {
        return faultValue;
    }

    public void setFaultValue(String faultValue) {
        this.faultValue = faultValue;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }
}
