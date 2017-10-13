package com.tiza.process.common.model;

/**
 * Description: VehicleStorehouse
 * Author: DIYILIU
 * Update: 2017-10-13 10:13
 */
public class VehicleStorehouse {

    private long vehicleId;
    private Storehouse storehouse;

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Storehouse getStorehouse() {
        return storehouse;
    }

    public void setStorehouse(Storehouse storehouse) {
        this.storehouse = storehouse;
    }
}
