import com.diyiliu.common.model.Circle;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.DateUtil;
import com.diyiliu.common.util.JacksonUtil;
import com.diyiliu.common.util.SpringUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Description: MainTest
 * Author: DIYILIU
 * Update: 2017-09-19 10:43
 */

public class MainTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test() {

        String str = "[{\"lng\":\"121.458091\",\"lat\":\"31.217368\",\"radius\":\"139.749\"}]";

        JavaType javaType = getCollectionType(ArrayList.class, Circle.class);

        try {
            List<Circle> list = mapper.readValue(str, javaType);
            System.out.println(list.get(0).getLng());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    @Test
    public void test2() {

        long mile = 999;

        double mileage = new BigDecimal(mile).divide(new BigDecimal(10)).doubleValue();

        System.out.println(mileage);
    }

    @Test
    public void test3() {
        Date origin = DateUtil.stringToDate("2000-01-01 00:00:00");

        System.out.println(DateUtil.dateToString(origin, "9位数字的毫秒数（不足9位前面补0）:%tN%n"));

        System.out.println(new Date(0));
    }


    @Test
    public void testJs() throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        System.out.println(engine.eval(1 + "*1").toString());
    }


    @Test
    public void test4() {
        String str = "FE";

        byte[] bytes = CommonUtil.hexStringToBytes(str);

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        int b = buf.readByte();

        System.out.println(b);

        System.out.println(0xFE);

        System.out.println(b == 0xFE);
    }


    @Test
    public void test5() {

        String str = "2424002D02FE05AD1307E2031E0B2719FFFFFFFFFFFFFFFFFFFF00FF0764428001F10A3E0B6D270AFFFF4581B2";
        int i = Integer.parseInt(str.substring(12, 18), 16);

        System.out.println(i);


        System.out.println(Integer.parseInt("FE05AD"));
    }


    @Test
    public void test6() {

        String str = "FFFF";

        byte[] bytes = CommonUtil.hexStringToBytes(str);

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        System.out.println(buf.readUnsignedShort());
    }


    @Test
    public void test7() {
        String str = "0000557cbe03";
        byte[] bytes = CommonUtil.hexStringToBytes(str);

        bytes = CommonUtil.byteToByte(bytes, 2, 4, "little");

        System.out.println(CommonUtil.bytesToLong(bytes));
    }

    @Test
    public void test8() {
        String str = "232303FE4C43465A314650423248305A313737373101014F12041209252101020101000000001F3211CE267D620220C3500E00020101033B4E204E203A0000267D050006F86190021F09300700000000000000000008010111CD267D006E00016E1030103110321034103410321031103110321031103210311030103210311033103210321032102F1034103310311032102C102B102B102D102E102D102D102E102C102A102B102D103310311032103410351032103310331034103110351032102D102C102D102E102E102E102E103310351035103410351035103410341035103410341032103210311031103410311031103310301033103110311031102C102C102A102C102C102C102D102B102D102B102C102C102F102F102F1031103310301031102F102E1030102F1030102A102B102E102A102C102A102B0901010016414142414140414141414141414241424141424142410601401035016D102A01154201064046";

        byte[] bytes = CommonUtil.hexStringToBytes(str);

        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);

/*        buf.markReaderIndex();
        byte[] content = new byte[bytes.length - 1];
        buf.readBytes(content);
        byte last = buf.readByte();

        buf.resetReaderIndex();*/

        // buf.readByte();

        byte[] content1 = new byte[bytes.length - 1];
        buf.getBytes(0, content1);

        byte b1 = CommonUtil.getCheck(content1);

        // byte b = CommonUtil.getCheck(content);

        System.out.println(b1);
        //System.out.println(b);
        // System.out.println(last);
    }

    @Test
    public void test9() {
        String str = "232304FE4C43465A314650425848305A31373830380100081204120A05300008";
        byte[] bytes = CommonUtil.hexStringToBytes(str);

        byte b = CommonUtil.getCheck(bytes);

        System.out.println(b);


        str = "4C43465A314650423348305A3138303431";

        System.out.println(new String(CommonUtil.hexStringToBytes(str)));
    }


    @Test
    public void test10() {
        String str = "";
        byte[] bytes = null;

        byte[] content = CommonUtil.hexStringToBytes("0000000F");
        bytes = CommonUtil.byteToByte(content, 4, 1, "big");
    }


    @Test
    public void test11() throws Exception {
        int val = 1;
        final String retVal = CommonUtil.parseExp(val, "*1+0", "decimal");

        Map m = new HashMap() {
            {
                this.put("abc", retVal);
            }
        };

        System.out.println(retVal);
        System.out.println(JacksonUtil.toJson(m));
    }


    @Test
    public void test12() throws Exception {

        String str = "1.0";

        System.out.println(new BigDecimal(str).intValue());
    }
}
