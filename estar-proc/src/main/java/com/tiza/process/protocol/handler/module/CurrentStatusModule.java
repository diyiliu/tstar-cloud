package com.tiza.process.protocol.handler.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.model.VehicleInfo;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 车辆实时状态
 *
 * Description: CurrentStatusModule
 * Author: DIYILIU
 * Update: 2017-10-26 18:52
 */
public class CurrentStatusModule extends BaseHandle {

    private VehicleInfo vehicleInfo;

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        Map<String, String> context = rpTuple.getContext();
        if (context.containsKey(EStarConstant.FlowKey.REAL_MODE)) {

            String terminalId = rpTuple.getTerminalID();
            ICache vehicleCache = SpringUtil.getBean("vehicleCacheProvider");
            if (!vehicleCache.containsKey(terminalId)) {

                return rpTuple;
            }

            vehicleInfo = (VehicleInfo) vehicleCache.get(terminalId);
            vehicleInfo.setDateTime(rpTuple.getTime());

            Map modelMap = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.REAL_MODE), HashMap.class);
            dealRealModel(modelMap);
        }


        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }

    public void dealRealModel(Map modelMap) {
        String prefix = "model:gb32960:";
        Jedis jedis = getJedis();

        for (Iterator iterator = modelMap.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            int value = (int) modelMap.get(key);

            String redisKey = prefix + key;
            if (!jedis.exists(redisKey)) {

                createMessage(key, value);
                jedis.set(redisKey, String.valueOf(value));
            } else {
                int last = Integer.valueOf(jedis.get(redisKey));

                // 状态发生变化
                if (value != last) {

                    createMessage(key, value);
                    jedis.set(redisKey, String.valueOf(value));
                }
            }
        }
    }

    public void createMessage(String key, int value) {
        Map message = new HashMap();
        message.put("vehicleId", vehicleInfo.getId());
        message.put("license", vehicleInfo.getLicense());
        message.put("vin", vehicleInfo.getVin());
        message.put("time", new Date(vehicleInfo.getDateTime()));

        String category = "";
        String content = "";
        switch (key) {
            case EStarConstant.RealMode.IN_OUT:
                category = "vehicle";
                if (1 == value){
                    content = "车辆登入";
                }else if (0 == value){
                    content = "车辆登出";
                }
                break;
            case EStarConstant.RealMode.ON_OFF:
                category = "vehicle";
                if (1 == value){
                    content = "车辆启动";
                }else if (2 == value){
                    content = "车辆熄火";
                }
                break;
            case EStarConstant.RealMode.ALARM_LEVEL:
                category = "fault";
                content = "车辆故障，当前故障等级为" + value + "级";
                break;
            case EStarConstant.RealMode.TOP_OFF:
                category = "charge";
                if (1 == value){
                    content = "停车充电";
                }else if (4 == value){
                    content = "充电完成";
                }
                break;
            default:
                break;
        }
        message.put("category", category);
        message.put("message", content);

        // 消息发布到redis
        Jedis jedis = getJedis();
        String channel = processorConf.get(EStarConstant.Redis.VEHICLE_EVENT);
        jedis.publish(channel, JacksonUtil.toJson(message));
    }
}
