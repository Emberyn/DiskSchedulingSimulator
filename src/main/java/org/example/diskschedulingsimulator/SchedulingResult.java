package org.example.diskschedulingsimulator;

import java.util.List;

/**
 * 该类用于存储磁盘调度算法的调度结果。
 * 包含了算法名称、总寻道距离、平均寻道时间、寻道序列、初始磁头位置和磁盘请求列表等信息。
 */
public class SchedulingResult {
    private String algorithmName;
    private int totalSeekDistance;
    private double averageSeekTime;
    private List<Integer> seekSequence;
    private int initialHeadPosition;
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



    public String getAlgorithmName() {
        return algorithmName;
    }


    public int getTotalSeekDistance() {
        return totalSeekDistance;
    }


    public double getAverageSeekTime() {
        return averageSeekTime;
    }


    public List<Integer> getSeekSequence() {
        return seekSequence;
    }


    public int getInitialHeadPosition() {
        return initialHeadPosition;
    }


    public List<DiskRequest> getRequests() {
        return requests;
    }
}