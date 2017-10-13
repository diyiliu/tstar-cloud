package com.tiza.process.protocol.handler.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.model.Point;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.InOutRecord;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.Storehouse;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Description: StrategyAlarmModule
 * Author: DIYILIU
 * Update: 2017-09-19 09:00
 */
public class StrategyAlarmModule extends BaseHandle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ICache recordMap;
    private ICache vehicleStorehouseMap;

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        recordMap = SpringUtil.getBean("vehicleOutInCacheProvider");
        vehicleStorehouseMap = SpringUtil.getBean("vehicleStorehouseCacheProvider");

        Map<String, String> context = rpTuple.getContext();
        String vehicleId = rpTuple.getTerminalID();

        if (context.containsKey(MStarConstant.FlowKey.POSITION)) {
            Position position = JacksonUtil.toObject(context.get(MStarConstant.FlowKey.POSITION), Position.class);

            // 有状态位
            if (MapUtils.isEmpty(position.getStatusMap())) {

                //logger.warn("无状态位数据!");
                return rpTuple;
            }
            Map<String, String> statusMap = position.getStatusMap();

            int locationStatus = Double.valueOf(statusMap.get("LOCATIONSTATUS")).intValue();
            // 有效定位 并且 车辆有仓库
            if (locationStatus == 1 && vehicleStorehouseMap.containsKey(vehicleId)) {
                // 当前位置
                Point point = new Point(position.getLng(), position.getLat());

                Storehouse storehouse = (Storehouse) vehicleStorehouseMap.get(vehicleId);
                int sid = storehouse.getId();

                if (storehouse.getArea() == null) {

                    logger.warn("车辆仓库数据异常!");
                    return rpTuple;
                }

                if (storehouse.getArea().isPointInArea(point) == 1) {

                    logger.info("车辆[{}]在仓库[{}]内...", vehicleId, storehouse.getId());
                } else {
                    if (recordMap.containsKey(vehicleId)) {
                        InOutRecord oldRecord = (InOutRecord) recordMap.get(vehicleId);
                        long interval = (position.getDateTime().getTime() - oldRecord.getGpsTime().getTime()) / (1000 * 60);

                        //logger.info("车辆[{}]在仓库[{}]外, 数据间隔[{}]...", vehicleId, storehouse.getId(), interval);

                        if (interval > storehouse.getRate()) {

                            toCreate(position, vehicleId, oldRecord.getStorehouseId());
                        }
                    } else {

                        toCreate(position, vehicleId, sid);
                    }
                }
            }else {
                //logger.warn("无效定位数据!");
            }
        }

        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }

    private void toCreate(Position position, String vehicleId, int storehouseId) {
        String sql = "INSERT INTO bs_warehouseoutin" +
                "(vehicleid, unitid, gpstime, encryptlng, encryptlat, systemtime) " +
                "VALUES(?,?,?,?,?,?)";

        Object[] paramValues = new Object[]{vehicleId, storehouseId,
                position.getDateTime(), position.getEnLngD(), position.getEnLatD(), new Date()};

        VehicleDao vehicleDao = SpringUtil.getBean("vehicleDao");
        if (!vehicleDao.update(sql, paramValues)) {

            logger.error("新增车辆策略报警失败!");
        }

        InOutRecord record = new InOutRecord();
        record.setGpsTime(position.getDateTime());

        recordMap.put(vehicleId, record);
    }
}
