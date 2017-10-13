package com.tiza.process.common.dao;

import com.diyiliu.common.dao.BaseDao;
import com.diyiliu.common.model.Area;
import com.diyiliu.common.model.Circle;
import com.diyiliu.common.model.Point;
import com.diyiliu.common.model.Region;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.model.InOutRecord;
import com.tiza.process.common.model.Storehouse;
import com.tiza.process.common.model.VehicleInfo;
import com.tiza.process.common.model.VehicleStorehouse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Description: VehicleDao
 * Author: DIYILIU
 * Update: 2017-08-07 14:58
 */

@Component
public class VehicleDao extends BaseDao {

    public List<VehicleInfo> selectVehicleInfo() {
        String sql = MStarConstant.getSQL(MStarConstant.SQL.SELECT_VEHICLE_INFO);

        if (jdbcTemplate == null) {
            logger.warn("未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<VehicleInfo>() {
            @Override
            public VehicleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                VehicleInfo vehicleInfo = new VehicleInfo();
                vehicleInfo.setId(rs.getInt("id"));
                vehicleInfo.setTerminalId(rs.getLong("terminalid"));
                vehicleInfo.setDeviceId(rs.getString("deviceid"));
                vehicleInfo.setTerminalNo(rs.getString("terminalno"));
                vehicleInfo.setSim(rs.getString("simno"));
                vehicleInfo.setProtocolType(rs.getString("protocoltype"));
                vehicleInfo.setSoftVersion(rs.getString("versioncode"));

                return vehicleInfo;
            }
        });
    }

    public List<VehicleStorehouse> selectVehicleStorehouse() {
        String sql = MStarConstant.getSQL(MStarConstant.SQL.SELECT_VEHICLE_STOREHOUSE);

        if (jdbcTemplate == null) {
            logger.warn("未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<VehicleStorehouse>() {
            @Override
            public VehicleStorehouse mapRow(ResultSet rs, int rowNum) throws SQLException {
                VehicleStorehouse  vehicleStorehouse = new VehicleStorehouse();
                vehicleStorehouse.setVehicleId(rs.getLong("vehicleid"));

                Storehouse storehouse = new Storehouse();
                storehouse.setId(rs.getInt("id"));
                storehouse.setRate(rs.getInt("uploadminute"));

                Clob content = (Clob) rs.getObject("fencegeoinfo");
                String str = content.getSubString(1, (int) content.length());

                int shape = rs.getInt("fencesharp");
                Area area = null;
                try {
                    switch (shape) {

                        // 圆形
                        case 1:
                            List<Circle> list = JacksonUtil.toList(str, Circle.class);
                            area = list.get(0);
                            break;
                        case 2:
                            List<Point> points = JacksonUtil.toList(str, Point.class);
                            area = new Region(points.toArray(new Point[points.size()]));
                            break;
                        default:
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                storehouse.setArea(area);
                vehicleStorehouse.setStorehouse(storehouse);

                return vehicleStorehouse;
            }
        });
    }

    public List<InOutRecord> selectInOutRecord(){
        String sql = MStarConstant.getSQL(MStarConstant.SQL.SELECT_VEHICLE_OUT_IN);

        if (jdbcTemplate == null) {
            logger.warn("未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<InOutRecord>() {
            @Override
            public InOutRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                InOutRecord record = new InOutRecord();
                record.setVehicleId(rs.getInt("vehicleid"));
                record.setStorehouseId(rs.getInt("unitid"));
                record.setGpsTime(new Date(rs.getTimestamp("gpstime").getTime()));

                return record;
            }
        });
    }
}
