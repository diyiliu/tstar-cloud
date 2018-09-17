package com.tiza.gateway.gb32960;

import cn.com.tiza.tstar.common.entity.TStarData;
import cn.com.tiza.tstar.gateway.handler.BaseUserDefinedHandler;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Description: GB32960ProtocolHandler
 * Author: Wangw
 * Update: 2017-09-05 11:41
 */

public class GB32960ProtocolHandler extends BaseUserDefinedHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public TStarData handleRecvMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgBody);

        ByteBuf buf = Unpooled.copiedBuffer(msgBody);

        // 协议头
        buf.readShort();
        // 命令标识
        int cmd = buf.readUnsignedByte();
        // 应答标识
        int resp = buf.readUnsignedByte();

        // VIN码
        byte[] vinArray = new byte[17];
        buf.readBytes(vinArray);
        String vin = new String(vinArray);

        // 加密方式
        buf.readByte();
        // 数据单元长度
        int length = buf.readUnsignedShort();

        TStarData tStarData = new TStarData();
        tStarData.setMsgBody(msgBody);
        tStarData.setCmdID(cmd);
        tStarData.setTerminalID(vin);
        tStarData.setTime(System.currentTimeMillis());

        logger.debug("上行: 终端[{}] 指令[{}], 内容[{}]...", vin, String.format("%02X", cmd), CommonUtil.bytesToStr(msgBody));
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
            byte[] msgBody = tStarData.getMsgBody();
            if (length < 6) {
                logger.error("数据[{}]长度异常, 无法完成应答!", CommonUtil.bytesToStr(msgBody));
                return;
            }

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
            /*  车辆数据 改为 平台数据(原来的数据改为系统时间)
            byte[] dateArray = new byte[6];
            System.arraycopy(msgBody, 24, dateArray, 0, 6);
            */
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

        logger.debug("下行, 终端[{}], 指令[{}], 内容[{}]...", respData.getTerminalID(), String.format("%02X", respData.getCmdID()), CommonUtil.bytesToStr(respData.getMsgBody()));
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
