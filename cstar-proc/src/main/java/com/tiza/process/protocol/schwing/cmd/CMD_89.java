package com.tiza.process.protocol.schwing.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.Status;
import com.tiza.process.protocol.schwing.SchwingDataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_89
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_89 extends SchwingDataProcess {

    public CMD_89() {
        this.cmd = 0x89;
    }

    @Override
    public void parse(byte[] content, Header header) {
        Position position = renderPosition(content);
        Status status = renderStatus(position.getStatus());
        // 车辆休眠，视为离线（status.onOff 默认为0）

        toKafka((M2Header) header, position, status, null);
    }
}
