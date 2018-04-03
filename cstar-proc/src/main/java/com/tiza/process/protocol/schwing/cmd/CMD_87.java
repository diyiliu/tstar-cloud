package com.tiza.process.protocol.schwing.cmd;

import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.common.model.*;
import com.tiza.process.protocol.schwing.SchwingDataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_87
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_87 extends SchwingDataProcess {

    public CMD_87() {
        this.cmd = 0x87;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] positionArray = new byte[22];
        buf.readBytes(positionArray);

        Position position = renderPosition(positionArray);
        Status status = renderStatus(position.getStatus());
        // 车辆在线
        status.setOnOff(1);

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

        /*
        * 施维英协议新增内容
        * 正反转、转速、油位状态量、油位
        * */
        if (parameters.containsKey("05")) {
            int rotateDirection = CommonUtil.getNoSin(parameters.get("05")[0]);
            int rotateSpeed = CommonUtil.getNoSin(parameters.get("05")[1]);

            param.setRotateDirection(rotateDirection);
            param.setRotateSpeed(rotateSpeed);
        }
        if (parameters.containsKey("06")) {
            Long stateVolume = CommonUtil.bytesToLong(parameters.get("06"));
            param.setStateVolume(stateVolume.intValue());
        }
        if (parameters.containsKey("07")) {
            Long fuelVolume = CommonUtil.bytesToLong(parameters.get("07"));
            param.setFuelVolume(fuelVolume.intValue());
        }

        toKafka(m2Header, position, status, param);

        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(m2Header.getTerminalId());
        FunctionInfo functionInfo = null;
                //(CanInfo) canCacheProvider.get(vehicleInfo.getSoftVersion());
        Map emptyValues = null;
        try {
            emptyValues = functionInfo.getEmptyValues();
        } catch (Exception e) {
            logger.error("没有can数据");
        }
        if (functionInfo != null && parameters.containsKey(functionInfo.getModelCode())){
            byte[] bytes = parameters.get(functionInfo.getModelCode());
            Map<String, CanPackage> canPackages = functionInfo.getCanPackages();

            try {
                Map canValues = parseCan(bytes, canPackages, functionInfo.getPidLength());
                emptyValues.putAll(canValues);
            } catch (Exception e) {
                logger.error("can数据 解析异常！"+e.getMessage());
            }
        }
        param.setCanValues(emptyValues);
    }

    private Map parseParameter(byte[] content) {
        Map parameters = new HashMap<>();

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

    private Map parseCan(byte[] bytes, Map<String, CanPackage> canPackages, int idLength){

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        Map canValues = new HashedMap();
        while (buf.readableBytes() > idLength){
            byte[] idBytes = new byte[idLength];
            buf.readBytes(idBytes);

            String packageId = CommonUtil.bytesToStr(idBytes);
            if (!canPackages.containsKey(packageId)){
                logger.error("未配置的功能集[{}]", packageId);
                break;
            }

            CanPackage canPackage = canPackages.get(packageId);
            if (buf.readableBytes() < canPackage.getLength()){
                logger.error("功能集数据不足！");
                break;
            }
            byte[] content = new byte[canPackage.getLength()];
            buf.readBytes(content);

            Map values = parsePackage(content, canPackage.getItemList());
            canValues.putAll(values);
        }

        return canValues;
    }
}
