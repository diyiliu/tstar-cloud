package com.tiza.process.common.config;

import com.diyiliu.common.util.ConstantUtil;

/**
 * Description: EStarConstant
 * Author: DIYILIU
 * Update: 2017-08-07 14:45
 */

public final class EStarConstant extends ConstantUtil {
    public enum Kafka {
        ;
        public final static String TRACK_TOPIC = "trackTopic";
        public final static String FAULT_TOPIC = "faultTopic";

        public final static String EMAIL_TOPIC = "emailTopic";
        public final static String SMS_TOPIC = "smsTopic";
    }

    public enum FlowKey {
        ;
        // 报警数据
        public final static String VEHICLE_FAULT = "vehicleFault";
        // 极值数据
        public final static String VEHICLE_EXTREME = "vehicleExtreme";
        // 报警等级
        public final static String ALARM_LEVEL = "alarmLevel";
        // 实时状态
        public final static String REAL_MODE = "realMode";
    }

    public enum RealMode{
        ;
        // 登陆 登出
        public final static String IN_OUT = "inOut";
        // 开关机
        public final static String ON_OFF = "onOff";
        // 报警等级
        public final static String ALARM_LEVEL = "alarmLevel";
        // 充电状态
        public final static String TOP_OFF = "topOff";
    }


    public enum Location{
        ;
        public final static String GPS_TIME = "gpsTime";
        public final static String LOCATION_STATUS = "locationStatus";
        public final static String ORIGINAL_LNG = "originalLng";
        public final static String ORIGINAL_LAT = "originalLat";
        public final static String LNG = "lng";
        public final static String LAT = "lat";
        public final static String MILEAGE = "mileage";

        public final static String VEHICLE_ID = "vehicleId";
    }

    public enum Fault{
        ;
        public final static String FAULT_UNIT = "faultUnit";
        public final static String FAULT_VALUE = "faultValue";
        public final static String START_TIME = "startTime";
        public final static String END_TIME = "endTime";
        public final static String FAULT_NAME = "faultName";
        public final static String FAULT_DESC = "faultDesc";

        public final static String VEHICLE_ID = "vehicleId";
    }


    public enum SQL{
        ;
        public final static String SELECT_VEHICLE_INFO = "selectVehicleInfo";
        public final static String SELECT_VEHICLE_FAULT = "selectVehicleFault";
        public final static String SELECT_FAULT_CODE = "selectFaultCode";
        public final static String SELECT_ALARM_STRATEGY = "selectAlarmStrategy";
    }
}
