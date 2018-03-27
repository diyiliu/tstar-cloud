package com.tiza.op;

import cn.com.tiza.tstar.op.client.BaseJob;
import com.tiza.op.entity.MileageRecord;
import com.tiza.op.model.GroupingComparator;
import com.tiza.op.model.Position;
import com.tiza.op.model.TrackKey;
import com.tiza.op.track.mapper.TrackMapper;
import com.tiza.op.track.reducer.TrackReducer;
import com.tiza.op.util.DateUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.util.Date;

/**
 * Description: Main
 * Author: DIYILIU
 * Update: 2017-09-26 10:38
 */
public class Main extends BaseJob {

    public static void main(String[] args) throws Exception {
        Job job = new Main().getJob();
        FileInputFormat.addInputPath(job, new Path(args[0]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    @Override
    public Job getJob() throws Exception {
        job.setJobName("trackMileage");
        if (data_time == null) {
            data_time = DateUtil.date2Str(new Date());
        }
        job.getConfiguration().set("data_time", data_time);

        String table = "ALY_MILEAGE" + data_time.substring(0, 6);

        job.setJarByClass(Main.class);

        job.setGroupingComparatorClass(GroupingComparator.class);
        job.setMapperClass(TrackMapper.class);
        job.setMapOutputKeyClass(TrackKey.class);
        job.setMapOutputValueClass(Position.class);
        job.setInputFormatClass(TextInputFormat.class);

        job.setReducerClass(TrackReducer.class);
        job.setOutputKeyClass(MileageRecord.class);
        job.setOutputValueClass(NullWritable.class);
        job.setOutputFormatClass(DBOutputFormat.class);
        job.setNumReduceTasks(4);

        DBOutputFormat.setOutput(job, table, new String[]{"VEHICLEID", "DAY", "ODO", "DAYMILEAGE", "CREATETIME"});
        return job;
    }
}
