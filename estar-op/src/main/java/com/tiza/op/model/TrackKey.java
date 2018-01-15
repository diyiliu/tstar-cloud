package com.tiza.op.model;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Description: TrackKey
 * Author: DIYILIU
 * Update: 2017-09-27 15:30
 */
public class TrackKey implements WritableComparable<TrackKey> {

    private String vehicleId;
    private long datetime;

    public TrackKey() {
    }

    public TrackKey(String vehicleId, long datetime) {
        this.vehicleId = vehicleId;
        this.datetime = datetime;
    }

    @Override
    public int hashCode() {
        return this.vehicleId.hashCode();
    }

    @Override
    public int compareTo(TrackKey o) {
        int result = vehicleId.compareTo(o.getVehicleId());

        /*if (result == 0){
            return Long.compare(datetime, o.getDatetime());
        }*/

        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {

        Text.writeString(out, vehicleId);
        //out.writeLong(datetime);
    }

    @Override
    public void readFields(DataInput in) throws IOException {

        this.vehicleId = Text.readString(in);
        //this.datetime = in.readLong();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
