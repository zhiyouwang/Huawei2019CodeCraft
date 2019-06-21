package com.huawei;

import com.huawei.emulator.Emulator;
import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

import static com.huawei.util.Util.*;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 4) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

        String carFilePath = args[0];
        String roadFilePath = args[1];
        String crossFilePath = args[2];
        String answerFilePath = args[3];
        logger.info("carFilePath=" + carFilePath);
        logger.info("roadFilePath=" + roadFilePath);
        logger.info("crossFilePath=" + crossFilePath);
        logger.info("answerFilePath=" + answerFilePath);

        HashMap<Integer, Car> carMap = loadCarFile(carFilePath);
        HashMap<Short, Road> roadMap = loadRoadFile(roadFilePath);
        HashMap<Short, Cross> crossMap = loadCrossFile(crossFilePath);

        Emulator emulator = new Emulator(carMap, roadMap, crossMap);

        // 循环进行到下一时刻直到所有车辆均到达目的地
        int size = carMap.size();
        while (emulator.leavedCars != size) {
            emulator.plan();
        }

        // 输出答案文件
        try {
            outputCarPathHistoriesString(emulator.carArchive, answerFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}