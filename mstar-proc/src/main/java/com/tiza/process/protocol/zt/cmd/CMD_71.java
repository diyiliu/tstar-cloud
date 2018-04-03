package com.tiza.process.protocol.zt.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.protocol.zt.ZTDataProcess;
import org.springframework.stereotype.Service;

/**
 * 终端命令应答
 * Description: CMD_71
 * Author: DIYILIU
 * Update: 2018-04-02 09:04
 */

@Service
public class CMD_71 extends ZTDataProcess{

    public CMD_71() {
        this.cmd = 0x71;
    }

    @Override
    public void parse(byte[] content, Header header) {


    }
}
