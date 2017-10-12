package com.tiza.process.common.listener;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.listener.Initializer;
import com.diyiliu.common.task.ITask;
import com.tiza.process.common.dao.FaultDao;
import com.tiza.process.common.model.FaultCode;
import com.tiza.process.common.model.VehicleFault;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: GB32960HandlerInitializer
 * Author: DIYILIU
 * Update: 2017-09-21 15:48
 */
public class GB32960HandlerInitializer implements Initializer {

    @Resource
    private ITask refreshVehicleInfoTask;

    @Resource
    private ICache faultCodeCacheProvider;

    @Resource
    private ICache vehicleFaultCacheProvider;

    @Resource
    private FaultDao faultDao;

    @Override
    public void init() {

        // 刷新车辆列表
        refreshVehicleInfoTask.execute();

        initFaultCode();

        initVehicleFault();
    }

    /**
     * 获取故障代码库
     */
    public void initFaultCode(){

        List<FaultCode> faultCodes = faultDao.selectFaultCode();
        for (FaultCode code: faultCodes){

            String key = code.getFaultUnit() + "_" + code.getFaultValue();
            faultCodeCacheProvider.put(key, code);
        }

    }

    /**
     * 获取车辆当前故障
     */
    public void initVehicleFault(){
        List<VehicleFault> vehicleFaults = faultDao.selectVehicleFault();
        for (VehicleFault fault: vehicleFaults){
            String vehicleId = String.valueOf(fault.getVehicleId());

            if (vehicleFaultCacheProvider.containsKey(vehicleId)){
                List list = (List) vehicleFaultCacheProvider.get(vehicleId);
                list.add(fault);
            }else {
                List list = new ArrayList();
                list.add(fault);

                vehicleFaultCacheProvider.put(vehicleId, list);
            }
        }
    }
}
