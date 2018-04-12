import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Description: TestRedis
 * Author: DIYILIU
 * Update: 2017-11-16 14:53
 */
public class TestRedis {

    @Test
    public void test() {
        Jedis jedis = new Jedis("192.168.1.156", 6379);

        String content = "{\"gpsTime\":\"2017-11-16 14:00:17\",\"vehicleId\":1,\"status\":0,\"lng\":118.739045,\"lat\":31.991662}";
        jedis.publish("EStar:VehicleMove", content);
    }


    @Test
    public void test2() {
        Jedis jedis = new Jedis("192.168.1.156", 6379);
        jedis.select(1);

        String key = "model:gb32960:inOut";
        jedis.set(key, "1");


        System.out.println(jedis.exists(key));
    }
}
