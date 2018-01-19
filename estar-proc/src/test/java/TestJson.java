import com.alibaba.fastjson.JSON;
import com.diyiliu.common.util.CommonUtil;
import com.diyiliu.common.util.JacksonUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: TestJson
 * Author: DIYILIU
 * Update: 2017-10-30 15:59
 */
public class TestJson {

    @Test
    public void test(){

        Map map = new HashMap();
        map.put(0.1, 10);

        System.out.println(JacksonUtil.toJson(map));

        System.out.println(JSON.toJSONString(map));
    }

    @Test
    public void test1(){

        System.out.println(CommonUtil.toHex(-51));
    }


    @Test
    public void test2(){

        Map m = new HashMap();
        m.put(1, "abc");
        m.put(2, "def");
        m.put(3, new ArrayList(){
            {
                this.add(123);
                this.add(456);
                this.add(new HashMap(){
                    {
                        this.put("hello", "world");
                    }
                });
            }
        });

        System.out.println(JacksonUtil.toJson(m));
    }
}
