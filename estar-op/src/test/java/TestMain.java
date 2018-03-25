import com.tiza.op.model.Position;
import com.tiza.op.util.DateUtil;
import com.tiza.op.util.JacksonUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Description: TestMain
 * Author: DIYILIU
 * Update: 2017-09-28 15:04
 */
public class TestMain {
    private  static  Logger logger = LoggerFactory.getLogger(TestMain.class);

    @Test
    public void test() {

        Calendar calendar = Calendar.getInstance();


        String str = DateUtil.date2Str(calendar.getTime());

        System.out.println(str);

        Date date = DateUtil.str2Date(str);

        System.out.println(date);
    }

    @Test
    public void test1() {
        List<Position> list = new ArrayList() {
            {
                this.add(new Position(11.1, 111.2, 1005l, 1.0));
                this.add(new Position(11.2, 111.3, 1003l, 1.1));
                this.add(new Position(11.3, 111.4, 1002l, 1.2));
                this.add(new Position(11.4, 111.5, 1001l, 0.9));
                this.add(new Position(11.5, 111.6, 1004l, 1.4));
            }
        };

        // 按时间排序
        Collections.sort(list);

        // 当日最小位置
        double minMileage = list.get(0).getMileage();
        // 当日最大里程
        double maxMileage = list.get(list.size() - 1).getMileage();

        System.out.println(list.get(0).getDateTime());
        System.out.println(list.get(list.size() - 1).getDateTime());
        System.out.println(minMileage);
        System.out.println(maxMileage);
        double dailyMileage = maxMileage - minMileage;

        System.out.println(dailyMileage);
    }
}
