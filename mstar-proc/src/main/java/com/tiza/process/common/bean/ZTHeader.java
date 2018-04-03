package com.tiza.process.common.bean;

import cn.com.tiza.tstar.common.entity.TStarData;

/**
 * Description: ZTHeader
 * Author: DIYILIU
 * Update: 2018-04-02 08:58
 */
public class ZTHeader extends M2Header{

    private int cmd;
    private int length;
    private String terminalId;
    private int version;
    private int serial;
    private byte[] content = null;
    private int check;

    private int start;
    private int end;

    private TStarData tStarData;

    public ZTHeader() {

    }

    public ZTHeader(int cmd, int length, String terminalId, int version, int serial,
                    byte[] content, int check, int start, int end) {

        this.cmd = cmd;
        this.length = length;
        this.terminalId = terminalId;
        this.version = version;
        this.serial = serial;
        this.content = content;
        this.check = check;
        this.start = start;
        this.end = end;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEndTag() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public TStarData gettStarData() {
        return tStarData;
    }

    public void settStarData(TStarData tStarData) {
        this.tStarData = tStarData;
    }
}
