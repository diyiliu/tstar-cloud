package com.tiza.process.common.bean;

import cn.com.tiza.tstar.common.entity.TStarData;
import com.diyiliu.common.model.Header;

/**
 * Description: M2Header
 * Author: DIYILIU
 * Update: 2017-09-14 14:23
 */
public class M2Header extends Header{

    private int cmd;
    private int length;
    private String terminalId;
    private int version;
    private int factory;
    private int terminalType;
    private int user;
    private int serial;
    private byte[] content = null;
    private int check;

    private byte[] end = new byte[]{0x0D, 0x0A};

    private TStarData tStarData;

    public M2Header() {

    }

    public M2Header(int cmd, int length, String terminalId,
                         int version, int factory, int terminalType,
                         int user, int serial, byte[] content,
                         int check, byte[] end) {

        this.cmd = cmd;
        this.length = length;
        this.terminalId = terminalId;
        this.version = version;
        this.factory = factory;
        this.terminalType = terminalType;
        this.user = user;
        this.serial = serial;
        this.content = content;
        this.check = check;
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

    public int getFactory() {
        return factory;
    }

    public void setFactory(int factory) {
        this.factory = factory;
    }

    public int getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(int terminalType) {
        this.terminalType = terminalType;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
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

    public byte[] getEnd() {
        return end;
    }

    public void setEnd(byte[] end) {
        this.end = end;
    }

    public TStarData gettStarData() {
        return tStarData;
    }

    public void settStarData(TStarData tStarData) {
        this.tStarData = tStarData;
    }
}
