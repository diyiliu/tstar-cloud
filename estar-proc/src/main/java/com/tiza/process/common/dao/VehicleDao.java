package com.tiza.process.common.dao;

import com.diyiliu.common.dao.BaseDao;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.model.VehicleInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Description: VehicleDao
 * Author: DIYILIU
 * Update: 2017-08-07 14:58
 */

@Component
public class VehicleDao extends BaseDao {

    public List<VehicleInfo> selectVehicleInfo() {
        String sql = EStarConstant.getSQL(EStarConstant.SQL.SELECT_VEHICLE_INFO);

        if (jdbcTemplate == null){
            logger.warn("尚未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<VehicleInfo>() {

            @Override
            public VehicleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                VehicleInfo vehicleInfo = new VehicleInfo();
                vehicleInfo.setId(rs.getInt("id"));
                vehicleInfo.setVin(rs.getString("vin"));
                vehicleInfo.setIccid(rs.getString("iccid"));
                vehicleInfo.setLicense(rs.getString("license"));

                return vehicleInfo;
            }
        });
    }
}
