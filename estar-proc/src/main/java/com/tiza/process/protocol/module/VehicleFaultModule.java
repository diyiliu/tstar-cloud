package com.tiza.process.protocol.module;

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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
            List hisList = (List) vehicleFaultCache.get(String.valueOf(vehicleInfo.getId()));

            dealFault(vehicleInfo, hisList, faultMap);
        }

        return rpTuple;
    }

    @Override
    public void init() throws Exception {

    }


    public void dealFault(VehicleInfo vehicleInfo, List<VehicleFault> hisList, Map<String, List> faultMap) {
        Date current = new Date(vehicleInfo.getDateTime());
        Map<String, List<VehicleFault>> unitMap = toUnitMap(hisList);

        // 解除故障报警
        if (CollectionUtils.isNotEmpty(hisList)) {
            Set hisKeys = unitMap.keySet();
            Set currKeys = faultMap.keySet();

            Collection subKeys = CollectionUtils.subtract(hisKeys, currKeys);
            for (Iterator iterator = subKeys.iterator(); iterator.hasNext();){
                String key = (String) iterator.next();

                List<VehicleFault> subList = unitMap.get(key);
                for (VehicleFault fault : subList) {
                    if (fault.isOver()) {
                        continue;
                    }

                    if (current.after(fault.getStartTime())) {
                        fault.setEndTime(current);
                        fault.setOver(true);
                        toUpdate(fault);

                    }
                }
            }
        }

        // 产生故障报警
        if (MapUtils.isNotEmpty(faultMap)) {
            Map<String, VehicleFault> hisMap = toHisMap(hisList);

            for (Iterator iterator = faultMap.keySet().iterator(); iterator.hasNext(); ) {
                String faultUnit = String.valueOf(iterator.next());
                List list = faultMap.get(faultUnit);

                for (int i = 0; i < list.size(); i++) {
                    String faultValue = String.valueOf(list.get(i));

                    String key = faultUnit + "_" + faultValue;
                    if (!faultCodeCache.containsKey(key)) {
                        continue;
                    }

                    boolean exist = false;
                    if (hisMap.containsKey(key)) {
                        exist = true;
                        VehicleFault fault = hisMap.get(key);

                        // 更新报警
                        if (fault.isOver() &&
                                current.after(fault.getEndTime())) {

                            fault.setStartTime(current);
                            fault.setOver(false);
                            fault.setEndTime(null);
                            toUpdate(fault);

                        }
                    }

                    if (!exist) {
                        VehicleFault vehicleFault = new VehicleFault();
                        vehicleFault.setVehicleId(vehicleInfo.getId());
                        vehicleFault.setFaultUnit(faultUnit);
                        vehicleFault.setFaultValue(String.valueOf(faultValue));
                        vehicleFault.setStartTime(current);

                        // 新增报警
                        toCreate(vehicleFault);

                        String vehicleId = String.valueOf(vehicleInfo.getId());
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

                // 解除故障报警
                if (unitMap.containsKey(faultUnit)) {
                    List<VehicleFault> l = unitMap.get(faultUnit);
                    Collection<VehicleFault> sumList = CollectionUtils.subtract(l, toFaultList(faultUnit, list));
                    for (VehicleFault fault : sumList) {

                        // 解除报警
                        if (current.after(fault.getStartTime())) {

                            fault.setEndTime(current);
                            fault.setOver(true);
                            toUpdate(fault);
                        }
                    }
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


    private Map<String, VehicleFault> toHisMap(List<VehicleFault> list) {
        Map<String, VehicleFault> map = new HashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            for (VehicleFault fault : list) {
                String key = fault.getFaultUnit() + "_" + fault.getFaultValue();
                map.put(key, fault);
            }
        }

        return map;
    }


    private Map<String, List<VehicleFault>> toUnitMap(List<VehicleFault> list) {
        Map<String, List<VehicleFault>> map = new HashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            for (VehicleFault fault : list) {
                String unit = fault.getFaultUnit();
                if (map.containsKey(unit)) {

                    List l = map.get(unit);
                    l.add(fault);
                } else {
                    List l = new ArrayList();
                    l.add(fault);

                    map.put(unit, l);
                }
            }
        }

        return map;
    }

    /**
     * 构建故障单元
     *
     * @param faultUnit
     * @param list
     * @return
     */
    private List<VehicleFault> toFaultList(String faultUnit, List list){
        List<VehicleFault> faultList = new ArrayList();
        if (CollectionUtils.isNotEmpty(list)){
            for (int i = 0 ; i < list.size(); i++){

                VehicleFault fault = new VehicleFault();
                fault.setFaultUnit(faultUnit);
                fault.setFaultValue(String.valueOf(list.get(i)));

                faultList.add(fault);
            }
        }

        return faultList;
    }
}
