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
 * <p>
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

    @Override
    public RPTuple handle(RPTuple rpTuple) throws Exception {
        Map<String, String> context = rpTuple.getContext();

        String terminalId = rpTuple.getTerminalID();
        ICache vehicleCache = SpringUtil.getBean("vehicleCacheProvider");
        if (context.containsKey(EStarConstant.FlowKey.VEHICLE_FAULT) && vehicleCache.containsKey(terminalId)) {
            VehicleInfo vehicleInfo = (VehicleInfo) vehicleCache.get(terminalId);
            vehicleInfo.setDateTime(rpTuple.getTime());

            faultCodeCache = SpringUtil.getBean("faultCodeCacheProvider");
            vehicleFaultCache = SpringUtil.getBean("vehicleFaultCacheProvider");
            faultDao = SpringUtil.getBean("faultDao");
            Map faultMap = JacksonUtil.toObject(context.get(EStarConstant.FlowKey.VEHICLE_FAULT), HashMap.class);
            for (Iterator iterator = faultMap.keySet().iterator(); iterator.hasNext(); ) {
                String key = String.valueOf(iterator.next());
                List value = (List) faultMap.get(key);
                dealFault(vehicleInfo, key, value);
            }
        }

        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }

    public void dealFault(VehicleInfo vehicleInfo, String faultUnit, List list) {
        String vehicleId = String.valueOf(vehicleInfo.getId());
        Date current = new Date(vehicleInfo.getDateTime());

        // 解除故障报警
        if (vehicleFaultCache.containsKey(vehicleId)) {
            List<VehicleFault> faultList = (List<VehicleFault>) vehicleFaultCache.get(vehicleId);
            for (VehicleFault fault : faultList) {
                String unit = fault.getFaultUnit();
                if (fault.isOver() || !unit.equals(faultUnit)) {

                    continue;
                }

                // 解除报警
                if (!hasValue(list, fault.getFaultValue())
                        && current.after(fault.getStartTime())) {

                    fault.setEndTime(current);
                    fault.setOver(true);
                    toUpdate(fault);
                }
            }
        }

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

                        // 更新报警
                        if (fault.isOver() &&
                                current.after(fault.getEndTime())) {

                            fault.setStartTime(current);
                            fault.setOver(false);
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
                vehicleFault.setStartTime(current);

                logger.warn("新增报警:  fault[{}]", JacksonUtil.toJson(vehicleFault));
                toCreate(vehicleFault);
                if (vehicleFaultCache.containsKey(vehicleId)) {
                    List<VehicleFault> faultList = (List<VehicleFault>) vehicleFaultCache.get(vehicleId);
                    faultList.add(vehicleFault);
                } else {
                    List<VehicleFault> faultList = new ArrayList();
                    faultList.add(vehicleFault);

                    vehicleFaultCache.put(vehicleId, faultList);
                }
            }
        }
    }

    public void toCreate(VehicleFault vehicleFault) {

        String sql = "INSERT INTO bs_vehiclefault(id,vehicleid,faultunit,faultvalue,starttime) VALUES(sq_bs_vehiclefault.nextval,?,?,?,?)";

        Object[] param = new Object[]{vehicleFault.getVehicleId(), vehicleFault.getFaultUnit(), vehicleFault.getFaultValue(), vehicleFault.getStartTime()};

        if (!faultDao.update(sql, param)) {

            logger.error("新增车辆故障信息失败！SQL:[{}], 参数:[{}]", sql, param);
        }

        toKafka(vehicleFault);
    }

    public void toUpdate(VehicleFault vehicleFault) {

        String sql = "UPDATE bs_vehiclefault t SET t.starttime=?, t.endtime=? " +
                "WHERE t.vehicleid=? AND t.faultunit=? AND t.faultvalue=?";

        Object[] param = new Object[]{vehicleFault.getStartTime(), vehicleFault.getEndTime(),
                vehicleFault.getVehicleId(), vehicleFault.getFaultUnit(), vehicleFault.getFaultValue()};

        if (!faultDao.update(sql, param)) {

            logger.error("更新车辆故障信息失败！SQL:[{}], 参数:[{}]", sql, param);
        }

        toKafka(vehicleFault);
    }


    public void toKafka(VehicleFault vehicleFault) {
        String key = vehicleFault.getFaultUnit() + "_" + vehicleFault.getFaultValue();
        FaultCode faultCode = (FaultCode) faultCodeCache.get(key);

        Map faultMap = new HashMap();
        faultMap.put(EStarConstant.Fault.FAULT_UNIT, vehicleFault.getFaultUnit());
        faultMap.put(EStarConstant.Fault.FAULT_VALUE, vehicleFault.getFaultValue());
        faultMap.put(EStarConstant.Fault.START_TIME, DateUtil.dateToString(vehicleFault.getStartTime()));
        faultMap.put(EStarConstant.Fault.END_TIME,
                vehicleFault.getStartTime() == null ? "" : DateUtil.dateToString(vehicleFault.getEndTime()));

        faultMap.put(EStarConstant.Fault.FAULT_NAME, faultCode.getName());
        faultMap.put(EStarConstant.Fault.FAULT_DESC, faultCode.getDesc());

        faultMap.put(EStarConstant.Fault.VEHICLE_ID, vehicleFault.getVehicleId());

        RPTuple tuple = new RPTuple();
        tuple.setTerminalID(String.valueOf(vehicleFault.getVehicleId()));
        tuple.setTime(vehicleFault.getStartTime().getTime());
        String msgBody = JacksonUtil.toJson(faultMap);
        tuple.setMsgBody(msgBody.getBytes(Charset.forName(EStarConstant.JSON_CHARSET)));

        logger.info("终端[{}]写入Kafka故障信息...", vehicleFault.getVehicleId());
        storeInKafka(tuple, processorConf.get(EStarConstant.Kafka.FAULT_TOPIC));
    }

    /**
     * 是否存在故障信息
     *
     * @param list
     * @param value
     * @return
     */
    private boolean hasValue(List list, String value) {

        for (int i = 0; i < list.size(); i++) {

            if (value.equalsIgnoreCase(String.valueOf(list.get(i)))) {

                return true;
            }
        }

        return false;
    }
}
