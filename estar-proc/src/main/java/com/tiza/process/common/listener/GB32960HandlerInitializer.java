package com.tiza.process.common.listener;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.listener.Initializer;
import com.diyiliu.common.task.ITask;
import com.tiza.process.common.dao.AlarmDao;
import com.tiza.process.common.dao.FaultDao;
import com.tiza.process.common.model.AlarmStrategy;
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
    private ICache alarmStrategyCacheProvider;

    @Resource
    private FaultDao faultDao;

    @Resource
    private AlarmDao alarmDao;

    @Override
    public void init() {
        // 刷新车辆列表
        refreshVehicleInfoTask.execute();

        // 初始化故障代码库
        initFaultCode();

        // 初始化车辆当前故障
        initVehicleFault();

        // 初始化车辆报警策略
        initVehicleAlarmStrategy();
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

            putList(vehicleId, vehicleFaultCacheProvider, fault);
        }
    }

    /**
     * 获取车辆报警策略
     */
    public void initVehicleAlarmStrategy(){
        List<AlarmStrategy> alarmStrategies = alarmDao.selectAlarmStrategy();
        for (AlarmStrategy strategy: alarmStrategies){
            String vehicleId = String.valueOf(strategy.getVehicleId());

            putList(vehicleId, alarmStrategyCacheProvider, strategy);
        }
    }

    /**
     * 缓存中添加列表
     * @param key
     * @param cache
     * @param object
     */
    public void putList(String key, ICache cache, Object object){
        if (cache.containsKey(key)){

            List  list = (List) cache.get(key);
            list.add(object);
        }else {
            List list = new ArrayList();
            list.add(object);

            cache.put(key, list);
        }
    }
}
