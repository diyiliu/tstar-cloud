import com.diyiliu.common.util.JacksonUtil;
import com.tiza.process.common.model.VehicleFault;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description: TestCollection
 * Author: DIYILIU
 * Update: 2017-12-26 10:49
 */
public class TestCollection {



    @Test
    public void test(){

        VehicleFault fault1 = new VehicleFault();
        fault1.setFaultUnit("1");
        fault1.setFaultValue("2");

        VehicleFault fault2 = new VehicleFault();
        fault1.setVehicleId(1l);
        fault2.setFaultUnit("1");
        fault2.setFaultValue("2");

        VehicleFault fault3 = new VehicleFault();
        fault3.setFaultUnit("2");
        fault3.setFaultValue("2");

        List l1 = new ArrayList();
//        l1.add(fault1);
        l1.add(fault2);

        List l2 = new ArrayList();
        l2.add(fault1);
        //l2.add(fault3);

        Collection l = CollectionUtils.subtract(l1, l2);
        System.out.println(JacksonUtil.toJson(l));
    }
}
