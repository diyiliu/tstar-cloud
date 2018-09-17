package com.tiza.op.track.reducer;

import com.tiza.op.entity.MileageRecord;
import com.tiza.op.model.Position;
import com.tiza.op.model.TrackKey;
import com.tiza.op.util.DateUtil;
import com.tiza.op.util.JacksonUtil;
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
    protected void setup(Context context) {
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
            Position position = new Position();
            position.setDateTime(p.getDateTime());
            position.setMileage(p.getMileage());
            positions.add(position);

            positions.add(position);
        }

        // 按时间排序
        Collections.sort(positions);

        // 数据过滤
        List<Position> list = dataFilter(positions);
        if (list.size() < 1) {

            return;
        }

        // 当日最小位置
        double minMileage = list.get(0).getMileage();
        // 当日最大里程
        double maxMileage = list.get(list.size() - 1).getMileage();

        double dailyMileage = maxMileage - minMileage;
        if (dailyMileage < 0 || dailyMileage > 1000) {
            logger.info("错误数据[车辆: {}, 数据: {}]", key.getVehicleId(), JacksonUtil.toJson(list));
            return;
        }

        MileageRecord record = new MileageRecord();
        record.setVehicleId(Long.parseLong(key.getVehicleId()));
        record.setDateTime(date);
        record.setMileage(dailyMileage);
        record.setTotalMileage(maxMileage);
        record.setCreateTime(new Date());

        logger.info(record.toString());
        context.write(record, NullWritable.get());
    }


    /**
     * 过滤垃圾数据
     *
     * @param list
     * @return
     */
    public List<Position> dataFilter(List<Position> list) {
        if (list.size() < 3) {

            return list;
        }

        List<Position> positions = new ArrayList();
        int length = list.size();
        for (int i = 0; i < length; i++) {
            Position p = list.get(i);

            double mile1;
            double mile2;
            // 中间位置
            if (i > 0 && i + 1 < length) {
                Position np = list.get(i + 1);
                mile1 = np.getMileage() - p.getMileage();

                Position lp = list.get(i - 1);
                mile2 = p.getMileage() - lp.getMileage();
            } else {
                // 首数据
                if (i == 0) {
                    Position np = list.get(i + 1);
                    mile1 = np.getMileage() - p.getMileage();

                    Position nnp = list.get(i + 2);
                    mile2 = nnp.getMileage() - p.getMileage();
                } else {
                    // 末尾数据
                    Position lp = list.get(i - 1);
                    mile1 = p.getMileage() - lp.getMileage();

                    Position llp = list.get(i - 2);
                    mile2 = p.getMileage() - llp.getMileage();
                }
            }

            if (isValid(mile1) || isValid(mile2)) {
                positions.add(p);
            }
        }

        return positions;
    }

    public boolean isValid(double mile) {

        if (mile < 0 || mile > 100) {

            return false;
        }

        return true;
    }
}
