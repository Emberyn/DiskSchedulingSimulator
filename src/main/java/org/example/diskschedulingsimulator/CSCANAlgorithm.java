package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 循环扫描（C-SCAN）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法会先向一个方向（通常是向右）扫描，处理该方向上的所有请求，
 * 到达最右端后，直接返回最左端，再从最左端开始向右扫描处理剩余的请求。
 */
public class CSCANAlgorithm extends BaseAlgorithm {
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);

        List<DiskRequest> requestsCopy = new ArrayList<>(requests);

        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getTrackNumber(), r2.getTrackNumber()));

        // 创建两个列表，分别用于存储初始磁头位置左侧和右侧的请求
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

        // 如果右侧和左侧都有请求，到达最右端并返回最左端
        if (!right.isEmpty() && !left.isEmpty()) {
            seekSequence.add(1499);
            totalSeekDistance += Math.abs(1499 - currentPosition);
            seekSequence.add(0);
            totalSeekDistance += 1499;
            currentPosition = 0;
        }

        // 从左端开始扫描，处理左侧的请求
        for (DiskRequest request : left) {
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
        return "C-SCAN";
    }

    @Override
    public String getAlgorithmDescription() {
        return "循环扫描算法";
    }
}