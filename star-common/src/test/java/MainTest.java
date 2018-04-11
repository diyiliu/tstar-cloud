import com.diyiliu.common.model.Circle;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.DateUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public void test5(){

        String str = "2424002D02FE05AD1307E2031E0B2719FFFFFFFFFFFFFFFFFFFF00FF0764428001F10A3E0B6D270AFFFF4581B2";
        int i =   Integer.parseInt(str.substring(12,18),16);

        System.out.println(i);


        System.out.println(Integer.parseInt("FE05AD"));
    }


    @Test
    public void test6(){

        String str = "FFFF";

        byte[] bytes = CommonUtil.hexStringToBytes(str);

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        System.out.println(buf.readUnsignedShort());
    }
}
