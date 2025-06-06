package org.example.diskschedulingsimulator;

/**
 * 该类表示一个磁盘请求。
 * 包含了磁道号、到达时间、处理状态和处理顺序等信息。
 */
public class DiskRequest {
    private int trackNumber;
    private int arrivalTime;
    private boolean processed;
    private int processingOrder;


    public DiskRequest(int trackNumber, int arrivalTime) {
        this.trackNumber = trackNumber;
        this.arrivalTime = arrivalTime;
        // 初始状态为未处理
        this.processed = false;
        // 初始处理顺序为 -1，表示未处理
        this.processingOrder = -1;
    }


    public int getTrackNumber() {
        return trackNumber;
    }


    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }


    public int getArrivalTime() {
        return arrivalTime;
    }


    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }


    public boolean isProcessed() {
        return processed;
    }


    public void setProcessed(boolean processed) {
        this.processed = processed;
    }


    public int getProcessingOrder() {
        return processingOrder;
    }


    public void setProcessingOrder(int processingOrder) {
        this.processingOrder = processingOrder;
    }


    @Override
    public String toString() {
        return "Track: " + trackNumber + ", Arrival: " + arrivalTime;
    }
}