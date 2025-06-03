package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

public class CSCANAlgorithm extends BaseAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);
        
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getTrackNumber(), r2.getTrackNumber()));
        
        List<DiskRequest> left = new ArrayList<>();
        List<DiskRequest> right = new ArrayList<>();
        
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
        
        // 向右扫描
        for (DiskRequest request : right) {
            seekSequence.add(request.getTrackNumber());
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            currentPosition = request.getTrackNumber();
            request.setProcessingOrder(processedRequests.size());
            processedRequests.add(request);
        }
        
        // 到达最右端并返回最左端
        if (!right.isEmpty() && !left.isEmpty()) {
            seekSequence.add(1499);
            totalSeekDistance += Math.abs(1499 - currentPosition);
            seekSequence.add(0);
            totalSeekDistance += 1499;
            currentPosition = 0;
        }
        
        // 从左端开始扫描
        for (DiskRequest request : left) {
            seekSequence.add(request.getTrackNumber());
            totalSeekDistance += Math.abs(request.getTrackNumber() - currentPosition);
            currentPosition = request.getTrackNumber();
            request.setProcessingOrder(processedRequests.size());
            processedRequests.add(request);
        }
        
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