package com.tiza.gateway.gb32960;

import cn.com.tiza.tstar.gateway.codec.CustomDecoder;
import com.diyiliu.common.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Description: GB32960Decoder
 * Author: Wangw
 * Update: 2017-09-05 10:31
 */

public class GB32960Decoder extends CustomDecoder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {

        if (buf.readableBytes() < 25){

            return;
        }

        buf.markReaderIndex();

        byte header1 = buf.readByte();
        byte header2 = buf.readByte();

        // 头标识
        if (header1 != 0x23 || header2 != 0x23){

            logger.error("协议头校验失败，断开连接!");
            ctx.disconnect();
            return;
        }
        buf.readBytes(new byte[20]);

        // 数据单元长度
        int length = buf.readShort();
        if (buf.readableBytes() < length + 1){

            buf.resetReaderIndex();
            return;
        }
        buf.readBytes(new byte[length]);

        // 校验位
        int last = buf.readByte();

        buf.resetReaderIndex();

        byte[] content = new byte[2 + 20 + 2 + length];
        buf.getBytes(0, content);

        // 计算校验位
        byte check = CommonUtil.getCheck(content);

        byte[] bytes = new byte[2 + 20 + 2 + length + 1];
        buf.readBytes(bytes);

        // 验证校验位
        if (last != check){
            logger.error("校验位错误, 原始数据[{}]!", CommonUtil.bytesToStr(bytes));
            ctx.close();
            return;
        }
        out.add(Unpooled.copiedBuffer(bytes));
    }
}
