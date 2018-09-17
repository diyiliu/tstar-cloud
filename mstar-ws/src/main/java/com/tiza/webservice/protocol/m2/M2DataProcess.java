package com.tiza.webservice.protocol.m2;

import com.diyiliu.common.cache.ICache;
import com.diyiliu.common.model.Header;
import com.diyiliu.common.model.IDataProcess;
import com.diyiliu.common.util.CommonUtil;
import com.tiza.webservice.common.model.SendMSG;
import com.tiza.webservice.common.task.MSGSenderTask;
import com.tiza.webservice.protocol.bean.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: M2DataProcess
 * Author: DIYILIU
 * Update: 2017-08-03 19:15
 */

@Service
public class M2DataProcess implements IDataProcess {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int cmd = 0xFF;

    @Resource
    protected ICache m2CMDCacheProvider;

    @Resource
    protected ICache waitRespCacheProvider;

    @Value("${terminalType}")
    protected String terminalType = "mstar_tiza";

    @Override
    public Header dealHeader(byte[] bytes) {

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        int length = buf.readUnsignedShort();

        byte[] termi = new byte[5];
        buf.readBytes(termi);
        String terminalId = CommonUtil.parseSIM(termi);

        int version = buf.readByte();

        int factory = buf.readByte();

        int terminalType = buf.readByte();

        int user = buf.readByte();

        int serial = buf.readUnsignedShort();

        int cmd = buf.readUnsignedByte();

        byte[] content = new byte[buf.readableBytes() - 3];
        buf.readBytes(content);

        int check = buf.readByte();

        byte[] end = new byte[2];
        buf.readBytes(end);

        return new M2Header(cmd, length, terminalId,
                version, factory, terminalType,
                user, serial, content,
                check, end);
    }

    @Override
    public void parse(byte[] content, Header header) {

    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        return headerToSendBytes(m2Header.getContent(), m2Header.getCmd(), m2Header);
    }

    public void init(){
        m2CMDCacheProvider.put(cmd, this);
    }


    /**
     * 指令下发
     * @param id
     * @param cmd
     * @param terminalId
     * @param argus
     */
    public void send(int id, int cmd, String terminalId, Object... argus){

        M2Header header = new M2Header();
        header.setTerminalId(terminalId);

        M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(cmd);
        if (process == null){
            logger.info("指令下发异常, 无此命令[{}]解析器!", CommonUtil.toHex(cmd));
            return;
        }
        byte[] content = process.pack(header, argus);

        SendMSG msg = new SendMSG(terminalId, cmd, content);
        msg.setId(id);

        int serial = header.getSerial();
        if (0x03 == cmd){
            // sim卡后四位 + cmdId
            serial = Integer.parseInt(terminalId.substring(terminalId.length() - 4)) + cmd;
        }else if (0x07 == cmd){
            Map paramMap = (Map) argus[0];
            int paramId = (int) paramMap.get("id");

            // sim卡后四位 + cmdId + 参数id
            serial = Integer.parseInt(terminalId.substring(terminalId.length() - 4)) + cmd + paramId;
        }

        msg.setSerial(serial);
        msg.setTerminalType(terminalType);

        MSGSenderTask.send(msg);
    }

    /**
     * 原始指令下发(锁车)
     * @param id
     * @param cmd
     * @param terminalId
     * @param bytes
     */
    public void send(int id, int cmd, String terminalId, byte[] bytes){

        M2Header header = new M2Header();
        header.setCmd(cmd);
        header.setTerminalId(terminalId);
        header.setContent(bytes);

        byte[] content = pack(header, null);

        SendMSG msg = new SendMSG(terminalId, cmd, content);
        msg.setId(id);

        msg.setSerial(header.getSerial());
        msg.setTerminalType(terminalType);

        MSGSenderTask.send(msg);
    }


    /**
     * 消息头（不包含校验位、结束位）
     *
     * @param header
     * @return
     */
    public byte[] headerToContent(M2Header header) {

        ByteBuf buf = Unpooled.buffer(header.getLength());
        buf.writeShort(header.getLength());
        buf.writeBytes(CommonUtil.packSIM(header.getTerminalId()));
        buf.writeByte(header.getVersion());
        buf.writeByte(header.getFactory());
        buf.writeByte(header.getTerminalType());
        buf.writeByte(header.getUser());
        buf.writeShort(header.getSerial());
        buf.writeByte(header.getCmd());
        buf.writeBytes(header.getContent());

        return buf.array();
    }

    /**
     * header转byte[]
     * @param header
     * @return
     */
    public byte[] headerToBytes(M2Header header) {

        ByteBuf buf = Unpooled.buffer(header.getLength() + 3);
        buf.writeShort(header.getLength());
        buf.writeBytes(CommonUtil.packSIM(header.getTerminalId()));
        buf.writeByte(header.getVersion());
        buf.writeByte(header.getFactory());
        buf.writeByte(header.getTerminalType());
        buf.writeByte(header.getUser());
        buf.writeShort(header.getSerial());
        buf.writeByte(header.getCmd());
        buf.writeBytes(header.getContent());
        buf.writeByte(header.getCheck());
        buf.writeBytes(header.getEnd());

        return buf.array();
    }

    /**
     * header转byte[]，自动生成序列号和校验位
     * @param content
     * @param cmd
     * @param header
     * @return
     */
    public byte[] headerToSendBytes(byte[] content, int cmd, M2Header header) {

        header.setLength(14 + content.length);
        header.setSerial(getMsgSerial());
        header.setCmd(cmd);
        header.setContent(content);

        byte[] bytes = headerToContent(header);
        byte check = CommonUtil.getCheck(bytes);

        header.setCheck(check);

        return headerToBytes(header);
    }

    private int statusBit(long l, int offset) {

        return new Long((l >> offset) & 0x01).intValue();
    }

    /**
     * 命令序号
     **/
    private static AtomicLong msgSerial = new AtomicLong(0);
    private static int getMsgSerial() {
        Long serial = msgSerial.incrementAndGet();
        if (serial > 65535) {
            msgSerial.set(0);
            serial = msgSerial.incrementAndGet();
        }

        return serial.intValue();
    }
}
