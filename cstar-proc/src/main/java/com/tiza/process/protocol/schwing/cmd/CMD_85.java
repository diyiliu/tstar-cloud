package com.tiza.process.protocol.schwing.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.bean.M2Header;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.model.Status;
import com.tiza.process.protocol.schwing.SchwingDataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_85
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class CMD_85 extends SchwingDataProcess {

    public CMD_85() {
        this.cmd = 0x85;
    }

    @Override
    public void parse(byte[] content, Header header) {
        Position position = renderPosition(content);
        Status status = renderStatus(position.getStatus());

        // 车辆在线
        status.setOnOff(1);

        toKafka((M2Header) header, position, status, null);
    }
}
