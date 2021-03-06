import com.diyiliu.common.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * Description: TestContent
 * Author: DIYILIU
 * Update: 2018-03-21 15:03
 */
public class TestContent {


    @Test
    public void test(){

        byte[] userBytes = CommonUtil.hexStringToBytes("78676A743230313730303030");
        System.out.println(new String(userBytes));

        byte[] pwBytes = CommonUtil.hexStringToBytes("78676A7432303137303030303030303030303030");
        System.out.println(new String(pwBytes));
    }


    @Test
    public void test2(){
        String str = "232301FE4C43465A314650423648305A313737353601001E12031611052E003138393836303742303031313730313038303437310100";
        byte[] bytes = CommonUtil.hexStringToBytes(str);
        System.out.println(CommonUtil.getCheck(bytes));

        System.out.println((byte)0xC8);
    }


    @Test
    public void test3(){
        String str = "232301FE4C43465A314650423648305A313737353601001E12031611052E003138393836303742303031313730313038303437310100C8";
        byte[] bytes = CommonUtil.hexStringToBytes(str);

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        if (buf.readableBytes() < 25){

            return;
        }

        buf.markReaderIndex();

        byte header1 = buf.readByte();
        byte header2 = buf.readByte();

        // 头标识
        if (header1 != 0x23 || header2 != 0x23){

            System.out.println("协议头校验失败，断开连接!");
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

        byte[] arr = new byte[2 + 20 + 2 + length + 1];
        buf.readBytes(arr);

        // 验证校验位
        if (last != check){
            System.out.println("校验位错误, 原始数据: " +  CommonUtil.bytesToStr(arr));
            return;
        }

    }


    @Test
    public void test4(){
        String str = "3131313131313131313131313131313131";

        System.out.println(new String(CommonUtil.hexStringToBytes(str)));
    }
}
