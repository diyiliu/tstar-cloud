package com.tiza.process.protocol.gb32960;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.map.MapLocation;
import com.diyiliu.common.map.MapUtil;
import com.diyiliu.common.model.Header;
import com.diyiliu.common.model.IDataProcess;
import com.diyiliu.common.util.DateUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.bean.GB32960Header;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.VehicleInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Description: GB32960DataProcess
 * Author: Wangw
 * Update: 2017-09-06 16:57
 */

@Service
public class GB32960DataProcess implements IDataProcess {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected int cmd = 0xFF;

    private static BaseHandle handler;

    @Resource
    protected ICache cmdCacheProvider;

    @Resource
    protected ICache vehicleCacheProvider;

    @Resource
    private VehicleDao vehicleDao;

    @Override
    public Header dealHeader(byte[] bytes) {

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        // 读取头标志[0x23,0x23]
        buf.readBytes(new byte[2]);

        int cmd = buf.readByte();
        int resp = buf.readByte();

        byte[] vinBytes = new byte[17];
        buf.readBytes(vinBytes);
        String vin = new String(vinBytes);

        // 加密方式
        buf.readByte();

        int length = buf.readUnsignedShort();
        byte[] content = new byte[length];
        buf.readBytes(content);

        GB32960Header header = new GB32960Header();
        header.setCmd(cmd);
        header.setResp(resp);
        header.setVin(vin);
        header.setContent(content);

        return header;
    }

    @Override
    public void parse(byte[] content, Header header) {

    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        return new byte[0];
    }

    /**
     * 更新车辆当前位置表
     *
     * @param header
     * @param paramValues
     */
    protected void updateGpsInfo(GB32960Header header, List<Map> paramValues) {

        String vin = header.getVin();
        if (!vehicleCacheProvider.containsKey(vin)) {
            logger.warn("[{}] 车辆列表不存在!", vin);
            return;
        }
        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(vin);

        Map kafkaMap = new HashMap();

        Date gpsTime = null;
        List list = new ArrayList();
        StringBuilder strb = new StringBuilder("update BS_VEHICLEGPSINFO set ");
        for (int i = 0; i < paramValues.size(); i++) {
            Map map = paramValues.get(i);
            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                Object value = map.get(key);

                if (key.equalsIgnoreCase("GpsTime")) {
                    gpsTime = (Date) value;
                    vehicleInfo.setDateTime(gpsTime.getTime());
                }
                if (key.equalsIgnoreCase("VehicleStatus")) {
                    vehicleInfo.setStatus((Integer) value);
                }

                if (key.equalsIgnoreCase("position")) {
                    Position position = (Position) value;
                    position.setDateTime(gpsTime);

                    strb.append("LocationStatus").append("=?, ");
                    list.add(position.getStatus());
                    kafkaMap.put("LocationStatus", position.getStatus());

                    // 有效定位
                    if (position.getStatus() == 0) {

                        strb.append("WGS84LAT").append("=?, ");
                        strb.append("WGS84LNG").append("=?, ");
                        strb.append("GCJ02LAT").append("=?, ");
                        strb.append("GCJ02LNG").append("=?, ");
                        list.add(position.getLatD());
                        list.add(position.getLngD());
                        list.add(position.getEnLatD());
                        list.add(position.getEnLngD());

                        kafkaMap.put("WGS84LAT", position.getLatD());
                        kafkaMap.put("WGS84LNG", position.getLngD());
                        kafkaMap.put("GCJ02LAT", position.getEnLatD());
                        kafkaMap.put("GCJ02LNG", position.getEnLngD());

                        if (position.getEnLatD() != null && position.getEnLngD() != null) {
                            //  解析省、市、区
                            MapLocation location = MapUtil.getArea(position.getEnLatD(), position.getEnLngD());
                            if (location != null) {
                                strb.append("PROVINCE").append("=?, ");
                                strb.append("CITY").append("=?, ");
                                strb.append("DISTRICT").append("=?, ");
                                list.add(location.getProvince());
                                list.add(location.getCity());
                                list.add(location.getTown());

                                kafkaMap.put("PROVINCE", location.getProvince());
                                kafkaMap.put("CITY", location.getCity());
                                kafkaMap.put("DISTRICT", location.getTown());
                            }
                        }

                        // 发布redis
                        if (0x02 == header.getCmd()) {
                            toRedis(header, vehicleInfo, position);
                        }
                    }
                    continue;
                }
                strb.append(key).append("=?, ");
                list.add(formatValue(value));
            }

            if (!map.containsKey("position")) {
                kafkaMap.putAll(map);
            }
        }

        // 写入kafka
        toKafka(header, vehicleInfo, kafkaMap);

        // 更新当前位置信息
        if (0x02 == header.getCmd()) {
            String sql = strb.substring(0, strb.length() - 2) + " where VEHICLEID=" + vehicleInfo.getId();
            vehicleDao.update(sql, list.toArray());
        }
    }

    private void toKafka(GB32960Header header, VehicleInfo vehicle, Map paramValues) {
        paramValues.put(EStarConstant.Location.VEHICLE_ID, vehicle.getId());

        RPTuple rpTuple = new RPTuple();
        rpTuple.setCmdID(header.getCmd());
        rpTuple.setCmdSerialNo(header.getSerial());
        rpTuple.setTerminalID(String.valueOf(vehicle.getId()));

        String msgBody = JacksonUtil.toJson(paramValues);
        rpTuple.setMsgBody(msgBody.getBytes(Charset.forName(EStarConstant.JSON_CHARSET)));
        rpTuple.setTime(vehicle.getDateTime());

        // 获取上下文中的配置信息
        RPTuple tuple = (RPTuple) header.gettStarData();
        Map<String, String> context = tuple.getContext();

        logger.info("终端[{}]写入Kafka位置信息...", header.getVin());
        handler.storeInKafka(rpTuple, context.get(EStarConstant.Kafka.TRACK_TOPIC));
    }

    private void toRedis(GB32960Header header, VehicleInfo vehicle, Position position) {

        Map posMap = new HashMap();
        posMap.put(EStarConstant.Location.GPS_TIME, DateUtil.dateToString(position.getDateTime()));
        posMap.put(EStarConstant.Location.LAT, position.getEnLatD());
        posMap.put(EStarConstant.Location.LNG, position.getEnLngD());
        posMap.put(EStarConstant.Location.VEHICLE_ID, vehicle.getId());

        posMap.put(EStarConstant.Location.STATUS, vehicle.getStatus() == null ? "" : vehicle.getStatus());

        // 获取上下文中的配置信息
        RPTuple tuple = (RPTuple) header.gettStarData();
        Map<String, String> context = tuple.getContext();

        logger.info("终端[{}]发布Redis位置信息...", header.getVin());
        Jedis jedis = handler.getJedis();
        try {
            String channel = context.get(EStarConstant.Redis.VEHICLE_MOVE);
            jedis.publish(channel, JacksonUtil.toJson(posMap));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Object formatValue(Object obj) {
        if (obj instanceof Map ||
                obj instanceof Collection) {

            return JacksonUtil.toJson(obj);
        }

        return obj;
    }

    @Override
    public void init() {
        cmdCacheProvider.put(cmd, this);
    }

    public static void setHandler(BaseHandle parseHandler) {
        handler = parseHandler;
    }
}
