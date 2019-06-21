package com.huawei.emulator;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.util.Constant;
import com.huawei.util.Util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import static com.huawei.util.Util.*;

public class Emulator {

    private HashMap<Short, Cross> crossMap;
    private HashMap<Short, Road> roadMap;
    private HashMap<Integer, Car> carMap;

    private Router router;

    // 当前系统时间片
    private short tick = 0;
    public int leavedCars = 0;
    // 保存每个时间片上各道路双向道路车辆数目的快照集合. 行号为道路的 index. snapshot 的 0 列是 source -> destination 方向的车辆数目,
    // 1 列是 destination -> source 方向的车辆数目.
    private HashMap<Short, short[][]> snapshot = new HashMap<>();
    // 用于保存每时间片的快照的数组的长度
    private short snapshotLength;

    public HashMap<Integer, int[]> carArchive = new HashMap<>();

    /**
     * 构造函数, 负责初始化所需要的各种对象
     *
     * @param carMap   车辆列表
     * @param roadMap  道路列表
     * @param crossMap 路口列表
     */
    public Emulator(HashMap<Integer, Car> carMap, HashMap<Short, Road> roadMap, HashMap<Short, Cross> crossMap) {
        this.crossMap = crossMap;
        this.roadMap = roadMap;
        this.carMap = carMap;
        snapshotLength = (short) roadMap.size();
        // 把车辆按照计划出发时间添加到出发点路口的等待列表中, 便于调度
        LinkedList<Car> waitingList;
        for (Car car : carMap.values()) {
            waitingList = crossMap.get(car.sourceCrossId).waitingList;
            waitingList.offer(car);
            waitingList.sort(Comparator.comparingInt(o -> o.plannedDepartureTime));
        }

        // 保存道路在路口的相对顺序, 并在每个路口按照道路 ID 从小到大进行排序
        short i = 1;
        for (Cross cross : crossMap.values()) {
            // 注意避开空元素
            if (cross == null) {
                continue;
            }
            for (short roadId : cross.roads) {
                if (roadId != Constant.NO_ROAD_CONNECTED) {
                    cross.connectedRoadsInOrder.add(roadMap.get(roadId));
                }
            }
            // 及时清理无用的字段
            cross.roads = null;
            // 设置路口在数组中的相对下标
            cross.index = i++;
            // 进行按照道路 ID 大小进行排序, 以后按照这个顺序进行调度
            cross.connectedRoadsInOrder.sort(Comparator.comparingInt(o -> o.roadId));
        }
        // 进行方向计算的路由器类
        router = new Router(roadMap, crossMap);
    }

