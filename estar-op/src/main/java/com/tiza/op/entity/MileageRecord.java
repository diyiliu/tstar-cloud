package com.tiza.op.entity;

import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class MileageRecord implements DBWritable {

	private long vehicleId;
	private Date dateTime;

	private double mileage;
	private double totalMileage;

	private Date createTime;
	
	public MileageRecord(){		
	}
	
	@Override
	public void readFields(ResultSet rs) throws SQLException {

	}

	@Override
	public void write(PreparedStatement ps) throws SQLException {
		ps.setLong(1, vehicleId);
		ps.setDate(2, new java.sql.Date(dateTime.getTime()));
		ps.setDouble(3, totalMileage);
		ps.setDouble(4, mileage);
		ps.setTimestamp(5, new Timestamp(createTime.getTime()));
	}

	public long getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(long vehicleId) {
		this.vehicleId = vehicleId;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public double getMileage() {
		return mileage;
	}

	public void setMileage(double mileage) {
		this.mileage = keepDecimal(mileage, 1);
	}

	public double getTotalMileage() {
		return totalMileage;
	}

	public void setTotalMileage(double totalMileage) {
		this.totalMileage = keepDecimal(totalMileage, 1);
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {

		return "[vehicle: " + vehicleId + "; day: " + dateTime + "; mileage: " + mileage + "]";
	}

	public double keepDecimal(double d, int digit) {

		BigDecimal decimal = new BigDecimal(d);
		decimal = decimal.setScale(digit, RoundingMode.HALF_UP);

		return decimal.doubleValue();
	}
}
