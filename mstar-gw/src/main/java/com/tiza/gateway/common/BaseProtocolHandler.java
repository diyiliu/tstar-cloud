package com.tiza.gateway.common;

import cn.com.tiza.tstar.common.entity.TStarData;
import cn.com.tiza.tstar.gateway.entity.CommandData;
import cn.com.tiza.tstar.gateway.handler.BaseUserDefinedHandler;
import com.diyiliu.common.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: BaseProtocolHandler
 * Author: DIYILIU
 * Update: 2018-03-28 09:45
 */
public class BaseProtocolHandler extends BaseUserDefinedHandler {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected List respCmds = new ArrayList();

    /**
     * 命令序号
     **/
    private AtomicLong msgSerial = new AtomicLong(0);

    public int getMsgSerial() {
        Long serial = msgSerial.incrementAndGet();
        if (serial > 65535) {
            msgSerial.set(0);
            serial = msgSerial.incrementAndGet();
        }

        return serial.intValue();
    }

    @Override
    public TStarData handleRecvMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        return null;
    }


    @Override
    public void commandReceived(ChannelHandlerContext ctx, CommandData cmd) {

        logger.debug("下行消息，终端[{}]指令[{}], 内容[{}]...", cmd.getTerminalID(), CommonUtil.toHex(cmd.getCmdID()), CommonUtil.bytesToStr(cmd.getMsgBody()));
    }
}