    /**
     * 启发式规划
     */
    public void plan() {
        tick += 1;
        System.out.println(tick + " " + leavedCars);
        byte margin = 0;
        if (tick > 4000) {
            margin = 2;
        } else if (tick > 3500) {
            margin = 2;
        } else if (tick > 3000) {
            margin = 2;
        } else if (tick > 2500) {
            margin = 2;
        } else if (tick > 2000) {
//            margin = 2;
        } else if (tick > 1500) {
//            margin = 3;
        } else if (tick > 1000) {
//            margin = 4;
        } else if (tick > 500) {
//            margin = 2;
        }
        for (Cross cross : crossMap.values()) {
            if (cross == null || cross.waitingList == null) {
                // 避开空元素
                continue;
            }
            if (cross.nextDepartureTime == tick) {
                LinkedList<Car> waitingList = cross.waitingList;
                LinkedList<Car> availableCars = new LinkedList<>();
                for (short i = 0; i < waitingList.size(); i++) {
                    // 划定可以出发的车辆的范围, copy 一份数组出来
                    if (tick >= waitingList.getFirst().plannedDepartureTime) {
                        // 把可以出发的车辆添加到临时列表中
                        availableCars.offer(waitingList.removeFirst());
                        i -= 1;
                    } else {
                        // 遇到不能出发的车辆, 则其后的车辆都不能出发
                        break;
                    }
                }
                if (availableCars.isEmpty()) {
                    // 无可出发车辆, 跳转至下一个路口
                    continue;
                }
                // 有可出发车辆, 按照车辆的速度进行排序
                availableCars.sort(Comparator.comparingInt(o -> o.maxSpeed));
                byte counter = 1;
                if (tick > 4000) {
                    counter = (availableCars.size() > 3) ? 3 : (byte) availableCars.size();
                } else if (tick > 3500) {
                    counter = (availableCars.size() > 2) ? 2 : (byte) availableCars.size();
                } else if (tick > 3000) {
//                    counter = (availableCars.size() > 2) ? 2 : (byte) availableCars.size();
                } else if (tick > 2500) {
//                    counter = (availableCars.size() > 2) ? 2 : (byte) availableCars.size();
                } else if (tick > 2000) {
//                    counter = (availableCars.size() > 2) ? 2 : (byte) availableCars.size();
                } else if (tick > 1500) {
//                    counter = (availableCars.size() > 2) ? 2 : (byte) availableCars.size();
                } else if (tick > 1000) {
//                    counter = (availableCars.size() > 1) ? 1 : (byte) availableCars.size();
                } else if (tick > 500) {
//                    counter = (availableCars.size() > 1) ? 1 : (byte) availableCars.size();
                }
                for (short i = 0; i < counter; i++) {
                    Car car = availableCars.removeLast();
                    // 根据 snapshot 来规划路线
                    router.getDirection(car, snapshot, tick);
                    car.intermediateCrosses.offerFirst(cross.crossId);
                    car.actualDepartureTime = tick;
                    car.roadSequence = new short[car.intermediateCrosses.size() - 1];
                    // 车辆开始运行的时间片
                    short startTick = tick;
                    for (short j = 0; j < car.intermediateCrosses.size() - 1; j++) {
                        // 生成节点之间的道路列表
                        short roadId = getInterconnectedRoadId(car.intermediateCrosses.get(j),
                                car.intermediateCrosses.get(j + 1), crossMap);
                        car.roadSequence[j] = roadId;
                        // 根据决定的路线更新 snapshot
                        Road road = roadMap.get(roadId);
                        // 计算受影响的时间片数目
                        short tickToPass =
                                (short) (Math.ceil((float) road.length / Math.min(car.maxSpeed, road.maxSpeed)) +
                                        getQueuingDelay(tick));
                        for (short k = 0; k < tickToPass; k++) {
                            // 在受影响的时间片的位置加一. 存在则直接加一. 如果不存在, 则需要为其创建快照.
                            short key = (short) (startTick + k);
                            if (!snapshot.containsKey(key)) {
                                snapshot.put(key, Util.getEmptySnapshot(snapshotLength));
                            }
                            // 更新在路车辆数目
                            snapshot.get(key)[road.index][getLaneByCrossId(road, car.intermediateCrosses.get(j))] += 1;
                            // 更新选中的时间片
                            snapshot.get(key)[road.index][getLaneByCrossId(road, car.intermediateCrosses.get(j)) + 2] += 1;
                        }
                        // 计算从下一路口出发的时刻
                        startTick += tickToPass;
                    }
                    car.intermediateCrosses = null;
                    // 保存车辆的必要信息, 然后销毁车辆对象
                    int[] carInfo = new int[car.roadSequence.length + 2];
                    carInfo[0] = car.carId;
                    carInfo[1] = car.actualDepartureTime;
                    for (short k = 0; k < car.roadSequence.length; k++) {
                        carInfo[k + 2] = car.roadSequence[k];
                    }
                    carArchive.put(car.carId, carInfo);
                    carMap.remove(car.carId);

                    leavedCars += 1;
                }
                // 剩余的车辆推迟至下一调度
                int size = availableCars.size();
                for (int i = 0; i < size; i++) {
                    waitingList.offerFirst(availableCars.removeFirst());
                }
                cross.nextDepartureTime += (11 - margin);
            }
            // 销毁等待列表
            if (cross.waitingList.isEmpty()) {
                cross.waitingList = null;
            }
        }

        // 如果本时间片存在, 则删除本时间片的 snapshot 以控制内存使用
        snapshot.remove(tick);
//        System.gc();
    }

}
