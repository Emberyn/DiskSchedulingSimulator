package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestGenerator {
    private static final Random random = new Random();
    
    public static List<DiskRequest> generateRequests() {
        return generateRequests(400); // 生成400个完全随机的请求
    }
    
    public static List<DiskRequest> generateRequests(int count) {
        List<DiskRequest> requests = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int trackNumber = random.nextInt(1500); // 在0-1499范围内完全随机
            int arrivalTime = i; // 简化到达时间
            requests.add(new DiskRequest(trackNumber, arrivalTime));
        }
        
        return requests;
    }
}