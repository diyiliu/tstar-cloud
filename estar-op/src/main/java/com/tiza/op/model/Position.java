package com.tiza.op.model;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Description: Position
 * Author: DIYILIU
 * Update: 2017-09-26 14:22
 */
public class Position implements Writable, Comparable<Position> {

    private Double lngD;
    private Double latD;

    private Long dateTime;

    // 里程
    private Double mileage;

    public Position() {
    }

    public Position(Double lngD, Double latD, Long dateTime, Double mileage) {
        this.lngD = lngD;
        this.latD = latD;
        this.dateTime = dateTime;
        this.mileage = mileage;
    }

    public Double getLngD() {
        return lngD;
    }

    public void setLngD(Double lngD) {
        this.lngD = lngD;
    }

    public Double getLatD() {
        return latD;
    }

    public void setLatD(Double latD) {
        this.latD = latD;
    }

    public Long getDateTime() {
        return dateTime;
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {

        dataOutput.writeDouble(lngD);
        dataOutput.writeDouble(latD);
        dataOutput.writeLong(dateTime);
        dataOutput.writeDouble(mileage);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.lngD = dataInput.readDouble();
        this.latD = dataInput.readDouble();
        this.dateTime = dataInput.readLong();
        this.mileage = dataInput.readDouble();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj != null) {
            if (obj instanceof Position) {
                Position p = (Position) obj;
                if (p.getLngD() == lngD &&
                        p.getLatD() == latD &&
                        p.getMileage() == mileage) {

                    return true;
                }
            }
        }

        return false;
    }


    @Override
    public int compareTo(Position o) {

        return dateTime.compareTo(o.getDateTime());
    }
}
