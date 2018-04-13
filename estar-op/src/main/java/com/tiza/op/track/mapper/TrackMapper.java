package com.tiza.op.track.mapper;

import com.tiza.op.model.Position;
import com.tiza.op.model.TrackKey;
import com.tiza.op.util.DBUtil;
import com.tiza.op.util.DateUtil;
import com.tiza.op.util.JacksonUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Description: TrackMapper
 * Author: DIYILIU
 * Update: 2017-09-27 14:31
 */
public class TrackMapper extends Mapper<LongWritable, Text, TrackKey, Position> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 车辆集合
    private Set<String> vehicleSet = new HashSet();

    @Override
    protected void setup(Context context) {
        Configuration conf = context.getConfiguration();
        conf.addResource("op-core.xml");
        conf.addResource("track.xml");

        try {
            refreshVehicle(conf);
        } catch (Exception e) {
            logger.error("TrackMapper查询车辆错误！");
            e.printStackTrace();
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Map map = JacksonUtil.toObject(value.toString(), HashMap.class);

        String vehicleId = String.valueOf(map.get("vehicleId"));
        if (!vehicleSet.contains(vehicleId)) {

            return;
        }

        if (map.containsKey("GpsTime") &&
                map.containsKey("ODOStatus")) {

            int odoStatus = (int) map.get("ODOStatus");
            if (odoStatus == 1 && map.containsKey("ODO")) {
                Date date = DateUtil.stringToDate((String) map.get("GpsTime"));
                long datetime = date.getTime();
                double mileage = (Double) map.get("ODO");

                if (mileage > 0) {
                    Position position = new Position(datetime, mileage);
                    TrackKey trackKey = new TrackKey(vehicleId, datetime);
                    context.write(trackKey, position);
                }
            }
        }
    }

    /**
     * 获取车辆集合
     *
     * @param configuration
     */
    private void refreshVehicle(Configuration configuration) {

        String sql = "SELECT t.id FROM bs_vehicle t";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DBUtil.getConnection(configuration);

            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {

                vehicleSet.add(String.valueOf(resultSet.getLong("id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(resultSet);
            DBUtil.close(statement);
            DBUtil.close(connection);
        }
    }
}
