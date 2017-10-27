import com.diyiliu.common.dao.BaseDao;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.tiza.process.common.config.EStarConstant;
import com.tiza.process.common.dao.AlarmDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Description: TestJdbc
 * Author: DIYILIU
 * Update: 2017-10-27 10:59
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext.xml"})
public class TestJdbc {

    @Resource
    private AlarmDao alarmDao;

    @Test
    public void test() throws Exception {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
        dataSource.setJdbcUrl("jdbc:oracle:thin:@192.168.1.156:1521/xgsid.xg156.com");
        dataSource.setUser("ESTAR");
        dataSource.setPassword("ESTAR");

        BaseDao.initDataSource(dataSource);

        EStarConstant.initSqlCache("init-sql.xml");
        alarmDao.selectAlarmStrategy();
    }
}
