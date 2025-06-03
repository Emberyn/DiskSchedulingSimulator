package org.example.diskschedulingsimulator;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAlgorithm implements DiskSchedulingAlgorithm {
    
    protected SchedulingResult createResult(List<Integer> seekSequence, int totalSeekDistance, 
                                          List<DiskRequest> requests, int initialHeadPosition, 
                                          List<DiskRequest> processedRequests) {
        double averageSeekTime = requests.isEmpty() ? 0 : (double) totalSeekDistance / requests.size();
        return new SchedulingResult(seekSequence, totalSeekDistance, averageSeekTime, 
                                   getAlgorithmName(), initialHeadPosition, processedRequests);
    }
    
    protected List<Integer> initializeSeekSequence(int initialHeadPosition) {
        List<Integer> seekSequence = new ArrayList<>();
        seekSequence.add(initialHeadPosition);
        return seekSequence;
    }
}