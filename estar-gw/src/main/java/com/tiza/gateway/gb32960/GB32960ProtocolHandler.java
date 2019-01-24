package com.tiza.gateway.gb32960;

import cn.com.tiza.tstar.common.entity.TStarData;
import cn.com.tiza.tstar.gateway.handler.BaseUserDefinedHandler;
import com.diyiliu.common.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * Description: GB32960ProtocolHandler
 * Author: Wangw
 * Update: 2017-09-05 11:41
 */

public class GB32960ProtocolHandler extends BaseUserDefinedHandler {

    public TStarData handleRecvMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, msgBody);

        // 协议头
        byteBuf.readShort();
        // 命令标识
        int cmd = byteBuf.readUnsignedByte();
        // 应答标识
        int resp = byteBuf.readUnsignedByte();

        // VIN码
        byte[] vinArray = new byte[17];
        byteBuf.readBytes(vinArray);
        String vin = new String(vinArray);

        // 加密方式
        byteBuf.readByte();
        // 数据单元长度
        int length = byteBuf.readUnsignedShort();

        TStarData tStarData = new TStarData();
        tStarData.setMsgBody(msgBody);
        tStarData.setCmdID(cmd);
        tStarData.setTerminalID(vin);
        tStarData.setTime(System.currentTimeMillis());

        // 需要应答
        if (resp == 0xFE) {

            doResponse(channelHandlerContext, tStarData, length);
        }

        return tStarData;
    }

    /**
     * 指令应答
     *
     * @param ctx
     * @param tStarData
     * @param length
     */
    private void doResponse(ChannelHandlerContext ctx, TStarData tStarData, int length) {
        int cmd = tStarData.getCmdID();
        int respCmd = 0x01;

        ByteBuf buf;
        if (length == 0) {
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
        } else {
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
            byte[] dateArray = CommonUtil.dateToBytes(new Date());
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
        ctx.channel().writeAndFlush(respData);
    }
}
