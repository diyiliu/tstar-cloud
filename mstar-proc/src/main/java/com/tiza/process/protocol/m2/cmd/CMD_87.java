package com.tiza.process.protocol.m2.cmd;

import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.dao.VehicleDao;
import com.tiza.process.common.model.*;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.protocol.m2.M2DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Description: CMD_87
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_87 extends M2DataProcess {

    public CMD_87() {
        this.cmd = 0x87;
    }

    private Date gpsTime;

    private M2Header m2Header;

    @Override
    public void parse(byte[] content, Header header) {
        m2Header = (M2Header) header;
        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] positionArray = new byte[22];
        buf.readBytes(positionArray);

        Position position = renderPosition(positionArray);
        gpsTime = position.getDateTime();

        /*Status status = renderStatus(position.getStatus());
        // 车辆在线
        status.setOnOff(1);*/

        byte[] paramArray = new byte[buf.readableBytes()];
        buf.readBytes(paramArray);

        Map<String, byte[]> parameters = parseParameter(paramArray);

        Parameter param = new Parameter();
        if (parameters.containsKey("01")) {
            long accTime = CommonUtil.bytesToLong(parameters.get("01"));
            param.setAccTime(accTime);
        }
        if (parameters.containsKey("02")) {
            int gsmSignal = CommonUtil.getNoSin(parameters.get("02")[0]);
            param.setGsmSignal(gsmSignal);
        }
        if (parameters.containsKey("03")) {
            double voltage = CommonUtil.bytesToLong(parameters.get("03"));
            param.setVoltage(voltage);
        }
        if (parameters.containsKey("04")) {
            int satellite = CommonUtil.getNoSin(parameters.get("04")[0]);
            param.setSatellite(satellite);
        }

        FunctionInfo functionInfo = getFunctionInfo(m2Header.getTerminalId());
        if (functionInfo != null) {
            Map statusValues = parsePackage(position.getStatusBytes(), functionInfo.getStatusItems());
            position.setStatusMap(statusValues);

            Map emptyValues = null;
            try {
                emptyValues = functionInfo.getEmptyValues();
            } catch (Exception e) {
                logger.error("没有can数据");
            }

            logger.info("设备[{}]功能配置[{}, {}]", m2Header.getTerminalId(), JacksonUtil.toJson(parameters.keySet()), functionInfo.getModelCode());
            if (parameters.containsKey(functionInfo.getModelCode())) {
                byte[] bytes = parameters.get(functionInfo.getModelCode());
                Map<String, CanPackage> canPackages = functionInfo.getCanPackages();

                try {
                    Map canValues = parseCan(bytes, canPackages, functionInfo.getPidLength());
                    m2Header.setCanData(canValues);
                    emptyValues.putAll(canValues);
                    logger.info("设备[{}] CAN 数据[{}]", m2Header.getTerminalId(), JacksonUtil.toJson(canValues));
                } catch (Exception e) {
                    logger.error("can数据 解析异常！" + e.getMessage());
                }
            }
            param.setCanValues(emptyValues);
        }

        toKafka(m2Header, position, param);
    }

    private Map parseParameter(byte[] content) {
        Map parameters = new HashMap();
        ByteBuf byteBuf = Unpooled.copiedBuffer(content);

        while (byteBuf.readableBytes() > 4) {
            int id = byteBuf.readUnsignedShort();
            int length = byteBuf.readUnsignedShort();
            if (byteBuf.readableBytes() < length) {
                logger.error("工况数据长度不足！");
                break;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);

            parameters.put(CommonUtil.toHex(id), bytes);
        }

        return parameters;
    }

    private Map parseCan(byte[] bytes, Map<String, CanPackage> canPackages, int idLength) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        Map canValues = new HashedMap();
        while (buf.readableBytes() > idLength) {
            byte[] idBytes = new byte[idLength];
            buf.readBytes(idBytes);

            String packageId = CommonUtil.bytesToStr(idBytes);
            if (!canPackages.containsKey(packageId)) {
                logger.error("未配置的功能集[{}]", packageId);
                break;
            }

            CanPackage canPackage = canPackages.get(packageId);
            if (buf.readableBytes() < canPackage.getLength()) {
                logger.error("功能集数据不足！");
                break;
            }
            byte[] content = new byte[canPackage.getLength()];
            buf.readBytes(content);

            Map values = parsePackage(content, canPackage.getItemList());
            // 九合泵送数据统计
            if ("0117".equals(packageId)){
                String key = "STRING1";
                String flag = (String) values.get(key);
                if ("255".equals(flag)){
                    values.remove(key);
                    pumpData(values);
                }
            }

            canValues.putAll(values);
        }

        return canValues;
    }

    private void pumpData(Map map){
        VehicleInfo vehicle = (VehicleInfo) vehicleCacheProvider.get(m2Header.getTerminalId());

        List list = new ArrayList();
        Set<String> set = map.keySet();
        StringBuilder str1 = new StringBuilder();
        StringBuilder str2 = new StringBuilder();
        for (Iterator<String> iterator = set.iterator(); iterator.hasNext();){
            String field = iterator.next();
            Object value = map.get(field);
            list.add(value);
            str1.append(field).append(",");
            str2.append("?,");
        }
        str1.append("WORKTIME,CREATETIME,VEHICLEID");
        str2.append("?,?,?");
        list.add(gpsTime);
        list.add(new Date());
        list.add(vehicle.getId());

        String sql = "INSERT INTO ALY_QDJH_DETAILS(" +  str1 + ")VALUES(" +  str2 + ")";
        VehicleDao vehicleDao = SpringUtil.getBean("vehicleDao");
        vehicleDao.update(sql, list.toArray());
    }
}
