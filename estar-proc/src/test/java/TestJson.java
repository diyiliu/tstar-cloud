import com.alibaba.fastjson.JSON;
import com.diyiliu.common.util.JacksonUtil;
import org.junit.Test;

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
}
