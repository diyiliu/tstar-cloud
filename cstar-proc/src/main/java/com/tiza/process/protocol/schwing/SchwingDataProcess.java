package com.tiza.process.protocol.schwing;

import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.util.DateUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.common.config.CStarConstant;
import com.tiza.process.common.model.Parameter;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.Status;
import com.tiza.process.common.model.VehicleInfo;
import com.tiza.process.protocol.m2.M2DataProcess;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: SchwingDataProcess
 * Author: DIYILIU
 * Update: 2017-09-20 14:36
 */
public class SchwingDataProcess extends M2DataProcess {

    @Override
    public void toKafka(M2Header header, Position position, Status status, Parameter parameter) {
        String terminalId = header.getTerminalId();
        if (!vehicleCacheProvider.containsKey(terminalId)) {
            logger.warn("该终端[{}]不存在车辆列表中...", terminalId);
            return;
        }
        VehicleInfo vehicle = (VehicleInfo) vehicleCacheProvider.get(terminalId);

        Map posMap = new HashMap();
        posMap.put(CStarConstant.Location.GPS_TIME,
                DateUtil.dateToString(position.getDateTime()));
        posMap.put(CStarConstant.Location.SPEED, position.getSpeed());
        posMap.put(CStarConstant.Location.ALTITUDE, position.getHeight());
        posMap.put(CStarConstant.Location.DIRECTION, position.getDirection());
        posMap.put(CStarConstant.Location.LOCATION_STATUS, status.getLocation());
        posMap.put(CStarConstant.Location.ACC_STATUS, status.getAcc());
        posMap.put(CStarConstant.Location.ORIGINAL_LNG, position.getLngD());
        posMap.put(CStarConstant.Location.ORIGINAL_LAT, position.getLatD());
        posMap.put(CStarConstant.Location.LNG, position.getEnLngD());
        posMap.put(CStarConstant.Location.LAT, position.getEnLatD());

        // 正反转、转速、油位
        posMap.put(CStarConstant.Parameter.ACC_HOUR, parameter.getAccHour());
        posMap.put(CStarConstant.Parameter.ROTATE_DIRECTION, parameter.getRotateDirection());
        posMap.put(CStarConstant.Parameter.ROTATE_SPEED, parameter.getRotateSpeed());
        posMap.put(CStarConstant.Parameter.FUEL_VOLUME, parameter.getFuelVolume());

        posMap.put("VehicleId", vehicle.getId());

        RPTuple rpTuple = new RPTuple();
        rpTuple.setCmdID(header.getCmd());
        rpTuple.setCmdSerialNo(header.getSerial());

        rpTuple.setTerminalID(String.valueOf(vehicle.getId()));

        String msgBody = JacksonUtil.toJson(posMap);
        rpTuple.setMsgBody(msgBody.getBytes(Charset.forName("UTF-8")));
        rpTuple.setTime(position.getDateTime().getTime());

        RPTuple tuple = (RPTuple) header.gettStarData();
        tuple.setTerminalID(String.valueOf(vehicle.getId()));

        // 将解析的位置和状态放入上下文中
        Map<String, String> context = tuple.getContext();
        context.put(CStarConstant.FlowKey.POSITION, JacksonUtil.toJson(position));
        context.put(CStarConstant.FlowKey.STATUS, JacksonUtil.toJson(status));
        context.put(CStarConstant.FlowKey.PARAMETER, JacksonUtil.toJson(parameter));

        logger.info("终端[{}]写入Kafka位置信息...", terminalId);
        handler.storeInKafka(rpTuple, context.get(CStarConstant.Kafka.TRACK_TOPIC));
    }
}
