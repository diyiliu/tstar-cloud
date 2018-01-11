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

        List<VehicleInfo> vehicleInfos = vehicleDao.selectVehicleInfo();
        refresh(vehicleInfos, vehicleCacheProvider);
    }

    private void refresh(List<VehicleInfo> vehicleInfos, ICache vehicleCache) {
        if (vehicleInfos == null){
            logger.warn("无车辆信息！");
            return;
        }

        Set oldKeys = vehicleCache.getKeys();
        Set tempKeys = new HashSet<>(vehicleInfos.size());

        for (VehicleInfo vehicle : vehicleInfos) {
            vehicleCache.put(vehicle.getDeviceId(), vehicle);
            tempKeys.add(vehicle.getDeviceId());
        }

        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);
        for (Iterator iterator = subKeys.iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            vehicleCache.remove(key);
        }
    }
}
