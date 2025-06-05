package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 该类用于生成磁盘请求列表。
 * 可以生成指定数量的随机磁盘请求，每个请求包含磁道号和到达时间。
 */
public class RequestGenerator {
    // 随机数生成器，用于生成随机的磁道号
    private static final Random random = new Random();

    /**
     * 生成 400 个完全随机的磁盘请求。
     * 调用 generateRequests(int count) 方法，传入参数 400。
     * @return 包含 400 个磁盘请求的列表
     */
    public static List<DiskRequest> generateRequests() {
        return generateRequests(400);
    }

    /**
     * 生成指定数量的随机磁盘请求。
     * 每个请求的磁道号在 0 - 1499 范围内随机生成，到达时间按生成顺序依次递增。
     * @param count 要生成的磁盘请求数量
     * @return 包含指定数量磁盘请求的列表
     */
    public static List<DiskRequest> generateRequests(int count) {
        // 创建一个空的磁盘请求列表
        List<DiskRequest> requests = new ArrayList<>();

        // 循环生成指定数量的磁盘请求
        for (int i = 0; i < count; i++) {
            // 生成 0 - 1499 范围内的随机磁道号
            int trackNumber = random.nextInt(1500);
            // 到达时间按生成顺序依次递增
            int arrivalTime = i;
            // 创建一个新的磁盘请求对象并添加到列表中
            requests.add(new DiskRequest(trackNumber, arrivalTime));
        }

        return requests;
    }
}