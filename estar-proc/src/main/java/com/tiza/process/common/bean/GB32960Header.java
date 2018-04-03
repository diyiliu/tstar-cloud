package com.tiza.process.common.bean;

import cn.com.tiza.tstar.common.entity.TStarData;
import com.diyiliu.common.model.Header;

/**
 * Description: GB32960Header
 * Author: Wangw
 * Update: 2017-09-06 16:55
 */
public class GB32960Header extends Header {

    private int cmd;
    private int resp;
    private int length;
    private String vin;
    private String terminalId;
    private int serial;
    private byte[] content = null;
    private int check;

    private byte[] startWith = new byte[]{0x23, 0x23};
    private TStarData tStarData;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getResp() {
        return resp;
    }

    public void setResp(int resp) {
        this.resp = resp;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public byte[] getStartWith() {
        return startWith;
    }

    public void settStarData(TStarData tStarData) {
        this.tStarData = tStarData;
    }

    public TStarData gettStarData() {
        return tStarData;
    }
}
