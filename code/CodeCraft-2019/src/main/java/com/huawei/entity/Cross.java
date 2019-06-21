package com.huawei.entity;

import com.huawei.util.Constant;

import java.util.ArrayList;
import java.util.LinkedList;

public class Cross {

    // 路口 ID
    public short crossId;
    // 连接到的道路 ID 数组，从 12 点方向开始，顺时针排列 4 条道路
    public short[] roads = new short[Constant.MAX_NUMBER_OF_ROADS];
    // 等待出发的车辆 ID 列表
    public LinkedList<Car> waitingList = new LinkedList<>();
    // 已经排序后的已连接道路列表, 用于确定调度时的顺序. 第一列保存道路 ID, 第二列保存相对位置.
    public ArrayList<Road> connectedRoadsInOrder = new ArrayList<>(Constant.MAX_NUMBER_OF_ROADS);
    // 下一次安排车辆出发的时间
    public short nextDepartureTime = 1;
    // 本路口在标志数组中的下标
    public short index;
    // 从路口出发的时间, 提供启发式算法的时间维度
    public short startTime = 1;

    /**
     * 构造路口
     *
     * @param crossId 路口 ID
     * @param roadId1 道路 ID 1
     * @param roadId2 道路 ID 2
     * @param roadId3 道路 ID 3
     * @param roadId4 道路 ID 4
     */
    public Cross(short crossId, short roadId1, short roadId2, short roadId3, short roadId4) {
        this.crossId = crossId;
        roads[0] = roadId1;
        roads[1] = roadId2;
        roads[2] = roadId3;
        roads[3] = roadId4;
    }

}
