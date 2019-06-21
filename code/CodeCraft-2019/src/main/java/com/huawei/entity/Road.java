package com.huawei.entity;

public class Road {

    // 道路 ID
    public short roadId;
    // 道路长度
    public byte length;
    // 道路允许的最大速度
    public byte maxSpeed;
    // 每侧车道数。双向道路两侧车道数相同。
    public byte lanesPerSide;
    // 道路起点路口 ID
    public short source;
    // 道路终点路口 ID
    public short destination;
    public boolean isDuplex;

    // 道路的相对顺序下标, 用于在 snapshot 中定位本道路的数据
    public int index;

    public Road(short roadId, byte length, byte maxSpeed, byte lanesPerSide, short source, short destination,
                boolean isDuplex) {
        this.roadId = roadId;
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.lanesPerSide = lanesPerSide;
        this.source = source;
        this.destination = destination;
        this.isDuplex = isDuplex;
    }
}
