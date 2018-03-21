package com.tiza.process.protocol.gb32960.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.protocol.gb32960.GB32960DataProcess;
import org.springframework.stereotype.Service;

/**
 * 平台登出
 *
 * Description: CMD_06
 * Author: Wangw
 * Update: 2017-09-07 14:57
 */

@Service
public class CMD_06 extends GB32960DataProcess{

    public CMD_06() {
        this.cmd = 0x06;
    }

    @Override
    public void parse(byte[] content, Header header) {



    }
}
