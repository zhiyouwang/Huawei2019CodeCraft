package com.huawei.util;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Util {

    /**
     * 从给定的文件中读取车辆列表，返回的车辆列表可以通过车辆 ID 直接访问。车辆从 0 开始编号。
     *
     * @param filePath 车辆记录文件
     * @return 车辆列表
     */
    public static HashMap<Integer, Car> loadCarFile(String filePath) {
        HashMap<Integer, Car> result = new HashMap<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            String[] parameters;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                parameters = StringUtils.split(StringUtils.strip(line, "()"), ", ");
                result.put(Integer.parseInt(parameters[0]), new Car(
                        // 车辆 ID
                        Integer.parseInt(parameters[0]),
                        // 车辆出发点路口 ID
                        Short.parseShort(parameters[1]),
                        // 车辆终点路口 ID
                        Short.parseShort(parameters[2]),
                        // 车辆最大速度
                        Byte.parseByte(parameters[3]),
                        // 车辆计划出发时间
                        Short.parseShort(parameters[4])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从给定的文件中读取道路列表。道路列表可以用道路 ID 直接访问。道路从 0 开始编号。
     *
     * @param filePath 道路列表文件路径
     * @return 道路列表
     */
    public static HashMap<Short, Road> loadRoadFile(String filePath) {
        HashMap<Short, Road> result = new HashMap<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            String[] parameters;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                parameters = StringUtils.split(StringUtils.strip(line, "()"), ", ");
                result.put(Short.parseShort(parameters[0]), new Road(
                        // 道路 ID
                        Short.parseShort(parameters[0]),
                        // 道路长度
                        Byte.parseByte(parameters[1]),
                        // 道路最大车速
                        Byte.parseByte(parameters[2]),
                        // 道路单侧车道数
                        Byte.parseByte(parameters[3]),
                        // 道路起点路口 ID
                        Short.parseShort(parameters[4]),
                        // 道路终点路口 ID
                        Short.parseShort(parameters[5]),
                        // 是否为双向道路
                        parameters[6].equals("1")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从路口列表文件中读取路口列表。路口列表可以用路口 ID 直接获取路口实例。路口从 1 开始编号。
     *
     * @param filePath 路口文件路径
     * @return 路口列表
     */
    public static LinkedHashMap<Short, Cross> loadCrossFile(String filePath) {
        LinkedHashMap<Short, Cross> result = new LinkedHashMap<>();
        ArrayList<Cross> temp = new ArrayList<>(256);
        // 路口列表从 1 开始编号，需要添加一个空白的占位元素
        result.put((short) 0, null);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            String[] parameters;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                parameters = StringUtils.split(StringUtils.strip(line, "()"), ", ");
                temp.add(new Cross(
                        // 路口 ID
                        Short.parseShort(parameters[0]),
                        // 道路 1 的 ID
                        Short.parseShort(parameters[1]),
                        // 道路 2 的 ID
                        Short.parseShort(parameters[2]),
                        // 道路 3 的 ID
                        Short.parseShort(parameters[3]),
                        // 道路 4 的 ID
                        Short.parseShort(parameters[4])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp.sort(Comparator.comparingInt(o -> o.crossId));
        for (Cross cross : temp) {
            result.put(cross.crossId, cross);
        }
        return result;
    }

    /**
     * 输出答案到指定文件中
     *
     * @param carArchive     车辆列表
     * @param answerFilePath 答案文件路径
     */
    public static void outputCarPathHistoriesString(HashMap<Integer, int[]> carArchive,
                                                    String answerFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(answerFilePath)));
        for (int[] carInfo : carArchive.values()) {
            writer.write(getPathHistoriesInRecordFormat(carInfo));
            writer.write("\n");
        }
        writer.close();
    }

    /**
     * 以 answer.txt 中要求的格式返回当前车辆的途径路口历史记录
     *
     * @return 符合记录格式的路口历史记录
     */
    private static String getPathHistoriesInRecordFormat(int[] carInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(carInfo[0]).append(", ").append(carInfo[1]);
        for (int i = 2; i < carInfo.length; i++) {
            builder.append(Constant.PATH_HISTORY_PREFIX_SEPARATOR);
            builder.append(carInfo[i]);
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 获得两个路口之间连接的道路 ID
     *
     * @param thisCrossId 路口 1
     * @param nextCrossId 路口 2
     * @param crossMap    路口列表
     * @return 连接两个路口的道路的 ID
     */
    public static short getInterconnectedRoadId(short thisCrossId, short nextCrossId, HashMap<Short, Cross> crossMap) {
        for (Road road : crossMap.get(thisCrossId).connectedRoadsInOrder) {
            if ((road.source == thisCrossId && road.destination == nextCrossId) ||
                    (road.source == nextCrossId && road.destination == thisCrossId)) {
                return road.roadId;
            }
        }
        return 0;
    }

    /**
     * 获取空白 snapshot
     *
     * @param length snapshot 长度
     * @return 空白 snapshot
     */
    public static short[][] getEmptySnapshot(int length) {
        return new short[length][4];
    }

    /**
     * 根据当前路口 ID 与道路计算车道方向. 从 source -> destination 方向返回 0, destination -> source 方向返回 1.
     *
     * @param road        指定道路
     * @param thisCrossId 起点路口 ID
     * @return 方向
     */
    public static int getLaneByCrossId(Road road, int thisCrossId) {
        return (road.source == thisCrossId) ? 0 : 1;
    }

    /**
     * 计算当前时刻的预计等待时间
     *
     * @param tick 当前时刻
     * @return 预计的等待时间
     */
    public static byte getQueuingDelay(int tick) {
        double base = (double) tick / 100;
        return (byte) (base * Math.pow((1 + base / 2), 2));
    }

}
