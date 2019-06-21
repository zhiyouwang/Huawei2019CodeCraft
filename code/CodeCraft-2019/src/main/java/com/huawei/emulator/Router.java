package com.huawei.emulator;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.util.Constant;

import java.util.HashMap;
import java.util.LinkedList;

import static com.huawei.util.Util.getLaneByCrossId;
import static com.huawei.util.Util.getQueuingDelay;

class Router {

    // 路网的节点总数
    private short numberOfNodes;

    // 路网的邻接表
    private HashMap<Short, HashMap<Short, Road>> adjacentList;
    // source 节点到其他节点的距离
    private float[] cost;
    // 直接前驱表
    private short[] path;
    // 本节点是否已被访问
    private boolean[] visited;
    // 用来保存尚未得到处理的节点的队列
    private LinkedList<Short> unhandledNodes = new LinkedList<>();

    private byte maxNumberOfLaneGlobal = 0;
    private byte maxSpeedOfRoadGlobal = 0;
    private HashMap<Short, Cross> crossMap;

    Router(HashMap<Short, Road> roadMap, HashMap<Short, Cross> crossMap) {
        this.numberOfNodes = (short) crossMap.size();
        this.crossMap = crossMap;
        this.adjacentList = constructAdjacentList(crossMap);
        cost = new float[numberOfNodes];
        path = new short[numberOfNodes];
        visited = new boolean[numberOfNodes];
        short i = 0;
        for (Road road : roadMap.values()) {
            // 求最大车道数
            if (road.lanesPerSide > maxNumberOfLaneGlobal) {
                maxNumberOfLaneGlobal = road.lanesPerSide;
            }
            // 求最大道路限速
            if (road.maxSpeed > maxSpeedOfRoadGlobal) {
                maxSpeedOfRoadGlobal = road.maxSpeed;
            }
            road.index = i++;
        }
    }

    /**
     * 求此车辆从当前路口出发应往何方向
     *
     * @param car      需要计算路径的车辆
     * @param snapshot 道路车辆数目快照
     * @param tick     当前时间片
     */
    void getDirection(Car car, HashMap<Short, short[][]> snapshot, short tick) {
        short[] path = dijkstra(car.sourceCrossId, car, snapshot, tick);

        // 直接前驱路口的 ID
        short priorCrossId = car.destinationCrossId;
        // 直接前驱路口在 path 数组中的位置
        short priorCrossIndex = crossMap.get(priorCrossId).index;

        do {
            car.intermediateCrosses.addFirst(priorCrossId);
            priorCrossId = path[priorCrossIndex];
            priorCrossIndex = crossMap.get(priorCrossId).index;
        } while (priorCrossId != car.sourceCrossId);

//        car.intermediateCrosses.offerFirst(car.destinationCrossId);
//        int prior = crossMap.get(car.destinationCrossId).index;
//        // 生成并缓存计划经过的路口序列
//        while (path[prior] != car.sourceCrossId) {
//            car.intermediateCrosses.offerFirst(prior);
//            prior = crossMap.get(path[prior]).index;
//        }
//        // 取回路径中的下一路口 ID
//        car.intermediateCrosses.getFirst();
    }

    /**
     * 根据道路表和路口表构造路网
     *
     * @param crossMap 路口表
     * @return 对应路网的邻接表
     */
    private HashMap<Short, HashMap<Short, Road>> constructAdjacentList(HashMap<Short, Cross> crossMap) {
        // 空白邻接表
        HashMap<Short, HashMap<Short, Road>> adjacentList = new HashMap<>();

        // 逐个路口建立邻接表
        for (Cross cross : crossMap.values()) {
            // 避开空元素
            if (cross == null) {
                continue;
            }
            // key 为目标路口 ID, value 为途径的道路的权值
            HashMap<Short, Road> mapForCurrentNode = new HashMap<>();
            // 遍历所有连接到的道路
            for (Road road : cross.connectedRoadsInOrder) {
                addEdge(mapForCurrentNode, cross.crossId, road);
            }
            adjacentList.put(cross.crossId, mapForCurrentNode);
        }

        return adjacentList;
    }

    /**
     * 把边添加到邻接表中
     *
     * @param mapForCurrentNode 当前道路的邻接表
     * @param crossId           当前路口 ID
     * @param road              欲添加的道路
     */
    private void addEdge(HashMap<Short, Road> mapForCurrentNode, int crossId, Road road) {
        if (!road.isDuplex && road.source != crossId) {
            // 非起点的单行道不需要添加到邻接表中
            return;
        }
        // 确定对向路口 ID
        short anotherCrossId;
        if (road.source == crossId) {
            anotherCrossId = road.destination;
        } else {
            anotherCrossId = road.source;
        }
        // 暂时以道路长度作为权值
        mapForCurrentNode.put(anotherCrossId, road);
    }

