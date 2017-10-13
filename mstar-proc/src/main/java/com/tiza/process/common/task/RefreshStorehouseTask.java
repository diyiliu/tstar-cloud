package com.tiza.process.common.task;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.task.ITask;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.VehicleStorehouse;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: RefreshStorehouseTask
 * Author: DIYILIU
 * Update: 2017-09-19 09:53
 */
public class RefreshStorehouseTask implements ITask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache vehicleStorehouseCacheProvider;

    @Resource
    private VehicleDao vehicleDao;

    @Override
    public void execute() {
        logger.info("刷新车辆仓库信息...");

        List<VehicleStorehouse> vehicleStorehouses = vehicleDao.selectVehicleStorehouse();
        refresh(vehicleStorehouses, vehicleStorehouseCacheProvider);
    }

    public void refresh(List<VehicleStorehouse> list, ICache provider){
        if (CollectionUtils.isEmpty(list)){
            logger.warn("无仓库信息！");
            return;
        }

        Set oldKeys = provider.getKeys();
        Set tempKeys = new HashSet(list.size());

        for (VehicleStorehouse vehicleStorehouse: list){
            String vehicleId = String.valueOf(vehicleStorehouse.getVehicleId());
            provider.put(vehicleId, vehicleStorehouse.getStorehouse());
            tempKeys.add(vehicleId);
        }

        // 被删除的
        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);
        for (Iterator iterator = subKeys.iterator(); iterator.hasNext();){
            int key = (int) iterator.next();
            provider.remove(key);
        }
    }
}
