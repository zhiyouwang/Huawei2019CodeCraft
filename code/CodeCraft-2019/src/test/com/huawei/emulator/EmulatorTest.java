package com.huawei.emulator;

import org.junit.Test;

import java.util.Arrays;

public class EmulatorTest {

    @Test
    public void testAssociateCarsWithSourceCross() {
        String carFilePath = "config/car.txt";
        String crossFilePath = "config/cross.txt";
        String roadFilePath = "config/road.txt";

//        ArrayList<Car> carList = loadCarFile(carFilePath);
//        ArrayList<Cross> crossList = loadCrossFile(crossFilePath);
//        ArrayList<Road> roadList = loadRoadFile(roadFilePath);

        int[] test = new int[] {6, 1, 2, -1, -1, 5};
        Arrays.sort(test);

//        Emulator emulator = new Emulator(carList, roadList, crossList);
    }

}