    /**
     * 使用迪杰斯特拉算法求 source 到图中其他顶点的距离。结果以最短路径的直接前驱表示。
     *
     * @param sourceId 出发点路口 ID
     * @param snapshot 道路车辆数目快照
     * @param tick     当前时间片
     * @return 出发点路口到其他顶点的最短路径的直接前驱（形式为路口 ID）
     */
    private short[] dijkstra(short sourceId, Car car, HashMap<Short, short[][]> snapshot, short tick) {
        // 重新初始化用到的状态变量
        for (int i = 0; i < numberOfNodes; i++) {
            cost[i] = Float.POSITIVE_INFINITY;
            path[i] = 0;
            visited[i] = false;
        }

        // 添加起点, 并初始化出发时间为当前时刻
        crossMap.get(sourceId).startTime = tick;
        cost[crossMap.get(sourceId).index] = 0;
        unhandledNodes.offer(sourceId);

        short currentNodeId;
        while (!unhandledNodes.isEmpty()) {
            currentNodeId = unhandledNodes.remove();
            Cross currentCross = crossMap.get(currentNodeId);

            HashMap<Short, Road> mapForCurrentNode = adjacentList.get(currentNodeId);
            for (Short neighbourCrossId : mapForCurrentNode.keySet()) {
                float cost;
                if (car.intermediateCrosses.contains(neighbourCrossId) || neighbourCrossId == car.sourceCrossId) {
                    cost = Float.POSITIVE_INFINITY;
                } else {
                    cost = getCost(mapForCurrentNode.get(neighbourCrossId), car, currentCross.startTime, currentNodeId, snapshot);
                }
                if (!visited[crossMap.get(neighbourCrossId).index]) {
                    unhandledNodes.offer(neighbourCrossId);
                    visited[crossMap.get(neighbourCrossId).index] = true;
                }
                if (this.cost[currentCross.index] + cost <
                        this.cost[crossMap.get(neighbourCrossId).index]) {
                    this.cost[crossMap.get(neighbourCrossId).index] = this.cost[currentCross.index] + cost;
                    path[crossMap.get(neighbourCrossId).index] = currentNodeId;
                }
            }
        }

        return path;
    }

    /**
     * 动态车辆计算走指定道路的成本
     *
     * @param road        指定道路
     * @param car         指定车辆
     * @param startTime   从本路口出发的时刻
     * @param thisCrossId 本路口的编号
     * @param snapshot    道路车流量快照
     * @return 走这条道路的成本
     */
    private float getCost(Road road, Car car, short startTime, short thisCrossId, HashMap<Short, short[][]> snapshot) {
        byte numberOfLane = road.lanesPerSide;
        byte actualSpeed = (byte) Math.min(road.maxSpeed, car.maxSpeed);
        if (road.lanesPerSide == 1) {
            return 5120;
        }

        /* 车道数调整因子, 要求优先选中车道数多的道路 */
        float laneFactor = (float) (maxNumberOfLaneGlobal - numberOfLane) / 10;
        if (laneFactor == 0) {
            laneFactor = 0.001f;
        }
        if (laneFactor > 0.8) {
            laneFactor *= 4;
        } else if (laneFactor > 0.6) {
            laneFactor *= 2;
        } else if (laneFactor > 0.4) {
            laneFactor *= 2;
        } else if (laneFactor > 0.2) {
            laneFactor *= 1;
        }
//        laneFactor /= 8;

        /* 平均负载调整因子, 要求优先选中平均负载小的道路 */
        // 通过该道路所需要的时间片, 向上取整
        byte tickToPass = (byte) ((byte) Math.ceil((float) road.length / actualSpeed) + getQueuingDelay(startTime));
        // 道路的负载
        float load = 0;
        // 本道路被选中的次数
        byte selected = 0;
        for (short i = 0; i < tickToPass; i++) {
            // 对应时间片的快照不存在的时候就认为此时刻道路上的负载为零
            if (snapshot.containsKey((short) (startTime + i))) {
                load += snapshot.get((short) (startTime + i))[road.index][getLaneByCrossId(road, thisCrossId)];
                selected += snapshot.get((short) (startTime + i))[road.index][getLaneByCrossId(road, thisCrossId) + 2];
                if (selected > Constant.MAX_SELECTED_COUNT) {
                    return 2048;
                }
            }
        }

        float loadFactor;
        // 求这些时间片上的道路的平均负载
        if (load == 0) {
            // 道路在这几个时间片上完全没有车辆, 取一个小的数让它被选中
            loadFactor = 0.001f;
        } else {
            loadFactor = load / tickToPass;
        }
        if (loadFactor > 0.75) {
            return 2048;
        } else if (loadFactor > 0.5) {
            loadFactor *= 8;
        } else if (loadFactor > 0.25) {
            loadFactor *= 4;
        } else if (loadFactor > 0) {
            loadFactor *= 1;
        }

        /* 车速调整因子, 要求优先选中靠近全局最大速度的道路 */
        float speedFactor = maxSpeedOfRoadGlobal - actualSpeed;
        if (speedFactor == 0) {
            speedFactor = 0.001f;
        }
        if (speedFactor > 8) {
            speedFactor *= 1.16;
        } else if (speedFactor > 6) {
            speedFactor *= 1.08;
        } else if (speedFactor > 4) {
            speedFactor *= 1.04;
        } else if (speedFactor > 2) {
            speedFactor *= 1.02;
        }

        // 双向道路调整因子, 要求优先选中双向道路
        float duplexFactor = road.isDuplex ? 1 : 4;

        float lengthFactor = (float) road.length / 10;

        return lengthFactor * loadFactor * duplexFactor * laneFactor * speedFactor;
//        return road.length;
    }
}
