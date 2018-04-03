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
 * 终端登录
 *
 * Description: CMD_7B
 * Author: DIYILIU
 * Update: 2018-04-02 09:04
 */

@Service
public class CMD_7B extends ZTDataProcess{

    public CMD_7B() {
        this.cmd = 0x7B;
    }

    @Override
    public void parse(byte[] content, Header header) {
        Position position = renderPosition(content);

        Map statusMap = new HashMap();
        statusMap.put(MStarConstant.Location.TERMINAL_STATUS, 1);
        position.setStatusMap(statusMap);

        toKafka((ZTHeader) header, position, null);
    }
}
