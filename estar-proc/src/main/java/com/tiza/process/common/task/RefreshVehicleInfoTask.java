package com.tiza.process.common.task;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.task.ITask;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.VehicleInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: RefreshVehicleInfoTask
 * Author: Wangw
 * Update: 2017-09-08 16:12
 */

public class RefreshVehicleInfoTask implements ITask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private VehicleDao vehicleDao;

    @Resource
    private ICache vehicleCacheProvider;

    @Override
    public void execute() {
        logger.info("刷新车辆列表...");

        List<VehicleInfo> list = vehicleDao.selectVehicleInfo();
        refresh(list, vehicleCacheProvider);
    }

    private void refresh(List<VehicleInfo> vehicleList, ICache vehicleCache) {
        if (vehicleList == null || vehicleList.size() < 1){
            logger.warn("无车辆!");
            return;
        }

        Set oldKeys = vehicleCache.getKeys();
        Set tempKeys = new HashSet<>(vehicleList.size());

        for (VehicleInfo vehicle : vehicleList) {
            vehicleCache.put(vehicle.getVin(), vehicle);
            tempKeys.add(vehicle.getVin());
        }

        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);
        for (Iterator iterator = subKeys.iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            vehicleCache.remove(key);
        }
    }
}
