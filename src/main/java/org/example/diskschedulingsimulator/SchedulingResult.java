package org.example.diskschedulingsimulator;

import java.util.List;

/**
 * 该类用于存储磁盘调度算法的调度结果。
 * 包含了算法名称、总寻道距离、平均寻道时间、寻道序列、初始磁头位置和磁盘请求列表等信息。
 */
public class SchedulingResult {
    // 磁盘调度算法的名称
    private String algorithmName;
    // 总寻道距离
    private int totalSeekDistance;
    // 平均寻道时间
    private double averageSeekTime;
    // 寻道序列，记录磁头移动的轨迹
    private List<Integer> seekSequence;
    // 初始磁头位置
    private int initialHeadPosition;
    // 磁盘请求列表
    private List<DiskRequest> requests;

    /**
     * 构造函数，用于初始化调度结果对象。
     * @param seekSequence      寻道序列
     * @param totalSeekDistance 总寻道距离
     * @param averageSeekTime   平均寻道时间
     * @param algorithmName     算法名称
     * @param initialHeadPosition 初始磁头位置
     * @param requests          磁盘请求列表
     */
    public SchedulingResult(List<Integer> seekSequence, int totalSeekDistance,
                            double averageSeekTime, String algorithmName,
                            int initialHeadPosition, List<DiskRequest> requests) {
        this.seekSequence = seekSequence;
        this.totalSeekDistance = totalSeekDistance;
        this.averageSeekTime = averageSeekTime;
        this.algorithmName = algorithmName;
        this.initialHeadPosition = initialHeadPosition;
        this.requests = requests;
    }

    // Getters 方法，用于获取调度结果的各个属性

    /**
     * 获取算法名称。
     *
     * @return 算法名称
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * 获取总寻道距离。
     *
     * @return 总寻道距离
     */
    public int getTotalSeekDistance() {
        return totalSeekDistance;
    }

    /**
     * 获取平均寻道时间。
     *
     * @return 平均寻道时间
     */
    public double getAverageSeekTime() {
        return averageSeekTime;
    }

    /**
     * 获取寻道序列。
     *
     * @return 寻道序列
     */
    public List<Integer> getSeekSequence() {
        return seekSequence;
    }

    /**
     * 获取初始磁头位置。
     *
     * @return 初始磁头位置
     */
    public int getInitialHeadPosition() {
        return initialHeadPosition;
    }

    /**
     * 获取磁盘请求列表。
     *
     * @return 磁盘请求列表
     */
    public List<DiskRequest> getRequests() {
        return requests;
    }
}