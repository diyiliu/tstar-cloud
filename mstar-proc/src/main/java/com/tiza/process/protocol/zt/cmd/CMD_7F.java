package com.tiza.process.protocol.zt.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.bean.ZTHeader;
import com.tiza.process.common.config.MStarConstant;
import com.tiza.process.common.model.FunctionInfo;
import com.tiza.process.common.model.Position;
import com.tiza.process.protocol.zt.ZTDataProcess;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作参数
 *
 * Description: CMD_7F
 * Author: DIYILIU
 * Update: 2018-04-02 09:07
 */

@Service
public class CMD_7F extends ZTDataProcess{

    public CMD_7F() {
        this.cmd = 0x7F;
    }

    @Override
    public void parse(byte[] content, Header header) {
        Position position = renderPosition(content);

        Map statusMap = new HashMap();
        statusMap.put(MStarConstant.Location.TERMINAL_STATUS, 1);
        position.setStatusMap(statusMap);

        // 状态位信息
        /*
        FunctionInfo functionInfo = getFunctionInfo(ztHeader.getTerminalId());
        if (functionInfo != null) {
            Map statusValues = parsePackage(position.getStatusBytes(), functionInfo.getStatusItems());
            statusMap.putAll(statusValues);
        }
        */

        toKafka((ZTHeader) header, position, null);
    }
}
