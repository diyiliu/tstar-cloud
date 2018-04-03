package com.tiza.process.common.model;

import com.diyiliu.common.model.Point;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.GpsCorrectUtil;

import java.util.Date;
import java.util.Map;

/**
 * Description: Position
 * Author: DIYILIU
 * Update: 2017-08-03 17:49
 */
public class Position {

    private Long lng;
    private Long lat;
    private Double lngD;
    private Double latD;
    private Double enLngD;
    private Double enLatD;

    private Integer speed;
    private Integer direction;
    private Integer height;
    private Long status;
    private Date dateTime;

    private byte[] statusBytes;
    private Map statusMap;

    public Position() {

    }


    public Position(long lng, long lat, int speed, int direction, int height, long status, Date dateTime) {
        this.lng = lng;
        this.lat = lat;
        this.speed = speed;
        this.direction = direction;
        this.height = height;
        this.status = status;
        this.dateTime = dateTime;
    }

    public Position(long lng, long lat, int speed, int direction, byte[] statusBytes, Date dateTime) {
        this.lng = lng;
        this.lat = lat;
        this.speed = speed;
        this.direction = direction;
        this.statusBytes = statusBytes;
        this.dateTime = dateTime;
    }


    public Position(long lng, long lat, int speed, int direction, int height, byte[] statusBytes, Date dateTime) {
        this.lng = lng;
        this.lat = lat;
        this.speed = speed;
        this.direction = direction;
        this.height = height;
        this.statusBytes = statusBytes;
        this.dateTime = dateTime;
    }

    public Long getLng() {
        return lng;
    }

    public void setLng(Long lng) {
        this.lng = lng;
    }

    public Long getLat() {
        return lat;
    }

    public void setLat(Long lat) {
        this.lat = lat;
    }

    public Double getLngD() {
        double d = this.lng / 1000000.0;
        lngD = CommonUtil.keepDecimal(d, 6);
        return lngD;
    }

    public void setLngD(Double lngD) {
        this.lngD = lngD;
    }

    public Double getLatD() {
        double d = this.lat / 1000000.0;
        latD = CommonUtil.keepDecimal(d, 6);
        return latD;
    }

    public Double getEnLngD() {
        Point point = GpsCorrectUtil.transform(latD, lngD);
        if (point == null){
            return  null;
        }
        return CommonUtil.keepDecimal(GpsCorrectUtil.transform(latD, lngD).getLng(), 6);
    }

    public Double getEnLatD() {
        Point point = GpsCorrectUtil.transform(latD, lngD);
        if (point == null){
            return  null;
        }
        return CommonUtil.keepDecimal(GpsCorrectUtil.transform(latD, lngD).getLat(), 6);
    }

    public void setLatD(Double latD) {
        this.latD = latD;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public byte[] getStatusBytes() {
        return statusBytes;
    }

    public void setStatusBytes(byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }

    public Map getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map statusMap) {
        this.statusMap = statusMap;
    }
}
