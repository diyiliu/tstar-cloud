package com.tiza.process.common.dao;

import com.diyiliu.common.dao.BaseDao;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.model.FaultCode;
import com.tiza.process.common.model.VehicleFault;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 车辆故障
 * Description: FaultDao
 * Author: DIYILIU
 * Update: 2017-10-12 14:33
 */

@Component
public class FaultDao extends BaseDao{

    public List<VehicleFault> selectVehicleFault(){
        String sql = EStarConstant.getSQL(EStarConstant.SQL.SELECT_VEHICLE_FAULT);

        if (jdbcTemplate == null){
            logger.warn("尚未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<VehicleFault>() {
            @Override
            public VehicleFault mapRow(ResultSet rs, int rowNum) throws SQLException {
                VehicleFault fault = new VehicleFault();
                fault.setVehicleId(rs.getLong("vehicleid"));
                fault.setFaultUnit(rs.getString("faultunit"));
                fault.setFaultValue(rs.getString("faultvalue"));
                fault.setStartTime(new Date(rs.getTimestamp("starttime").getTime()));

                fault.setOver(false);
                if (rs.getTimestamp("endtime") != null){

                    fault.setEndTime(new Date(rs.getTimestamp("endtime").getTime()));
                    fault.setOver(true);
                }

                return fault;
            }
        });
    }


    public List<FaultCode> selectFaultCode(){
        String sql = EStarConstant.getSQL(EStarConstant.SQL.SELECT_FAULT_CODE);

        if (jdbcTemplate == null){
            logger.warn("尚未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<FaultCode>() {
            @Override
            public FaultCode mapRow(ResultSet rs, int rowNum) throws SQLException {

                FaultCode code = new FaultCode();
                code.setFactoryId(rs.getLong("factoryid"));
                code.setFaultUnit(rs.getString("faultunit"));
                code.setFaultValue(rs.getString("faultvalue"));
                code.setName(rs.getString("name"));
                code.setDesc(rs.getString("description"));

                return code;
            }
        });
    }
}
