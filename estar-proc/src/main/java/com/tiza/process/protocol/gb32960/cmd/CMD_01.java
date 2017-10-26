package com.tiza.process.protocol.gb32960.cmd;

import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.protocol.bean.GB32960Header;
import com.tiza.process.protocol.gb32960.GB32960DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 车辆登入
 * Description: CMD_01
 * Author: Wangw
 * Update: 2017-09-07 14:57
 */

@Service
public class CMD_01 extends GB32960DataProcess{

    public CMD_01() {
        this.cmd = 0x01;
    }

    @Override
    public void parse(byte[] content, Header header) {
        GB32960Header gb32960Header = (GB32960Header) header;
        RPTuple tuple = (RPTuple) gb32960Header.gettStarData();

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] dateBytes = new byte[6];
        buf.readBytes(dateBytes);
        Date date = CommonUtil.bytesToDate(dateBytes);
        tuple.setTime(date.getTime());

        int serial = buf.readUnsignedShort();

        byte[] iccidBytes = new byte[20];
        buf.readBytes(iccidBytes);
        String iccid = new String(iccidBytes);

        int count = buf.readByte();
        int length = buf.readByte();
        if (buf.readableBytes() < count * length){

            logger.warn("解析可充电储能系统字节长度不足!");
            return;
        }

        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++){

            byte[] codeBytes = new byte[length];
            buf.readBytes(codeBytes);
            String code = new String(codeBytes);

            codes.add(code);
        }

        // 记录车辆登入状态
        Map<String, String> context = tuple.getContext();
        Map modelMap = new HashMap();
        modelMap.put(EStarConstant.RealMode.IN_OUT, 1);
        context.put(EStarConstant.FlowKey.REAL_MODE, JacksonUtil.toJson(modelMap));
    }
}
