package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

public class SCANAlgorithm extends BaseAlgorithm {
    
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
        
        // 到达最右端
        if (!right.isEmpty()) {
            seekSequence.add(1499);
            totalSeekDistance += Math.abs(1499 - currentPosition);
            currentPosition = 1499;
        }
        
        // 向左扫描
        for (int i = left.size() - 1; i >= 0; i--) {
            DiskRequest request = left.get(i);
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
        return "SCAN";
    }
    
    @Override
    public String getAlgorithmDescription() {
        return "扫描算法（电梯算法）";
    }
}