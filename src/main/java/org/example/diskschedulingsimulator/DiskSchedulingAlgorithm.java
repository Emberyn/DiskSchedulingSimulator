package org.example.diskschedulingsimulator;

import java.util.List;

public interface DiskSchedulingAlgorithm {
    SchedulingResult schedule(List<DiskRequest> requests, int initialHeadPosition);
    String getAlgorithmName();
    String getAlgorithmDescription();
}