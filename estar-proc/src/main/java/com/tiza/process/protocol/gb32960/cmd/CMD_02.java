package com.tiza.process.protocol.gb32960.cmd;

import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.model.Position;
import com.tiza.process.protocol.bean.GB32960Header;
import com.tiza.process.protocol.gb32960.GB32960DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Description: CMD_02
 * Author: Wangw
 * Update: 2017-09-07 14:57
 */

@Service
public class CMD_02 extends GB32960DataProcess {

    public CMD_02() {
        this.cmd = 0x02;
    }

    /**
     * 当前位置表参数
     */
    private List<Map> paramValues;

    /**
     * 指令中的当前时间
     */
    private Date currentTime;

    /**
     * 处理流上下文
     */
    private Map<String, String> context;


    private Map realMode;

    @Override
    public void parse(byte[] content, Header header) {
        GB32960Header gb32960Header = (GB32960Header) header;
        RPTuple tuple = (RPTuple) gb32960Header.gettStarData();
        context = tuple.getContext();

        ByteBuf buf = Unpooled.copiedBuffer(content);
        paramValues = new ArrayList<>();

        byte[] dateBytes = new byte[6];
        buf.readBytes(dateBytes);
        currentTime = CommonUtil.bytesToDate(dateBytes);
        tuple.setTime(currentTime.getTime());

        Map map = new HashMap();
        map.put("SystemTime", new Date());
        map.put("GpsTime", currentTime);
        paramValues.add(map);

        realMode = new HashMap();
        // 中断标识
        boolean interrupt = false;
        while (buf.readableBytes() > 0) {
            int flag = buf.readByte();
            switch (flag) {

                case 0x01:

                    interrupt = parseVehicle(buf);
                    break;
                case 0x02:

                    interrupt = parseMotor(buf);
                    break;
                case 0x03:

                    interrupt = parseBattery(buf);
                    break;
                case 0x04:

                    interrupt = parseEngine(buf);
                    break;
                case 0x05:

                    interrupt = parsePosition(buf);
                    break;
                case 0x06:

                    interrupt = parseExtreme(buf);
                    break;
                case 0x07:

                    interrupt = parseAlarm(buf);
                    break;

                case 0x08:

                    interrupt = parseStorageVoltage(buf);
                    break;
                case 0x09:

                    interrupt = parseStorageTemp(buf);
                    break;
                default:
                    if (buf.readableBytes() > 2) {
                        int length = buf.readUnsignedShort();
                        if (buf.readableBytes() < length){
                            interrupt = true;
                            break;
                        }
                        buf.readBytes(new byte[length]);
                    }
                    break;

            }
            if (interrupt) {
                logger.error("指令cmd[{}], 解析中断错误!", CommonUtil.toHex(flag));
                break;
            }
        }

        updateGpsInfo((GB32960Header) header, paramValues);

        // 车辆实时状态
        if (MapUtils.isNotEmpty(realMode)) {
            context.put(EStarConstant.FlowKey.REAL_MODE, JacksonUtil.toJson(realMode));
        }
    }

