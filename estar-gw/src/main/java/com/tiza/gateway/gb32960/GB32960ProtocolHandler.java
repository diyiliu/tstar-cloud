package com.tiza.gateway.gb32960;

import cn.com.tiza.tstar.common.entity.TStarData;
import cn.com.tiza.tstar.gateway.handler.BaseUserDefinedHandler;
import com.diyiliu.common.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: GB32960ProtocolHandler
 * Author: Wangw
 * Update: 2017-09-05 11:41
 */

public class GB32960ProtocolHandler extends BaseUserDefinedHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public TStarData handleRecvMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {

        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, msgBody);

        byteBuf.readByte();
        byteBuf.readByte();
        int cmd = byteBuf.readUnsignedByte();
        int resp = byteBuf.readUnsignedByte();

        byte[] vinArray = new byte[17];
        byteBuf.readBytes(vinArray);
        String vin = new String(vinArray);

        TStarData tStarData = new TStarData();
        tStarData.setMsgBody(msgBody);
        tStarData.setCmdID(cmd);
        tStarData.setTerminalID(vin);
        tStarData.setTime(System.currentTimeMillis());

        // 加密标识
        byteBuf.readByte();
        // 数据单元长度
        int length = byteBuf.readUnsignedShort();
        byteBuf.readBytes(new byte[length]);
        // 校验位
        int last = byteBuf.readByte();

        byte[] content = new byte[msgBody.length - 3];
        System.arraycopy(msgBody, 2, content, 0, content.length);
        byte check = CommonUtil.getCheck(content);

        // 验证校验位
        if (last != check){
            logger.error("check code error!");
            // 数据错误
            doResponse(channelHandlerContext, tStarData, 0x02);

            return null;
        }
        logger.info("Receive: Terminal[{}] CMD[{}], Content[{}]...", vin, String.format("%02X", cmd), CommonUtil.bytesToStr(msgBody));

        // 需要应答
        if (resp == 0xFE){

            doResponse(channelHandlerContext, tStarData, 0x01);
        }

        return tStarData;
    }

    /**
     * 指令应答
     * @param ctx
     * @param tStarData
     * @param respCmd
     */
    private void doResponse(ChannelHandlerContext ctx, TStarData tStarData, int respCmd){
        int cmd = tStarData.getCmdID();

        ByteBuf buf;
        if (0x07 == cmd){
            buf = Unpooled.buffer(25);
            buf.writeByte(0x23);
            buf.writeByte(0x23);
            buf.writeByte(cmd);
            buf.writeByte(respCmd);
            // VIN
            buf.writeBytes(tStarData.getTerminalID().getBytes());
            // 不加密
            buf.writeByte(0x01);
            buf.writeShort(0);

            // 获取校验位
            byte[] content = new byte[22];
            buf.getBytes(2, content);
            int check = CommonUtil.getCheck(content);

            buf.writeByte(check);
        }else {
            buf = Unpooled.buffer(31);
            buf.writeByte(0x23);
            buf.writeByte(0x23);
            buf.writeByte(cmd);
            buf.writeByte(respCmd);
            // VIN
            buf.writeBytes(tStarData.getTerminalID().getBytes());
            // 不加密
            buf.writeByte(0x01);
            buf.writeShort(6);

            // 时间
            byte[] dateArray = new byte[6];
            System.arraycopy(tStarData.getMsgBody(), 24, dateArray, 0, 6);
            buf.writeBytes(dateArray);

            // 获取校验位
            byte[] content = new byte[28];
            buf.getBytes(2, content);
            int check = CommonUtil.getCheck(content);

            buf.writeByte(check);
        }

        TStarData respData = new TStarData();
        respData.setTerminalID(tStarData.getTerminalID());
        respData.setCmdID(tStarData.getCmdID());
        respData.setMsgBody(buf.array());
        respData.setTime(System.currentTimeMillis());

        logger.info("Response, Terminal[{}], Content[{}]...", respData.getTerminalID(), CommonUtil.bytesToStr(respData.getMsgBody()));
        ctx.channel().writeAndFlush(respData);
    }


    /**
     * 命令序号
     *
    private static AtomicLong msgSerial = new AtomicLong(0);
    private static int getMsgSerial() {
        Long serial = msgSerial.incrementAndGet();
        if (serial > 65535) {
            msgSerial.set(0);
            serial = msgSerial.incrementAndGet();
        }

        return serial.intValue();
    }
     **/
}
