package com.tiza.process.common.model;

import com.diyiliu.common.model.Area;

/**
 * Description: Storehouse
 * Author: DIYILIU
 * Update: 2017-09-19 10:21
 */
public class Storehouse {

    private int id;
    private Area area;

    /** 检测车辆位置频率 (单位:分钟)*/
    private int rate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
