package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 磁盘调度算法的基类，实现了 DiskSchedulingAlgorithm 接口。
 * 该类提供了一些公共的方法，用于初始化寻道序列和创建调度结果对象。
 */
public abstract class BaseAlgorithm implements DiskSchedulingAlgorithm {
    /**
     * @param seekSequence      寻道序列
     * @param totalSeekDistance 总寻道距离
     * @param requests          磁盘请求列表
     * @param initialHeadPosition 初始磁头位置
     * @param processedRequests 已处理的请求列表
     * @return 包含调度结果的 SchedulingResult 对象
     */
    protected SchedulingResult createResult(List<Integer> seekSequence, int totalSeekDistance,
                                            List<DiskRequest> requests, int initialHeadPosition,
                                            List<DiskRequest> processedRequests) {
        // 计算平均寻道时间，如果请求列表为空，则平均寻道时间为 0
        double averageSeekTime = requests.isEmpty() ? 0 : (double) totalSeekDistance / requests.size();
        // 创建并返回调度结果对象
        return new SchedulingResult(seekSequence, totalSeekDistance, averageSeekTime,
                getAlgorithmName(), initialHeadPosition, processedRequests);
    }

    /**
     * 初始化寻道序列。
     * @param initialHeadPosition 初始磁头位置
     * @return 包含初始磁头位置的寻道序列列表
     */
    protected List<Integer> initializeSeekSequence(int initialHeadPosition) {
        // 创建一个新的列表
        List<Integer> seekSequence = new ArrayList<>();
        // 将初始磁头位置添加到列表中
        seekSequence.add(initialHeadPosition);
        return seekSequence;
    }
}