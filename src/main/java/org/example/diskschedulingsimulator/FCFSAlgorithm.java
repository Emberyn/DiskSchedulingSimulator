package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 先来先服务（FCFS）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法按照请求的到达时间顺序依次处理磁盘请求。
 */
public class FCFSAlgorithm extends BaseAlgorithm {
    /**
     * 执行 FCFS 磁盘调度算法。
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

        // 当前磁头位置，初始化为初始磁头位置
        int currentPosition = initialHeadPosition;
        // 总寻道距离，初始化为 0
        int totalSeekDistance = 0;

        // 创建一个副本列表，用于存储请求
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        // 按到达时间对请求进行排序
        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getArrivalTime(), r2.getArrivalTime()));

        // 遍历排序后的请求列表
        for (int i = 0; i < requestsCopy.size(); i++) {
            // 获取当前请求
            DiskRequest request = requestsCopy.get(i);
            // 获取当前请求的磁道号
            int trackNumber = request.getTrackNumber();
            // 将当前请求的磁道号添加到寻道序列中
            seekSequence.add(trackNumber);

            // 计算当前请求与当前磁头位置的寻道距离
            int seekDistance = Math.abs(trackNumber - currentPosition);
            // 累加总寻道距离
            totalSeekDistance += seekDistance;
            // 更新当前磁头位置为当前请求的磁道号
            currentPosition = trackNumber;

            // 设置该请求的处理顺序
            request.setProcessingOrder(i);
        }

        // 创建调度结果对象并返回
        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, requestsCopy);
    }

    /**
     * 获取算法名称。
     * @return 算法名称，即 "FCFS"
     */
    @Override
    public String getAlgorithmName() {
        return "FCFS";
    }

    /**
     * 获取算法描述。
     * @return 算法描述，即 "先来先服务算法"
     */
    @Override
    public String getAlgorithmDescription() {
        return "先来先服务算法";
    }
}