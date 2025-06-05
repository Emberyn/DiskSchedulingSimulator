package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 最短寻道时间优先（SSTF）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法会优先处理距离当前磁头位置最近的磁盘请求。
 */
public class SSTFAlgorithm extends BaseAlgorithm {
    /**
     * 执行 SSTF 磁盘调度算法。
     * 该方法会计算磁头的寻道序列、总寻道距离，并返回调度结果。
     *
     * @param requests          磁盘请求列表
     * @param initialHeadPosition 初始磁头位置
     * @return 包含调度结果的 SchedulingResult 对象
     */
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        // 初始化寻道序列，将初始磁头位置添加到序列中
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);

        // 创建一个副本列表，用于存储剩余未处理的请求
        List<DiskRequest> remainingRequests = new ArrayList<>(requests);
        // 创建一个列表，用于存储已处理的请求
        List<DiskRequest> processedRequests = new ArrayList<>();

        // 当前磁头位置，初始化为初始磁头位置
        int currentPosition = initialHeadPosition;
        // 总寻道距离，初始化为 0
        int totalSeekDistance = 0;

        // 循环处理剩余的请求，直到所有请求都被处理完
        while (!remainingRequests.isEmpty()) {
            // 最近的请求，初始化为 null
            DiskRequest closestRequest = null;
            // 最小寻道距离，初始化为最大整数
            int minDistance = Integer.MAX_VALUE;

            // 遍历剩余的请求，找到距离当前磁头位置最近的请求
            for (DiskRequest request : remainingRequests) {
                // 计算当前请求与当前磁头位置的距离
                int distance = Math.abs(request.getTrackNumber() - currentPosition);
                // 如果距离小于最小寻道距离，则更新最小寻道距离和最近的请求
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRequest = request;
                }
            }

            // 如果找到了最近的请求
            if (closestRequest != null) {
                // 将最近的请求的磁道号添加到寻道序列中
                seekSequence.add(closestRequest.getTrackNumber());
                // 累加总寻道距离
                totalSeekDistance += minDistance;
                // 更新当前磁头位置为最近请求的磁道号
                currentPosition = closestRequest.getTrackNumber();

                // 设置该请求的处理顺序
                closestRequest.setProcessingOrder(processedRequests.size());

                // 从剩余请求列表中移除该请求
                remainingRequests.remove(closestRequest);
                // 将该请求添加到已处理请求列表中
                processedRequests.add(closestRequest);
            }
        }

        // 创建调度结果对象并返回
        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, processedRequests);
    }

    /**
     * 获取算法名称。
     *
     * @return 算法名称，即 "SSTF"
     */
    @Override
    public String getAlgorithmName() {
        return "SSTF";
    }

    /**
     * 获取算法描述。
     *
     * @return 算法描述，即 "最短寻道时间优先算法"
     */
    @Override
    public String getAlgorithmDescription() {
        return "最短寻道时间优先算法";
    }
}