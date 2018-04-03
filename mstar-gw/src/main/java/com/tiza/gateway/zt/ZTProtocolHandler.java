package com.tiza.gateway.zt;

import cn.com.tiza.tstar.common.entity.TStarData;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.gateway.common.BaseProtocolHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Description: ZTProtocolHandler
 * Author: DIYILIU
 * Update: 2018-03-27 10:05
 */
public class ZTProtocolHandler extends BaseProtocolHandler {

    public ZTProtocolHandler() {

        // 需要返回0x30
        respCmds = new ArrayList() {
            {
                this.add(0x7B);
                this.add(0x7F);
            }
        };
    }

    @Override
    public TStarData handleRecvMessage(ChannelHandlerContext context, ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        if (!isValid(bytes)) {

            return null;
        }

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        // 标识头 + 长度
        buf.readBytes(new byte[3]);

        // 终端ID
        byte[] terminalBytes = new byte[5];
        buf.readBytes(terminalBytes);
        String terminalId = CommonUtil.parseSIM(terminalBytes);

        // 协议版本号
        buf.readByte();

        // 命令序号
        int cmdSerial = buf.readUnsignedShort();

        // 命令ID
        int cmd = buf.readByte();

        TStarData tStarData = new TStarData();
        tStarData.setTerminalID(terminalId);
        tStarData.setCmdID(cmd);
        tStarData.setCmdSerialNo(cmdSerial);
        tStarData.setMsgBody(bytes);
        tStarData.setTime(System.currentTimeMillis());

        logger.info("上行消息，终端[{}]指令[{}], 内容[{}]...", terminalId, CommonUtil.toHex(cmd), CommonUtil.bytesToStr(bytes));
        if (respCmds.contains(cmd)) {
            ByteBuf respBuf = Unpooled.buffer(4);
            respBuf.writeShort(cmdSerial);
            respBuf.writeByte(cmd);
            respBuf.writeByte(0);

            TStarData respData = new TStarData();
            respData.setTerminalID(terminalId);
            respData.setCmdSerialNo(cmdSerial);
            respData.setTime(System.currentTimeMillis());

            byte[] respMsg = createResp(Arrays.copyOf(bytes, bytes.length), respBuf.array(), 0x30, cmdSerial);
            respData.setCmdID(0x30);
            respData.setMsgBody(respMsg);
            context.channel().writeAndFlush(respData);

            logger.info("下行消息，终端[{}]指令[{}], 内容[{}]...", terminalId, CommonUtil.toHex(respData.getCmdID()), CommonUtil.bytesToStr(respData.getMsgBody()));
        }

        return tStarData;
    }


    /**
     * 生成回复指令内容
     *
     * @param recMsg  上行上行的指令内容
     * @param content 需要下行的指令内容
     * @param cmd     需要下行的命令ID
     * @param serial  下行的序列号
     * @return
     */
    public byte[] createResp(byte[] recMsg, byte[] content, int cmd, int serial) {
        int length = 14 + content.length;

        recMsg[0] = (byte) ((length >> 8) & 0xff);
        recMsg[1] = (byte) (length & 0xff);
        ByteBuf header = Unpooled.copiedBuffer(recMsg, 0, 9);

        ByteBuf remainder = Unpooled.buffer(3 + content.length);
        remainder.writeShort(serial);
        remainder.writeByte(cmd);
        remainder.writeBytes(content);

        // 校验位
        byte check = CommonUtil.getCheck(Unpooled.copiedBuffer(header, remainder).array());

        // 组合数据
        byte[] bytes = Unpooled.copiedBuffer(Unpooled.copiedBuffer(new byte[]{(byte) 0xFE}),
                header, remainder, Unpooled.copiedBuffer(new byte[]{check, (byte) 0xFD})).array();

        return bytes;
    }

    public boolean isValid(byte[] bytes) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        if (buf.readableBytes() < 14) {

            return false;
        }

        int start = buf.readUnsignedByte();
        if (start != 0xFE) {

            logger.error("封包标识符, 头[{}]错误!", CommonUtil.toHex(start));
            return false;
        }

        // 消息长度
        int length = buf.readUnsignedShort();

        if (buf.readableBytes() < length - 3) {

            logger.error("数据内容不完整", CommonUtil.bytesToStr(bytes));
            return false;
        }

        // 不验证校验位
        byte[] content = new byte[length - 4];
        buf.readBytes(content);

        int end = buf.readUnsignedByte();
        if (end != 0xFD) {

            logger.error("封包标识符, 尾[{}]错误!", CommonUtil.toHex(end));
            return false;
        }

        return true;
    }
}
