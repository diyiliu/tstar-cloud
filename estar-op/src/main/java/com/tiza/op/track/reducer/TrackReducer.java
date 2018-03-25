package com.tiza.op.track.reducer;

import com.tiza.op.entity.MileageRecord;
import com.tiza.op.model.Position;
import com.tiza.op.model.TrackKey;
import com.tiza.op.util.DateUtil;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Description: TrackReducer
 * Author: DIYILIU
 * Update: 2017-09-27 16:42
 */
public class TrackReducer extends Reducer<TrackKey, Position, MileageRecord, NullWritable> {
    private Date date;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        try {
            String datetime = context.getConfiguration().get("data_time");
            date = DateUtil.str2Date(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void reduce(TrackKey key, Iterable<Position> values, Context context) throws IOException, InterruptedException {
        List<Position> positions = new ArrayList();

        for (Iterator<Position> iterator = values.iterator(); iterator.hasNext(); ) {
            Position p = iterator.next();

            // 如果不手动new 对象, Reducer会复用对象得引用地址。
            /*
            Position position = new Position();
            position.setDateTime(p.getDateTime());
            position.setMileage(p.getMileage());
            positions.add(position);
            */

            try {
                positions.add((Position) p.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        // 按时间排序
        Collections.sort(positions);

        // 当日最小位置
        double minMileage = positions.get(0).getMileage();
        // 当日最大里程
        double maxMileage = positions.get(positions.size() - 1).getMileage();

        double dailyMileage = maxMileage - minMileage;
        logger.info("终端[{}], 最大里程;[{}], 最小里程:[{}], 当日里程[{}]。",
                key.getVehicleId(), maxMileage, minMileage, dailyMileage);

        MileageRecord record = new MileageRecord();
        record.setVehicleId(Long.parseLong(key.getVehicleId()));
        record.setDateTime(date);
        record.setMileage(dailyMileage);
        record.setTotalMileage(maxMileage);
        record.setCreateTime(new Date());

        context.write(record, NullWritable.get());
    }
}
