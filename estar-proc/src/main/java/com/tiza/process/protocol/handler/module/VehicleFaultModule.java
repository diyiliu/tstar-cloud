package com.tiza.process.protocol.handler.module;

import cn.com.tiza.tstar.common.process.BaseHandle;
import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.util.DateUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.dao.FaultDao;
import com.tiza.process.common.model.FaultCode;
import com.tiza.process.common.model.VehicleFault;
import com.tiza.process.common.model.VehicleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 车辆通用故障
 *
 * Description: VehicleFaultModule
 * Author: DIYILIU
 * Update: 2017-10-12 14:19
 */
public class VehicleFaultModule extends BaseHandle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 故障代码库
     */
    private ICache faultCodeCache;

    /**
     * 车辆当前故障
     */
    private ICache vehicleFaultCache;


    private FaultDao faultDao;

    private long gpsTime;

    private RPTuple rpTuple;

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        Map<String, String> context = rpTuple.getContext();

        String terminalId = rpTuple.getTerminalID();
        ICache vehicleCache = SpringUtil.getBean("vehicleCacheProvider");
        if (context.containsKey(EStarConstant.FlowKey.VEHICLE_FAULT) && vehicleCache.containsKey(terminalId)) {
            this.rpTuple = rpTuple;
            gpsTime = rpTuple.getTime();

            VehicleInfo vehicleInfo = (VehicleInfo) vehicleCache.get(terminalId);
            String vehicleId = String.valueOf(vehicleInfo.getId());

            faultCodeCache = SpringUtil.getBean("faultCodeCacheProvider");
            vehicleFaultCache = SpringUtil.getBean("vehicleFaultCacheProvider");
            faultDao = SpringUtil.getBean("faultDao");

            Map faultMap = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.VEHICLE_FAULT), HashMap.class);
            for (Iterator iterator = faultMap.keySet().iterator(); iterator.hasNext(); ) {
                long key = (long) iterator.next();
                List value = (List) faultMap.get(key);

                dealFault(vehicleId, String.valueOf(key), value);
            }
        }

        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }

    public void dealFault(String vehicleId, String faultUnit, List list) {

        // 产生故障报警
        for (int i = 0; i < list.size(); i++) {
            String faultValue = String.valueOf(list.get(i));

            String key = faultUnit + "_" + faultValue;
            if (!faultCodeCache.containsKey(key)) {
                continue;
            }

            boolean exist = false;
            if (vehicleFaultCache.containsKey(vehicleId)) {
                List<VehicleFault> faultList = (List<VehicleFault>) vehicleFaultCache.get(vehicleId);
                for (VehicleFault fault : faultList) {
                    if (faultUnit.equals(fault.getFaultUnit()) &&
                            faultValue.equals(fault.getFaultValue())) {

                        exist = true;
                        if (fault.isOver()) {

                            fault.setStartTime(new Date(gpsTime));
                            fault.setEndTime(null);
                            toUpdate(fault);
                        }
                        break;
                    }
                }
            }

            if (!exist) {
                VehicleFault vehicleFault = new VehicleFault();
                vehicleFault.setVehicleId(Long.parseLong(vehicleId));
                vehicleFault.setFaultUnit(faultUnit);
                vehicleFault.setFaultValue(String.valueOf(faultValue));
                vehicleFault.setStartTime(new Date(gpsTime));

                toCreate(vehicleFault);

                if (vehicleFaultCache.containsKey(vehicleId)) {
                    List<VehicleFault> faultList = (List<VehicleFault>) vehicleFaultCache.get(vehicleId);
                    faultList.add(vehicleFault);
                }else {
                    List<VehicleFault> faultList = new ArrayList();
                    faultList.add(vehicleFault);

                    vehicleFaultCache.put(vehicleId, faultList);
                }
            }
        }

        // 解除故障报警
        if (vehicleFaultCache.containsKey(vehicleId)) {
            List<VehicleFault> faultList = (List<VehicleFault>) vehicleFaultCache.get(vehicleId);

            for (VehicleFault fault: faultList){

                String unit = fault.getFaultUnit();
                if (fault.isOver() || !unit.equals(faultUnit)){

                    continue;
                }

                long value = Long.parseLong(fault.getFaultValue());
                // 解除报警
                if (!list.contains(value)){

                    fault.setEndTime(new Date(gpsTime));
                    fault.setOver(true);
                    toUpdate(fault);
                }
            }
        }

    }

    public void toCreate(VehicleFault vehicleFault) {

        String sql = "INSERT INTO bs_vehiclefault(vehicleid,faultunit,faultvalue,starttime) VALUES(?,?,?,?)";

        Object[] param = new Object[]{vehicleFault.getVehicleId(), vehicleFault.getFaultUnit(), vehicleFault.getFaultValue(), vehicleFault.getStartTime()};

        if (!faultDao.update(sql, param)) {

            logger.error("新增车辆故障信息失败！");
        }

        toKafka(vehicleFault);
    }

    public void toUpdate(VehicleFault vehicleFault) {

        String sql = "UPDATE bs_vehiclefault t SET t.starttime=?, t.endtime=? " +
                "WHERE t.vehicleid=? AND t.faultunit=? AND t.faultvalue=?";

        Object[] param = new Object[]{vehicleFault.getStartTime(), vehicleFault.getEndTime(),
                vehicleFault.getVehicleId(), vehicleFault.getFaultUnit(), vehicleFault.getFaultValue()};

        if (!faultDao.update(sql, param)) {

            logger.error("更新车辆故障信息失败！");
        }

        toKafka(vehicleFault);
    }


    public void toKafka(VehicleFault vehicleFault){
        String key = vehicleFault.getFaultUnit() + "_" + vehicleFault.getFaultValue();
        FaultCode faultCode = (FaultCode) faultCodeCache.get(key);

        Map faultMap = new HashMap();
        faultMap.put(EStarConstant.Fault.FAULT_UNIT, vehicleFault.getFaultUnit());
        faultMap.put(EStarConstant.Fault.FAULT_VALUE, vehicleFault.getFaultValue());
        faultMap.put(EStarConstant.Fault.START_TIME, DateUtil.dateToString(vehicleFault.getStartTime()));
        faultMap.put(EStarConstant.Fault.END_TIME,
                vehicleFault.getStartTime() == null? "": DateUtil.dateToString(vehicleFault.getEndTime()));

        faultMap.put(EStarConstant.Fault.FAULT_NAME, faultCode.getName());
        faultMap.put(EStarConstant.Fault.FAULT_DESC, faultCode.getDesc());

        faultMap.put(EStarConstant.Fault.VEHICLE_ID, vehicleFault.getVehicleId());

        RPTuple tuple = new RPTuple();
        tuple.setTerminalID(String.valueOf(vehicleFault.getVehicleId()));
        tuple.setTime(vehicleFault.getStartTime().getTime());
        String msgBody = JacksonUtil.toJson(faultMap);
        tuple.setMsgBody(msgBody.getBytes(Charset.forName(EStarConstant.JSON_CHARSET)));

        // 获取上下文中的配置信息
        Map<String, String> context = rpTuple.getContext();

        logger.info("终端[{}]写入Kafka故障信息...", vehicleFault.getVehicleId());
        storeInKafka(tuple, context.get(EStarConstant.Kafka.FAULT_TOPIC));
    }
}
