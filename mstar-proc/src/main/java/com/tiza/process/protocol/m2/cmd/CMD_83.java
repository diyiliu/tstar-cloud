package com.tiza.process.protocol.m2.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.model.FunctionInfo;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.protocol.m2.M2DataProcess;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_83
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_83 extends M2DataProcess {

    public CMD_83() {
        this.cmd = 0x83;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;
        Position position = renderPosition(content);

        Map statusMap = new HashMap();
        statusMap.put(MStarConstant.Location.TERMINAL_STATUS, 1);
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
