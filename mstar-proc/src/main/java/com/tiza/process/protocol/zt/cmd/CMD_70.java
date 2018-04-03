package com.tiza.process.protocol.zt.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.protocol.zt.ZTDataProcess;
import org.springframework.stereotype.Service;

/**
 * 终端心跳
 * Description: CMD_70
 * Author: DIYILIU
 * Update: 2018-04-02 09:04
 */

@Service
public class CMD_70 extends ZTDataProcess{

    public CMD_70() {
        this.cmd = 0x70;
    }

    @Override
    public void parse(byte[] content, Header header) {

        // 心跳
    }
}