    /**
     * 整车数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseVehicle(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 20) {

            return true;
        }

        int vehStatus = byteBuf.readUnsignedByte();
        if (0x01 == vehStatus || 0x02 == vehStatus) {
            realMode.put(EStarConstant.RealMode.ON_OFF, vehStatus);
        }
        int charge = byteBuf.readUnsignedByte();
        if (0x01 == charge || 0x04 == charge) {
            realMode.put(EStarConstant.RealMode.TOP_OFF, charge);
        }
        int runMode = byteBuf.readUnsignedByte();

        int speed = byteBuf.readUnsignedShort();
        long mile = byteBuf.readUnsignedInt();

        int voltage = byteBuf.readUnsignedShort();
        int electricity = byteBuf.readUnsignedShort();

        int soc = byteBuf.readUnsignedByte();
        int dcStatus = byteBuf.readUnsignedByte();

        int gears = byteBuf.readUnsignedByte();
        int ohm = byteBuf.readUnsignedShort();
        byteBuf.readShort();


        Map map = new HashMap();
        map.put("VehicleStatus", vehStatus);
        map.put("ChargeStatus", charge);
        map.put("DrivingMode", runMode);

        // 速度
        if (0xFFFF == speed) {

            map.put("SpeedStatus", 255);
        } else if (0xFFFE == speed) {

            map.put("SpeedStatus", 254);
        } else {

            map.put("SpeedStatus", 1);
            map.put("Speed", speed * 0.1);
        }

        // 里程
        if (0xFFFFFFFFl == mile) {

            map.put("ODOStatus", 255);
        } else if (0xFFFFFFFEl == mile) {

            map.put("ODOStatus", 254);
        } else {

            map.put("ODOStatus", 1);
            map.put("ODO", mile * 0.1);
        }

        // 电压
        if (0xFFFF == voltage) {

            map.put("VoltageStatus", 255);
        } else if (0xFFFE == voltage) {

            map.put("VoltageStatus", 254);
        } else {

            map.put("VoltageStatus", 1);
            map.put("Voltage", voltage * 0.1);
        }

        // 电流
        if (0xFFFF == electricity) {

            map.put("AmpStatus", 255);
        } else if (0xFFFE == electricity) {

            map.put("AmpStatus", 254);
        } else {

            map.put("AmpStatus", 1);
            map.put("Amp", electricity * 0.1 - 1000);
        }

        // SOC
        if (0xFF == soc || 0xFE == soc) {

            map.put("SOCStatus", soc);
        } else {

            map.put("SOCStatus", 1);
            map.put("SOC", soc);
        }

        // DC-DC
        map.put("DCDC", dcStatus);

        map.put("Gears", gears);

        map.put("Resistance", ohm);

        paramValues.add(map);

        return false;
    }

    /**
     * 驱动电机数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseMotor(ByteBuf byteBuf) {

        int count = byteBuf.readUnsignedByte();
        if (byteBuf.readableBytes() < count * 12) {

            return true;
        }

        for (int i = 0; i < count; i++) {

            int serial = byteBuf.readUnsignedByte();
            int status = byteBuf.readUnsignedByte();

            int controlTemp = byteBuf.readUnsignedByte();

            int rpm = byteBuf.readUnsignedShort();
            int torque = byteBuf.readUnsignedShort();

            int temp = byteBuf.readUnsignedByte();
            int voltage = byteBuf.readUnsignedShort();
            int electricity = byteBuf.readUnsignedShort();
        }

        return false;
    }

    /**
     * 燃料电池数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseBattery(ByteBuf byteBuf) {

        if (byteBuf.readableBytes() < 8) {

            return true;
        }
        int voltage = byteBuf.readUnsignedShort();
        int electricity = byteBuf.readUnsignedShort();

        int drain = byteBuf.readUnsignedShort();

        int count = byteBuf.readUnsignedShort();
        // 数量无效
        if (0xFFFE == count || 0xFFFF == count) {

            return false;
        }

        if (byteBuf.readableBytes() < count * 10) {

            return true;
        }

        for (int i = 0; i < count; i++) {

            int maxTemp = byteBuf.readUnsignedShort();
            int tempNumber = byteBuf.readUnsignedByte();

            int maxPPM = byteBuf.readUnsignedShort();
            int ppmNumber = byteBuf.readUnsignedByte();

            int maxPressure = byteBuf.readShort();
            int pressureNumber = byteBuf.readUnsignedByte();

            int dcStatus = byteBuf.readUnsignedByte();
        }

        return false;
    }

    /**
     * 发动机数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseEngine(ByteBuf byteBuf) {

        if (byteBuf.readableBytes() < 5) {

            return true;
        }

        int status = byteBuf.readUnsignedByte();
        int speed = byteBuf.readUnsignedShort();
        int drain = byteBuf.readUnsignedShort();

        return false;
    }

    /**
     * 车辆位置数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parsePosition(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 9) {

            return true;
        }

        int status = byteBuf.readByte();

        //0:有效;1:无效
        int effective = status & 0x01;
        int latDir = status & 0x02;
        int lngDir = status & 0x04;

        long lng = byteBuf.readUnsignedInt();
        long lat = byteBuf.readUnsignedInt();

        Position position = new Position();
        position.setStatus(effective);
        position.setLng(lng * (lngDir == 0 ? 1 : -1));
        position.setLat(lat * (latDir == 0 ? 1 : -1));

        Map map = new HashMap();
        map.put("position", position);
        paramValues.add(map);

        return false;
    }

    /**
     * 极值数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseExtreme(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 14) {

            return true;
        }

        Map map = new HashMap();
        // 最高电压
        int maxVoltageSysNo = byteBuf.readUnsignedByte();
        if (0xFF == maxVoltageSysNo || 0xFE == maxVoltageSysNo) {
            map.put("MaxVoltageBatterySubSysStatus", maxVoltageSysNo);
        } else {
            map.put("MaxVoltageBatterySubSysStatus", 1);
            map.put("MaxVoltageBatterySubSys", maxVoltageSysNo);
        }
        int maxVoltageCellNo = byteBuf.readUnsignedByte();
        if (0xFF == maxVoltageCellNo || 0xFE == maxVoltageCellNo) {
            map.put("MaxVoltageBatteryUnitStatus", maxVoltageCellNo);
        } else {
            map.put("MaxVoltageBatteryUnitStatus", 1);
            map.put("MaxVoltageBatteryUnit", maxVoltageCellNo);
        }
        int maxVoltageValue = byteBuf.readUnsignedShort();
        if (0xFF == maxVoltageValue || 0xFE == maxVoltageValue) {
            map.put("BatteryUnitMaxVoltageStatus", maxVoltageValue);
        } else {
            map.put("BatteryUnitMaxVoltageStatus", 1);
            map.put("BatteryUnitMaxVoltage", maxVoltageValue * 0.001);
        }

        // 最低电压
        int minVoltageSysNo = byteBuf.readUnsignedByte();
        if (0xFF == minVoltageSysNo || 0xFE == minVoltageSysNo) {
            map.put("MinVoltageBatterySubSysStatus", minVoltageSysNo);
        } else {
            map.put("MinVoltageBatterySubSysStatus", 1);
            map.put("MinVoltageBatterySubSys", minVoltageSysNo);
        }
        int minVoltageCellNo = byteBuf.readUnsignedByte();
        if (0xFF == minVoltageCellNo || 0xFE == minVoltageCellNo) {
            map.put("MinVoltageBatteryUnitStatus", minVoltageCellNo);
        } else {
            map.put("MinVoltageBatteryUnitStatus", 1);
            map.put("MinVoltageBatteryUnit", minVoltageCellNo);
        }
        int minVoltageValue = byteBuf.readUnsignedShort();
        if (0xFF == minVoltageValue || 0xFE == minVoltageValue) {
            map.put("BatteryUnitMinVoltageStatus", minVoltageValue);
        } else {
            map.put("BatteryUnitMinVoltageStatus", 1);
            map.put("BatteryUnitMinVoltage", minVoltageValue * 0.001);
        }

        // 最高温度
        int maxTempSysNo = byteBuf.readUnsignedByte();
        if (0xFF == maxTempSysNo || 0xFE == maxTempSysNo) {
            map.put("MaxTempBatterySubSysStatus", maxTempSysNo);
        } else {
            map.put("MaxTempBatterySubSysStatus", 1);
            map.put("MaxTempBatterySubSys", maxTempSysNo);
        }
        int maxTempCellNo = byteBuf.readUnsignedByte();
        if (0xFF == maxTempCellNo || 0xFE == maxTempCellNo) {
            map.put("MaxTempBatterySensorStatus", maxTempCellNo);
        } else {
            map.put("MaxTempBatterySensorStatus", 1);
            map.put("MaxTempBatterySensor", maxTempCellNo);
        }
        int maxTempValue = byteBuf.readUnsignedByte();
        if (0xFF == maxTempValue || 0xFE == maxTempValue) {
            map.put("BatteryMaxTempStatus", maxTempValue);
        } else {
            map.put("BatteryMaxTempStatus", 1);
            map.put("BatteryMaxTemp", maxTempValue - 40);
        }

        // 最低温度
        int minTempSysNo = byteBuf.readUnsignedByte();
        if (0xFF == minTempSysNo || 0xFE == minTempSysNo) {
            map.put("MinTempBatterySubSysStatus", minTempSysNo);
        } else {
            map.put("MinTempBatterySubSysStatus", 1);
            map.put("MinTempBatterySubSys", minTempSysNo);
        }
        int minTempCellNo = byteBuf.readUnsignedByte();
        if (0xFF == minTempCellNo || 0xFE == minTempCellNo) {
            map.put("MinTempBatterySensorStatus", minTempCellNo);
        } else {
            map.put("MinTempBatterySensorStatus", 1);
            map.put("MinTempBatterySensor", minTempCellNo);
        }
        int minTempValue = byteBuf.readUnsignedByte();
        if (0xFF == minTempValue || 0xFE == minTempValue) {
            map.put("BatteryMinTempStatus", minTempValue);
        } else {
            map.put("BatteryMinTempStatus", 1);
            map.put("BatteryMinTemp", minTempValue - 40);
        }

        paramValues.add(map);
        // 报警数据加入上下文中，交给下一个流程处理
        context.put(EStarConstant.FlowKey.VEHICLE_EXTREME, JacksonUtil.toJson(map));
        return false;
    }

    /**
     * 报警数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseAlarm(ByteBuf byteBuf) {
        int level = byteBuf.readUnsignedByte();
        long flag = byteBuf.readUnsignedInt();

        Map alarm = new HashMap();
        alarm.put("AlarmLevel", level);
        // 有效报警值[0, 3]
        if (level > -1 && level < 4) {
            alarm.put("AlarmTime", currentTime);
            realMode.put(EStarConstant.RealMode.ALARM_LEVEL, level);
            context.put(EStarConstant.FlowKey.ALARM_LEVEL, JacksonUtil.toJson(alarm));
        }
        paramValues.add(alarm);

        Map commonAlarm = toCommonAlarm(flag);
        paramValues.add(commonAlarm);

        Map faultMap = new HashMap();
        int chargeFault = byteBuf.readUnsignedByte();
        if (0xFE != chargeFault && 0xFF != chargeFault && chargeFault > 0) {
            if (byteBuf.readableBytes() < chargeFault * 4 + 3) {
                return true;
            }
            List list = new ArrayList();
            for (int i = 0; i < chargeFault; i++) {

                long l = byteBuf.readUnsignedInt();
                list.add(l);
            }
            faultMap.put(1, list);
        }

        int motorFault = byteBuf.readUnsignedByte();
        if (0xFE != motorFault && 0xFF != motorFault && motorFault > 0) {
            if (byteBuf.readableBytes() < chargeFault * 4 + 2) {
                return true;
            }
            List list = new ArrayList();
            for (int i = 0; i < motorFault; i++) {

                long l = byteBuf.readUnsignedInt();
                list.add(l);
            }
            faultMap.put(2, list);
        }

        int engineFault = byteBuf.readUnsignedByte();
        if (0xFE != engineFault && 0xFF != engineFault && engineFault > 0) {
            if (byteBuf.readableBytes() < chargeFault * 4 + 1) {
                return true;
            }
            List list = new ArrayList();
            for (int i = 0; i < engineFault; i++) {

                long l = byteBuf.readUnsignedInt();
                list.add(l);
            }
            faultMap.put(3, list);
        }

        int otherFault = byteBuf.readUnsignedByte();
        if (0xFE != otherFault && 0xFF != otherFault && otherFault > 0) {
            if (byteBuf.readableBytes() < chargeFault * 4) {
                return true;
            }
            List list = new ArrayList();
            for (int i = 0; i < otherFault; i++) {

                long l = byteBuf.readUnsignedInt();
                list.add(l);
            }
            //faultMap.put(4, list);
        }

        // 报警数据加入上下文中，交给下一个流程处理
        context.put(EStarConstant.FlowKey.VEHICLE_FAULT, JacksonUtil.toJson(faultMap));

        return false;
    }


    /**
     * 可充电储能电压数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseStorageVoltage(ByteBuf byteBuf) {

        int count = byteBuf.readUnsignedByte();
        if (0xFE == count || 0xFF == count) {

            return false;
        }
        if (byteBuf.readableBytes() < count * 10) {

            return true;
        }
        for (int i = 0; i < count; i++) {

            int sumSysNo = byteBuf.readUnsignedByte();
            int voltage = byteBuf.readUnsignedShort();
            int electricity = byteBuf.readUnsignedShort();

            int battery = byteBuf.readUnsignedShort();
            int serial = byteBuf.readUnsignedShort();

            int m = byteBuf.readUnsignedByte();
            if (byteBuf.readableBytes() < m * 2) {

                return true;
            }

            for (int j = 0; j < m; j++) {

                int kv = byteBuf.readUnsignedShort();
            }
        }

        return false;
    }


    /**
     * 可充电储能温度数据
     *
     * @param byteBuf
     * @return
     */
    private boolean parseStorageTemp(ByteBuf byteBuf) {

        int count = byteBuf.readUnsignedByte();
        if (0xFE == count || 0xFF == count) {

            return false;
        }
        if (byteBuf.readableBytes() < count * 3) {

            return true;
        }
        for (int i = 0; i < count; i++) {

            int sumSysNo = byteBuf.readUnsignedByte();
            int n = byteBuf.readUnsignedShort();

            if (0xFE == n || 0xFF == n) {
                continue;
            }
            if (byteBuf.readableBytes() < n) {

                return true;
            }
            for (int j = 0; j < n; j++) {

                int temp = byteBuf.readUnsignedByte();
            }
        }

        return false;
    }


    /**
     * 解析通用报警标志位
     *
     * @param flag
     * @return
     */
    private Map toCommonAlarm(long flag) {
        String[] alarmArray = new String[]{"TempDiffAlarm", "BatteryHighTempAlarm",
                "HighPressureAlarm", "LowPressureAlarm",
                "SocLowAlarm", "BatteryUnitHighVoltageAlarm",
                "BatteryUnitLowVoltageAlarm", "SocHighAlarm",
                "SocJumpAlarm", "BatteryMismatchAlarm",
                "BatteryUnitUniformityAlarm", "InsulationAlarm",
                "DCDCTempAlarm", "BrakeAlarm",
                "DCDCStatusAlarm", "MotorCUTempAlarm",
                "HighPressureLockAlarm", "MotorTempAlarm", "BatteryOverChargeAlarm"};

        Map alarmMap = new HashMap();

        for (int i = 0; i < alarmArray.length; i++) {
            String column = alarmArray[i];
            long value = (flag >> i) & 0x01;

            alarmMap.put(column, value);
        }

        return alarmMap;
    }
}
