package com.huawei.entity;

import java.util.LinkedList;

public class Car {

    // 车辆 ID
    public int carId;
    // 出发点路口 ID
    public short sourceCrossId;
    // 终点路口 ID
    public short destinationCrossId;
    // 车辆最大速度
    public byte maxSpeed;
    // 计划出发时间
    public short plannedDepartureTime;

    // 实际出发时间
    public short actualDepartureTime;
    // 从当前路口出发直至目标路口所经过的节点
    public LinkedList<Short> intermediateCrosses = new LinkedList<>();
    // 通过的道路 ID
    public short[] roadSequence;

    /**
     * 根据文本形式的信息来初始化车辆信息
     *
     * @param carId                车辆 ID
     * @param start                出发地路口 ID
     * @param end                  目的地路口 ID
     * @param maxSpeed             车辆最大速度
     * @param plannedDepartureTime 计划出发时间
     */
    public Car(int carId, short start, short end, byte maxSpeed, short plannedDepartureTime) {
        this.carId = carId;
        this.sourceCrossId = start;
        this.destinationCrossId = end;
        this.maxSpeed = maxSpeed;
        this.plannedDepartureTime = plannedDepartureTime;
    }


}
