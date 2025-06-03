package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

public class FCFSAlgorithm extends BaseAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition) {
        List<Integer> seekSequence = initializeSeekSequence(initialHeadPosition);
        
        int currentPosition = initialHeadPosition;
        int totalSeekDistance = 0;
        
        List<DiskRequest> requestsCopy = new ArrayList<>(requests);
        requestsCopy.sort((r1, r2) -> Integer.compare(r1.getArrivalTime(), r2.getArrivalTime()));
        
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