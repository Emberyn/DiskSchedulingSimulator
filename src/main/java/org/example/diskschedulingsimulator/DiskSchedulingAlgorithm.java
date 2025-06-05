package org.example.diskschedulingsimulator;

import java.util.List;

/**
 * 磁盘调度算法的接口。
 * 定义了磁盘调度算法需要实现的方法，包括调度方法、获取算法名称和算法描述的方法。
 */
public interface DiskSchedulingAlgorithm {
    /**
     * 执行磁盘调度算法。
     * 该方法会根据传入的磁盘请求列表和初始磁头位置，计算调度结果并返回。
     *
     * @param requests          磁盘请求列表
     * @param initialHeadPosition 初始磁头位置
     * @return 包含调度结果的 SchedulingResult 对象
     */
    SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition);

    /**
     * 获取算法名称。
     * @return 算法名称
     */
    String getAlgorithmName();

    /**
     * 获取算法描述。
     * @return 算法描述
     */
    String getAlgorithmDescription();
}