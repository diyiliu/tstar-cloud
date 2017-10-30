package com.tiza.process.common.model;

import com.diyiliu.common.model.Point;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.GpsCorrectUtil;

import java.util.Date;

/**
 * Description: Position
 * Author: DIYILIU
 * Update: 2017-09-26 14:22
 */
public class Position {

    private int status;

    private Long lng;
    private Long lat;

    private Double lngD;
    private Double latD;

    private Double enLngD;
    private Double enLatD;

    private Date dateTime;

    // 里程
    private Double mileage;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Double getLngD() {
        double d = this.lng / 1000000.0;
        lngD = CommonUtil.keepDecimal(d, 6);
        return lngD;
    }

    public Double getLatD() {
        double d = this.lat / 1000000.0;
        latD = CommonUtil.keepDecimal(d, 6);
        return latD;
    }

    public Double getEnLngD() {
        Point point = GpsCorrectUtil.transform(latD, lngD);
        if (point == null) {
            return null;
        }
        return CommonUtil.keepDecimal(point.getLng(), 6);
    }

    public Double getEnLatD() {
        Point point = GpsCorrectUtil.transform(latD, lngD);
        if (point == null) {
            return null;
        }
        return CommonUtil.keepDecimal(point.getLat(), 6);
    }


    public void setLng(Long lng) {
        this.lng = lng;
    }

    public void setLat(Long lat) {
        this.lat = lat;
    }


    public void setLngD(Double lngD) {
        this.lngD = lngD;
    }


    public void setLatD(Double latD) {
        this.latD = latD;
    }

    public void setEnLngD(Double enLngD) {
        this.enLngD = enLngD;
    }

    public void setEnLatD(Double enLatD) {
        this.enLatD = enLatD;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }
}
