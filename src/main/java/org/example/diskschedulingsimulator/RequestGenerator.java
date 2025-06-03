package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestGenerator {
    private static final Random random = new Random();
    
    public static List<DiskRequest> generateRequests() {
        return generateRequests(50); // 减少请求数量以简化显示
    }
    
    public static List<DiskRequest> generateRequests(int count) {
        List<DiskRequest> requests = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int trackNumber = random.nextInt(1500);
            int arrivalTime = i; // 简化到达时间
            requests.add(new DiskRequest(trackNumber, arrivalTime));
        }
        
        return requests;
    }
}