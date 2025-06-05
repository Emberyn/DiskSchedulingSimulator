package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 循环扫描（C-SCAN）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法会先向一个方向（通常是向右）扫描，处理该方向上的所有请求，
 * 到达最右端后，直接返回最左端，再从最左端开始向右扫描处理剩余的请求。
 */
public class CSCANAlgorithm extends BaseAlgorithm {
    /**
     * 执行 C-SCAN 磁盘调度算法。
     * 该方法会计算磁头的寻道序列、总寻道距离，并返回调度结果。
     * @param requests          磁盘请求列表
     * @param initialHeadPosition 初始磁头位置
     * @return 包含调度结果的 SchedulingResult 对象
     */
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        // 初始化寻道序列，将初始磁头位置添加到序列中
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);

        // 创建一个副本列表，用于存储请求
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        // 按磁道号对请求进行排序
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

        // 当前磁头位置，初始化为初始磁头位置
        int currentPosition = initialHeadPosition;
        // 总寻道距离，初始化为 0
        int totalSeekDistance = 0;
        // 创建一个列表，用于存储已处理的请求
        List<DiskRequest> processedRequests = new ArrayList<>();

        // 向右扫描，处理右侧的请求
        for (DiskRequest request : right) {
            // 将当前请求的磁道号添加到寻道序列中
            seekSequence.add(request.getTrackNumber());
            // 计算当前请求与当前磁头位置的寻道距离并累加总寻道距离
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            // 更新当前磁头位置为当前请求的磁道号
            currentPosition = request.getTrackNumber();
            // 设置该请求的处理顺序
            request.setProcessingOrder(processedRequests.size());
            // 将该请求添加到已处理请求列表中
            processedRequests.add(request);
        }

        // 如果右侧和左侧都有请求，到达最右端并返回最左端
        if (!right.isEmpty() && !left.isEmpty()) {
            // 将最右端的磁道号（1499）添加到寻道序列中
            seekSequence.add(1499);
            // 计算到达最右端的寻道距离并累加总寻道距离
            totalSeekDistance += Math.abs(1499 - currentPosition);
            // 将最左端的磁道号（0）添加到寻道序列中
            seekSequence.add(0);
            // 计算返回最左端的寻道距离（1499）并累加总寻道距离
            totalSeekDistance += 1499;
            // 更新当前磁头位置为最左端的磁道号
            currentPosition = 0;
        }

        // 从左端开始扫描，处理左侧的请求
        for (DiskRequest request : left) {
            // 将当前请求的磁道号添加到寻道序列中
            seekSequence.add(request.getTrackNumber());
            // 计算当前请求与当前磁头位置的寻道距离并累加总寻道距离
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            // 更新当前磁头位置为当前请求的磁道号
            currentPosition = request.getTrackNumber();
            // 设置该请求的处理顺序
            request.setProcessingOrder(processedRequests.size());
            // 将该请求添加到已处理请求列表中
            processedRequests.add(request);
        }

        // 创建调度结果对象并返回
        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, processedRequests);
    }

    /**
     * 获取算法名称。
     *
     * @return 算法名称，即 "C-SCAN"
     */
    @Override
    public String getAlgorithmName() {
        return "C-SCAN";
    }

    /**
     * 获取算法描述。
     *
     * @return 算法描述，即 "循环扫描算法"
     */
    @Override
    public String getAlgorithmDescription() {
        return "循环扫描算法";
    }
}