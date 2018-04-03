package com.tiza.process.protocol.gb32960.cmd;

import cn.com.tiza.tstar.common.process.RPTuple;
import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.bean.GB32960Header;
import com.tiza.process.protocol.gb32960.GB32960DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 车辆登出
 * Description: CMD_04
 * Author: Wangw
 * Update: 2017-09-07 14:57
 */

@Service
public class CMD_04 extends GB32960DataProcess{

    public CMD_04() {
        this.cmd = 0x04;
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

        // 记录车辆登出状态
        Map<String, String> context = tuple.getContext();
        Map modelMap = new HashMap();
        modelMap.put(EStarConstant.RealMode.IN_OUT, 1);
        context.put(EStarConstant.FlowKey.REAL_MODE, JacksonUtil.toJson(modelMap));
    }
}
