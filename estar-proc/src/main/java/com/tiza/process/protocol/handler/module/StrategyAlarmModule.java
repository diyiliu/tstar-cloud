package com.tiza.process.protocol.handler.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.dao.AlarmDao;
import com.tiza.process.common.model.AlarmStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义策略报警
 *
 * Description: StrategyAlarmModule
 * Author: DIYILIU
 * Update: 2017-10-23 16:09
 */
public class StrategyAlarmModule extends BaseHandle {

    private ICache alarmStrategyCache;
    private AlarmDao alarmDao;

    private RPTuple rpTuple;

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        this.rpTuple = rpTuple;
        Map<String, String> context = rpTuple.getContext();
        if (context.containsKey(EStarConstant.FlowKey.VEHICLE_EXTREME)) {
            String vehicleId = rpTuple.getTerminalID();

            alarmStrategyCache = SpringUtil.getBean("alarmStrategyCacheProvider");
            alarmDao = SpringUtil.getBean("alarmDao");

            Map alarmMap = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.VEHICLE_EXTREME), HashMap.class);
            if (alarmStrategyCache.containsKey(vehicleId)){
                List<AlarmStrategy> list = (List<AlarmStrategy>) alarmStrategyCache.get(vehicleId);
                for (AlarmStrategy strategy: list){

                    dealAlarm(strategy, alarmMap);
                }
            }
        }

        return null;
    }

    @Override
    public void init() throws Exception {

    }

    public void dealAlarm(AlarmStrategy alarmStrategy, Map alarmMap){

        if (alarmMap.containsKey("BATTERYUNITMAXVOLTAGE")){
            int maxVoltage = (int) alarmMap.get("BATTERYUNITMAXVOLTAGE");

            if (maxVoltage > alarmStrategy.getMaxVoltage() * 1000){


            }
        }

        if (alarmMap.containsKey("BATTERYUNITMINVOLTAGE")){
            int minVoltage = (int) alarmMap.get("BATTERYUNITMINVOLTAGE");

            if (minVoltage < alarmStrategy.getMinVoltage() * 1000){


            }
        }

        if (alarmMap.containsKey("BATTERYMAXTEMP")){
            int maxTemp = (int) alarmMap.get("BATTERYMAXTEMP") - 40;

            if (maxTemp > alarmStrategy.getMaxTemperature()){


            }
        }

        if (alarmMap.containsKey("BATTERYMINTEMP")){
            int minTemp = (int) alarmMap.get("BATTERYMINTEMP") - 40;

            if (minTemp < alarmStrategy.getMinTemperature()){


            }
        }
    }
}
