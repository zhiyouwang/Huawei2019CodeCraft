package com.huawei.emulator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class RouterTest {

    private static final Logger logger = Logger.getLogger(RouterTest.class);

    private Router router;

    @Before
    public void beforeTest() {
//        ArrayList<Car> carList = loadCarFile("test_config_1/car.txt");
//        ArrayList<Road> roadList = loadRoadFile("config/road.txt");
//        ArrayList<Cross> crossList = loadCrossFile("config/cross.txt");
//        router = new Router(roadList, crossList, );
    }

    @Test
    public void testConstructAdjacentList() {
    }

    @Test
    public void testDijkstra() {
        // test_config
        // assertArrayEquals(new int[]{0, 0, 1, 1, 5, 3, 4}, router.dijkstra(1));
        // test_config_1
//        Logger logger = Logger.getLogger(Router.class);
//        logger.info("start");
//        int[] result = router.dijkstra(1);
//        System.out.println(result.length - 1);
//        logger.info("end");
//        for (int element : result) {
//            System.out.print(element + ", ");
//        }
//        assertArrayEquals(new int[]{0, 0, 1, 2, 3, 6, 7, 8, 1, 3}, router.dijkstra(1));
    }

    @Test
    public void testGetNextCrossId() {

    }

}
