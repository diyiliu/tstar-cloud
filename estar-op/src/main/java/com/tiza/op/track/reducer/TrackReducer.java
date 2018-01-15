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
import java.util.Date;
import java.util.Iterator;

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

        // 当日最大里程
        double maxMileage = 0;
        // 当日最小里程
        double minMileage = 0;

        int i = 0;
        for (Iterator iterator = values.iterator(); iterator.hasNext();){
            Position position = (Position) iterator.next();

            double mileage = position.getMileage();
            if (mileage < 0){
                continue;
            }

            if (0 == i){
                maxMileage = mileage;
                minMileage = mileage;
            }else {
                if (mileage > maxMileage){
                    maxMileage = mileage;
                }

                if (mileage < minMileage){

                    minMileage = mileage;
                }
            }
            i++;
        }
        logger.info("最大里程;[{}], 最小里程:[{}]。", maxMileage, minMileage);
        double dailyMileage = maxMileage - minMileage;

        MileageRecord record = new MileageRecord();
        record.setVehicleId(Long.parseLong(key.getVehicleId()));
        record.setDateTime(date);
        record.setMileage(dailyMileage);
        record.setTotalMileage(maxMileage);
        record.setCreateTime(new Date());

        context.write(record, NullWritable.get());
    }
}
