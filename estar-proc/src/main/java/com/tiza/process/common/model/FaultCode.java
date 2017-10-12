package com.tiza.process.common.model;

/**
 * Description: FaultCode
 * Author: DIYILIU
 * Update: 2017-10-12 14:50
 */
public class FaultCode {

    private long id;
    private long factoryId;
    private String faultUnit;
    private String faultValue;
    private String name;
    private String desc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(long factoryId) {
        this.factoryId = factoryId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
