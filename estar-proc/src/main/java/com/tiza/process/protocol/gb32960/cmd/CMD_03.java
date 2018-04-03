package com.tiza.process.protocol.gb32960.cmd;

import com.diyiliu.common.model.Header;
import com.tiza.process.common.bean.GB32960Header;
import com.tiza.process.protocol.gb32960.GB32960DataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_03
 * Author: Wangw
 * Update: 2017-09-07 14:57
 */

@Service
public class CMD_03 extends GB32960DataProcess{

    public CMD_03() {
        this.cmd = 0x03;
    }

    @Override
    public void parse(byte[] content, Header header) {
        GB32960DataProcess dataProcess = (GB32960DataProcess) cmdCacheProvider.get(0x02);
        dataProcess.parse(content, header);
    }
}
