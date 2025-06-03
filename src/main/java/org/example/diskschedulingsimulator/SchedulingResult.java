package org.example.diskschedulingsimulator;

import java.util.List;

public class SchedulingResult {
    private String algorithmName;
    private int totalSeekDistance;
    private double averageSeekTime;
    private List<Integer> seekSequence;
    private int initialHeadPosition;
    private List<DiskRequest> requests;

    public SchedulingResult(List<Integer> seekSequence, int totalSeekDistance, 
                           double averageSeekTime, String algorithmName, 
                           int initialHeadPosition, List<DiskRequest> requests) {
        this.seekSequence = seekSequence;
        this.totalSeekDistance = totalSeekDistance;
        this.averageSeekTime = averageSeekTime;
        this.algorithmName = algorithmName;
        this.initialHeadPosition = initialHeadPosition;
        this.requests = requests;
    }

    // Getters
    public String getAlgorithmName() { return algorithmName; }
    public int getTotalSeekDistance() { return totalSeekDistance; }
    public double getAverageSeekTime() { return averageSeekTime; }
    public List<Integer> getSeekSequence() { return seekSequence; }
    public int getInitialHeadPosition() { return initialHeadPosition; }
    public List<DiskRequest> getRequests() { return requests; }
}