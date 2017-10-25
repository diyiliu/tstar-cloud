package com.tiza.process.common.model;

/**
 * Description: VehicleInfo
 * Author: DIYILIU
 * Update: 2017-08-07 14:33
 */
public class VehicleInfo {

    private long id;
    private long terminalId;

    private String deviceId;
    private String terminalNo;
    private String sim;
    private String vin;
    private String iccid;
    private String license;

    private String protocolType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(long terminalId) {
        this.terminalId = terminalId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
