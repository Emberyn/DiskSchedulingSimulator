package org.example.diskschedulingsimulator;

public class DiskRequest {
    private int trackNumber;
    private int arrivalTime;
    private boolean processed;
    private int processingOrder;

    public DiskRequest(int trackNumber, int arrivalTime) {
        this.trackNumber = trackNumber;
        this.arrivalTime = arrivalTime;
        this.processed = false;
        this.processingOrder = -1;
    }

    // Getters and Setters
    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }
    
    public int getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(int arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    
    public int getProcessingOrder() { return processingOrder; }
    public void setProcessingOrder(int processingOrder) { this.processingOrder = processingOrder; }

    @Override
    public String toString() {
        return "Track: " + trackNumber + ", Arrival: " + arrivalTime;
    }
}