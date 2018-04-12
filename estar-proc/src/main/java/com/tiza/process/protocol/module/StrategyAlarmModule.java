package com.tiza.process.protocol.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.util.DateUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.dao.AlarmDao;
import com.tiza.process.common.model.AlarmNotice;
import com.tiza.process.common.model.AlarmStrategy;
import com.tiza.process.common.model.VehicleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 自定义策略报警
 * <p>
 * Description: StrategyAlarmModule
 * Author: DIYILIU
 * Update: 2017-10-23 16:09
 */
public class StrategyAlarmModule extends BaseHandle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ICache alarmStrategyCache;
    private AlarmDao alarmDao;

    private RPTuple rpTuple;

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        Map<String, String> context = rpTuple.getContext();

        String terminalId = rpTuple.getTerminalID();
        ICache vehicleCache = SpringUtil.getBean("vehicleCacheProvider");
        if (vehicleCache.containsKey(terminalId)) {
            this.rpTuple = rpTuple;
            VehicleInfo vehicleInfo = (VehicleInfo) vehicleCache.get(terminalId);
            String vehicleId = String.valueOf(vehicleInfo.getId());

            alarmStrategyCache = SpringUtil.getBean("alarmStrategyCacheProvider");
            alarmDao = SpringUtil.getBean("alarmDao");

            Map map = new HashMap();
            if (context.containsKey(EStarConstant.FlowKey.VEHICLE_EXTREME)) {
                Map alarmMap = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.VEHICLE_EXTREME), HashMap.class);
                map.putAll(alarmMap);
            }

            if (context.containsKey(EStarConstant.FlowKey.ALARM_LEVEL)) {
                Map alarmLevel = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.ALARM_LEVEL), HashMap.class);
                map.putAll(alarmLevel);
            }

            if (alarmStrategyCache.containsKey(vehicleId)) {
                List<AlarmStrategy> list = (List<AlarmStrategy>) alarmStrategyCache.get(vehicleId);
                for (AlarmStrategy strategy : list) {
                    dealAlarm(strategy, map, vehicleInfo);
                }
            }
        }

        return rpTuple;
    }

    @Override
    public void init() {

    }

    public void dealAlarm(AlarmStrategy alarmStrategy, Map alarmMap, VehicleInfo vehicleInfo) throws IOException {

        StringBuffer redisKey = new StringBuffer("alarm:gb32960:");
        redisKey.append(vehicleInfo.getId());

        StringBuffer content = new StringBuffer();
        if (alarmMap.containsKey("AlarmLevel")) {
            int level = (int) alarmMap.get("AlarmLevel");

            if (level > alarmStrategy.getAlarmLevel()) {

                content.append("最高报警等级[").append(level).append("]报警。");
                redisKey.append(":level");
            }
        }

        if (alarmMap.containsKey("BatteryUnitMaxVoltage")) {
            double maxVoltage = (double) alarmMap.get("BatteryUnitMaxVoltage");

            if (maxVoltage > alarmStrategy.getMaxVoltage()) {

                content.append("最高电压值[").append(maxVoltage).append("]报警。");
                redisKey.append(":maxVoltage");
            }
        }


        if (alarmMap.containsKey("BatteryUnitMinVoltage")) {
            double minVoltage = (double) alarmMap.get("BatteryUnitMinVoltage");

            if (minVoltage < alarmStrategy.getMinVoltage()) {

                content.append("最低电压值[").append(minVoltage).append("]报警。");
                redisKey.append(":minVoltage");
            }
        }

        if (alarmMap.containsKey("BatteryMaxTemp")) {
            int maxTemp = (int) alarmMap.get("BatteryMaxTemp");

            if (maxTemp > alarmStrategy.getMaxTemperature()) {

                content.append("最高温度值[").append(maxTemp).append("]报警。");
                redisKey.append(":maxTemperature");
            }
        }

        if (alarmMap.containsKey("BatteryMinTemp")) {
            int minTemp = (int) alarmMap.get("BatteryMinTemp");

            if (minTemp < alarmStrategy.getMinTemperature()) {

                content.append("最低温度值[").append(minTemp).append("]报警。");
                redisKey.append(":minTemperature");
            }
        }

        if (content.length() < 1) {
            logger.warn("无报警内容。车辆[{}], 策略[{}], 数据[{}]",
                    vehicleInfo.getId(), JacksonUtil.toJson(alarmStrategy), JacksonUtil.toJson(alarmMap));
            return;
        }

        String title = "车辆[" + vehicleInfo.getLicense() + "]报警";
        Date now = new Date(rpTuple.getTime());

        String key = redisKey.toString();
        Jedis jedis = getJedis();
        try {
            if (jedis.exists(key)) {
                String alarmJson = jedis.get(key);

                HashMap old = JacksonUtil.toObject(alarmJson, HashMap.class);
                Date oldDate = DateUtil.stringToDate((String) old.get("time"));

                Calendar oldCal = Calendar.getInstance();
                oldCal.setTime(oldDate);

                Calendar nowCal = Calendar.getInstance();
                nowCal.setTime(now);

                /*if (nowCal.get(Calendar.DAY_OF_YEAR) - oldCal.get(Calendar.DAY_OF_YEAR) < 1) {

                    return;
                }*/
            }

            Map redisMap = new HashMap();
            redisMap.put("title", title);
            redisMap.put("time", now);
            jedis.set(key, JacksonUtil.toJson(redisMap));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        AlarmNotice notice = alarmStrategy.getAlarmNotice();
        if (notice.getSite() == 1) {
            String sql = "INSERT INTO bs_message(id,userid,title,content,createtime,status) VALUES(sq_bs_message.nextval,?,?,?,?,0)";
            Object[] param = new Object[]{notice.getUserId(), title, content, now};

            if (!alarmDao.update(sql, param)) {
                logger.error("新增车辆报警信息失败！SQL:[{}], 参数:[{}]", sql, param);
            }
        }

        if (notice.getEmail() == 1) {
            Map map = new HashMap();
            map.put("address", notice.getMailAddress());
            map.put("subject", title);
            map.put("content", content);
            map.put("time", now);

            RPTuple tuple = new RPTuple();
            tuple.setTerminalID(String.valueOf(vehicleInfo.getId()));
            tuple.setTime(now.getTime());
            String msgBody = JacksonUtil.toJson(map);
            tuple.setMsgBody(msgBody.getBytes(Charset.forName(EStarConstant.JSON_CHARSET)));


            logger.info("终端[{}]写入Kafka邮件报警信息...", vehicleInfo.getId());
            storeInKafka(tuple, processorConf.get(EStarConstant.Kafka.EMAIL_TOPIC));
        }

        if (notice.getSms() == 1) {
            Map map = new HashMap();
            map.put("mobile", notice.getMobile());
            map.put("content", content);
            map.put("time", now);

            RPTuple tuple = new RPTuple();
            tuple.setTerminalID(String.valueOf(vehicleInfo.getId()));
            tuple.setTime(now.getTime());
            String msgBody = JacksonUtil.toJson(map);
            tuple.setMsgBody(msgBody.getBytes(Charset.forName(EStarConstant.JSON_CHARSET)));

            logger.info("终端[{}]写入Kafka短信报警信息...", vehicleInfo.getId());
            storeInKafka(tuple, processorConf.get(EStarConstant.Kafka.SMS_TOPIC));
        }
    }
}
