package com.tiza.process.protocol.zt;

import com.diyiliu.common.model.Header;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.process.common.model.Position;
import com.tiza.process.common.bean.ZTHeader;
import com.tiza.process.protocol.m2.M2DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Date;

/**
 * Description: ZTDataProcess
 * Author: DIYILIU
 * Update: 2018-04-02 09:00
 */
public class ZTDataProcess extends M2DataProcess {

    @Override
    public Header dealHeader(byte[] bytes) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        int start = buf.readUnsignedByte();

        int length = buf.readUnsignedShort();

        byte[] termi = new byte[5];
        buf.readBytes(termi);
        String terminalId = CommonUtil.parseSIM(termi);

        int version = buf.readByte();

        int serial = buf.readUnsignedShort();

        int cmd = buf.readUnsignedByte();

        byte[] content = new byte[buf.readableBytes() - 2];
        buf.readBytes(content);

        int check = buf.readByte();
        int end = buf.readUnsignedByte();

        return new ZTHeader(cmd, length, terminalId, version, serial,
                content, check, start, end);
    }

    @Override
    protected Position renderPosition(byte[] bytes) {
        if (bytes.length < 20) {
            logger.error("长度不足，无法获取位置信息！");
            return null;
        }

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        long lat = buf.readUnsignedInt();
        long lng = buf.readUnsignedInt();
        int speed = buf.readUnsignedByte();
        int direction = buf.readUnsignedByte();

        byte[] statusBytes = new byte[4];
        buf.readBytes(statusBytes);

        byte[] dateBytes = new byte[6];
        buf.readBytes(dateBytes);
        Date dateTime = CommonUtil.bytesToDate(dateBytes);


        return new Position(lng, lat, speed, direction, statusBytes, dateTime);
    }
}
