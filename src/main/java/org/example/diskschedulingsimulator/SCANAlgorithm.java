package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描（SCAN）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法会先向一个方向（通常是向右）扫描，处理该方向上的所有请求，
 * 到达最右端后，再向左扫描处理剩余的请求。
 */
public class SCANAlgorithm extends BaseAlgorithm {

    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        // 初始化寻道序列，将初始磁头位置添加到序列中
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);

        // 创建一个副本列表，用于存储请求
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        // 按磁道号对请求进行排序
        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getTrackNumber(), r2.getTrackNumber()));

        List<DiskRequest> left = new ArrayList<>();
        List<DiskRequest> right = new ArrayList<>();

        // 遍历排序后的请求列表，将请求分别添加到左侧和右侧列表中
        for (DiskRequest request : requestsCopy) {
            if (request.getTrackNumber() < initialHeadPosition) {
                left.add(request);
            } else {
                right.add(request);
            }
        }

        // 当前磁头位置，初始化为初始磁头位置
        int currentPosition = initialHeadPosition;
        int totalSeekDistance = 0;
        List<DiskRequest> processedRequests = new ArrayList<>();

        // 向右扫描，处理右侧的请求
        for (DiskRequest request : right) {
            seekSequence.add(request.getTrackNumber());
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            currentPosition = request.getTrackNumber();
            request.setProcessingOrder(processedRequests.size());
            processedRequests.add(request);
        }

        // 如果右侧有请求，到达最右端
        if (!right.isEmpty()) {
            seekSequence.add(1499);
            totalSeekDistance += Math.abs(1499 - currentPosition);
            currentPosition = 1499;
        }

        // 向左扫描，处理左侧的请求
        for (int i = left.size() - 1; i >= 0; i--) {
            DiskRequest request = left.get(i);
            seekSequence.add(request.getTrackNumber());
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            currentPosition = request.getTrackNumber();
            request.setProcessingOrder(processedRequests.size());
            processedRequests.add(request);
        }

        // 创建调度结果对象并返回
        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, processedRequests);
    }


    @Override
    public String getAlgorithmName() {
        return "SCAN";
    }

    @Override
    public String getAlgorithmDescription() {
        return "扫描算法（电梯算法）";
    }
}