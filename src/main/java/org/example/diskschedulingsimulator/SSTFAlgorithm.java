package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

public class SSTFAlgorithm extends BaseAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);
        
        List<DiskRequest> remainingRequests = new ArrayList<>(requests);
        List<DiskRequest> processedRequests = new ArrayList<>();
        
        int currentPosition = initialHeadPosition;
        int totalSeekDistance = 0;
        
        while (!remainingRequests.isEmpty()) {
            DiskRequest closestRequest = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (DiskRequest request : remainingRequests) {
                int distance = Math.abs(request.getTrackNumber() - currentPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRequest = request;
                }
            }
            
            if (closestRequest != null) {
                seekSequence.add(closestRequest.getTrackNumber());
                totalSeekDistance += minDistance;
                currentPosition = closestRequest.getTrackNumber();
                
                closestRequest.setProcessingOrder(processedRequests.size());
                
                remainingRequests.remove(closestRequest);
                processedRequests.add(closestRequest);
            }
        }
        
        return createResult(seekSequence, totalSeekDistance, requests, initialHeadPosition, processedRequests);
    }
    
    @Override
    public String getAlgorithmName() {
        return "SSTF";
    }
    
    @Override
    public String getAlgorithmDescription() {
        return "最短寻道时间优先算法";
    }
}