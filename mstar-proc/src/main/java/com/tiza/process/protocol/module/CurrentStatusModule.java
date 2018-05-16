package com.tiza.process.protocol.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.Parameter;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.VehicleInfo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * Description: CurrentStatusModule
 * Author: DIYILIU
 * Update: 2017-09-14 15:14
 */
public class CurrentStatusModule  extends BaseHandle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        Map<String, String> context = rpTuple.getContext();
        String terminalId = rpTuple.getTerminalID();

        ICache vehicleCache = SpringUtil.getBean("vehicleCacheProvider");
        if (context.containsKey(MStarConstant.FlowKey.POSITION) && vehicleCache.containsKey(terminalId)) {
            VehicleInfo vehicleInfo = (VehicleInfo) vehicleCache.get(terminalId);
            String vehicleId = String.valueOf(vehicleInfo.getId());

            Position position = JacksonUtil.toObject(context.get(MStarConstant.FlowKey.POSITION), Position.class);

            Map canValue = null;
            if (context.containsKey(MStarConstant.FlowKey.PARAMETER)){
                Parameter parameter = JacksonUtil.toObject(context.get(MStarConstant.FlowKey.PARAMETER), Parameter.class);
                canValue = parameter.getCanValues();
            }

            VehicleDao vehicleDao = SpringUtil.getBean("vehicleDao");
            Object[] values = new Object[]{position.getEnLngD(), position.getLatD(),
                    position.getSpeed(), position.getDirection(), position.getHeight(), position.getDateTime()};

            StringBuilder sqlBuilder = new StringBuilder("UPDATE bs_vehiclegpsinfo t SET ");
            sqlBuilder.append("t.encryptlng      = ?, ");
            sqlBuilder.append("t.encryptlat      = ?, ");
            sqlBuilder.append("t.speed           = ?, ");
            sqlBuilder.append("t.direction       = ?, ");
            sqlBuilder.append("t.altitude        = ?, ");
            sqlBuilder.append("t.gpstime         = ?, ");
            sqlBuilder.append("t.systemtime      = SYSDATE, ");

            // 更新状态位
            if (MapUtils.isNotEmpty(position.getStatusMap())){
                toUpdate(sqlBuilder, position.getStatusMap());
            }
            // 更新工况信息
            if (MapUtils.isNotEmpty(canValue)){
                toUpdate(sqlBuilder, canValue);
            }

            String sql = sqlBuilder.substring(0, sqlBuilder.length() - 2) + " WHERE t.vehicleid =" + vehicleId;
            if (vehicleDao.update(sql, values)) {
                logger.info("车辆[{}]更新车辆当前表信息...", vehicleId);
            } else {
                logger.error("车辆[{}]更新车辆当前表信息失败!", vehicleId);
            }
        }

        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }

    private void toUpdate(StringBuilder sqlBuilder, Map map){
        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            Object value = map.get(key);

            if (value != null){
                if (value instanceof String){
                    value = "'" + value + "'";
                }
                sqlBuilder.append("t.").append(key).append("=").append(value).append(", ");
            }
        }
    }
}
