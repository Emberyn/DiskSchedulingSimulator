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
            DiskRequest closestRequest = null;
            int minDistance = Integer.MAX_VALUE;

            // 遍历剩余的请求，找到距离当前磁头位置最近的请求
            for (DiskRequest request : remainingRequests) {
                int distance = Math.abs(request.getTrackNumber() - currentPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRequest = request;
                }
            }

            // 如果找到了最近的请求
            if (closestRequest != null) {
                seekSequence.add(closestRequest.getTrackNumber());
                totalSeekDistance += minDistance;
                currentPosition = closestRequest.getTrackNumber();

                closestRequest.setProcessingOrder(processedRequests.size());

                remainingRequests.remove(closestRequest);
                processedRequests.add(closestRequest);
            }
        }

        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, processedRequests);
    }


    @Override
    public String getAlgorithmName() {
        return "SSTF";
    }


    @Override
    public String getAlgorithmDescription() {
        return "最短寻道时间优先算法";
    }
}