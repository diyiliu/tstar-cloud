package com.diyiliu.common.model;

import com.diyiliu.common.util.GisUtil;

/**
 * Description: Circle
 * Author: DIYILIU
 * Update: 2017-09-19 09:29
 */
public class Circle implements Area {

    /**
     * 中心点
     */
    private Point center;
    /**
     * 半径
     */
    private double radius;

    private double lat;
    private double lng;

    public Circle() {
    }

    public Circle(double radius, double lat, double lng) {
        this.radius = radius;
        this.lat = lat;
        this.lng = lng;
        this.center = new Point(lng, lat);
    }

    public Circle(Point point, double radius) {
        this.center = point;
        this.radius = radius;
    }

    public Point getCenter() {
        if (center == null) {
            center = new Point(lng, lat);
        }
        return this.center;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * 点与本区域的位置关系
     *
     * @param point
     * @return 0:在外;1:在内
     */
    @Override
    public int isPointInArea(Point point) {
        if (point == null) {
            point = new Point(lng, lat);
        }

        double distance = GisUtil.distanceP2P(point, getCenter()) * 1000;
        return ((distance > this.radius) ? 0 : 1);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
