package com.tiza.process.protocol.m2.cmd;

import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.protocol.m2.M2DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description: CMD_88
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_88 extends M2DataProcess {

    public CMD_88() {
        this.cmd = 0x88;
    }

    @Override
    public void parse(byte[] content, Header header) {
        ByteBuf buf = Unpooled.copiedBuffer(content);

        // 年月日
        byte[] ymd = new byte[3];
        buf.readBytes(ymd);

        int count = buf.readByte();
        if (buf.readableBytes() < count * 3 * 2){
            logger.error("解析指令[{}]， 数据异常...", cmd);
            return;
        }

        // 起始时间
        Date origin = new Date(0);
        List<Date> dateList = new ArrayList<>();

        // 时分秒
        byte[] hms = new byte[3];
        // 开始时间、结束时间
        for (int i = 0; i < count * 2; i++){
            buf.readBytes(hms);

            // 拼接 年月日 + 时分秒 = 完整日期
            ByteBuf dateBuf = Unpooled.copiedBuffer(ymd, hms);
            Date date = CommonUtil.bytesToDate(origin, dateBuf.array());
            dateList.add(date);
        }

        toKafka((M2Header) header, dateList);
    }
}
