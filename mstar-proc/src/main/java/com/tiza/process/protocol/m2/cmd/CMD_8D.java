package com.tiza.process.protocol.m2.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.model.FunctionInfo;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.protocol.m2.M2DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_8D
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_8D extends M2DataProcess {

    public CMD_8D() {
        this.cmd = 0x8D;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;
        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] bytes = new byte[22];
        buf.readBytes(bytes);

        // 00H: 开机信息; 01H: 关机信息
        byte power = buf.readByte();

        // 0:离线; 1:在线(对应数据库字段TERMINALSTATUS)
        int onOff = power == 0? 1: 0;

        Position position = renderPosition(bytes);
        Map statusMap = new HashMap();
        statusMap.put(MStarConstant.Location.TERMINAL_STATUS, onOff);
        position.setStatusMap(statusMap);

        // 状态位信息
        FunctionInfo functionInfo = getFunctionInfo(m2Header.getTerminalId());
        if (functionInfo != null) {
            Map statusValues = parsePackage(position.getStatusBytes(), functionInfo.getStatusItems());
            statusMap.putAll(statusValues);
        }

        toKafka((M2Header) header, position, null);
    }
}
