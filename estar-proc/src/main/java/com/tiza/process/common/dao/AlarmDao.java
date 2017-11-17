package com.tiza.process.common.dao;

import com.diyiliu.common.dao.BaseDao;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.model.AlarmNotice;
import com.tiza.process.common.model.AlarmStrategy;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Description: AlarmDao
 * Author: DIYILIU
 * Update: 2017-10-25 09:37
 */
@Component
public class AlarmDao extends BaseDao{


    public List<AlarmStrategy> selectAlarmStrategy(){
        String sql = EStarConstant.getSQL(EStarConstant.SQL.SELECT_ALARM_STRATEGY);

        if (jdbcTemplate == null){
            logger.warn("尚未装载数据源，无法连接数据库!");
            return null;
        }

        return jdbcTemplate.query(sql, new RowMapper<AlarmStrategy>() {
            @Override
            public AlarmStrategy mapRow(ResultSet rs, int rowNum) throws SQLException {
                AlarmStrategy alarmStrategy = new AlarmStrategy();
                alarmStrategy.setVehicleId(rs.getLong("vehicleid"));
                alarmStrategy.setId(rs.getInt("alarmid"));
                alarmStrategy.setName(rs.getString("name"));
                alarmStrategy.setAlarmLevel(rs.getInt("alarmlevel"));
                alarmStrategy.setMaxVoltage(rs.getDouble("batterymaxvoltage"));
                alarmStrategy.setMinVoltage(rs.getDouble("batteryminvoltage"));
                alarmStrategy.setMaxTemperature(rs.getInt("batterymaxtemp"));
                alarmStrategy.setMinTemperature(rs.getInt("batterymintemp"));

                AlarmNotice notice = new AlarmNotice();
                notice.setAlarmId(alarmStrategy.getId());
                notice.setUserId(rs.getLong("userid"));
                notice.setSms(rs.getInt("sms"));
                notice.setEmail(rs.getInt("email"));
                notice.setSite(rs.getInt("site"));
                notice.setUsername(rs.getString("fullname"));
                notice.setMobile(rs.getString("mobile"));
                notice.setMailAddress(rs.getString("mailaddress"));

                alarmStrategy.setAlarmNotice(notice);

                return alarmStrategy;
            }
        });
    }
}
