package org.example.diskschedulingsimulator;

import java.util.List;

/**
 * 磁盘调度算法的接口。
 * 定义了磁盘调度算法需要实现的方法，包括调度方法、获取算法名称和算法描述的方法。
 */
public interface DiskSchedulingAlgorithm {

    //执行磁盘调度算法。
    SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition);

    String getAlgorithmName();

    String getAlgorithmDescription();
}