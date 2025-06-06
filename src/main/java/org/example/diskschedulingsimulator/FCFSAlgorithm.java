package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 * 先来先服务（FCFS）算法实现类，继承自 BaseAlgorithm 类。
 * 该算法按照请求的到达时间顺序依次处理磁盘请求。
 */
public class FCFSAlgorithm extends BaseAlgorithm {

    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        // 初始化寻道序列，将初始磁头位置添加到序列中
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);

        // 当前磁头位置，初始化为初始磁头位置
        int currentPosition = initialHeadPosition;
        int totalSeekDistance = 0;

        // 创建一个副本列表，用于存储请求
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        // 按到达时间对请求进行排序
        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getArrivalTime(), r2.getArrivalTime()));

        // 遍历排序后的请求列表
        for (int i = 0; i < requestsCopy.size(); i++) {
            DiskRequest request = requestsCopy.get(i);
            int trackNumber = request.getTrackNumber();
            seekSequence.add(trackNumber);

            int seekDistance = Math.abs(trackNumber - currentPosition);
            totalSeekDistance += seekDistance;
            currentPosition = trackNumber;

            request.setProcessingOrder(i);
        }

        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, requestsCopy);
    }


    @Override
    public String getAlgorithmName() {
        return "FCFS";
    }


    @Override
    public String getAlgorithmDescription() {
        return "先来先服务算法";
    }
}