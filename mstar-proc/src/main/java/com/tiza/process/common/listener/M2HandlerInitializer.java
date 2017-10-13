package com.tiza.process.common.listener;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.listener.Initializer;
import com.diyiliu.common.task.ITask;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.InOutRecord;

import javax.annotation.Resource;
import java.util.List;

/**
 * Description: M2HandlerInitializer
 * Author: DIYILIU
 * Update: 2017-09-21 14:37
 */
public class M2HandlerInitializer implements Initializer {

    @Resource
    private ITask refreshVehicleInfoTask;

    @Resource
    private ITask refreshCanInfoTask;

    @Resource
    private ITask refreshStorehouseTask;

    @Resource
    private ICache vehicleOutInCacheProvider;

    @Resource
    private VehicleDao vehicleDao;

    @Override
    public void init() {
        // 刷新车辆列表
        refreshVehicleInfoTask.execute();

        // 刷新功能集配置
        refreshCanInfoTask.execute();

        // 刷新车辆仓库
        refreshStorehouseTask.execute();

        // 车辆最近一条和仓库之间的位置信息
        List<InOutRecord> list = vehicleDao.selectInOutRecord();
        for (InOutRecord record : list) {
            vehicleOutInCacheProvider.put(String.valueOf(record.getVehicleId()), record);
        }
    }
}
