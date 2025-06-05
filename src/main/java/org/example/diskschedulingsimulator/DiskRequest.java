package org.example.diskschedulingsimulator;

/**
 * 该类表示一个磁盘请求。
 * 包含了磁道号、到达时间、处理状态和处理顺序等信息。
 */
public class DiskRequest {
    // 磁盘请求的磁道号
    private int trackNumber;
    // 磁盘请求的到达时间
    private int arrivalTime;
    // 磁盘请求是否已被处理的标志
    private boolean processed;
    // 磁盘请求的处理顺序
    private int processingOrder;

    /**
     * 构造函数，用于初始化磁盘请求对象。
     * @param trackNumber 磁道号
     * @param arrivalTime 到达时间
     */
    public DiskRequest(int trackNumber, int arrivalTime) {
        this.trackNumber = trackNumber;
        this.arrivalTime = arrivalTime;
        // 初始状态为未处理
        this.processed = false;
        // 初始处理顺序为 -1，表示未处理
        this.processingOrder = -1;
    }

    // Getters 和 Setters 方法，用于获取和设置磁盘请求的各个属性
    /**
     * 获取磁道号。
     *
     * @return 磁道号
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /**
     * 设置磁道号。
     *
     * @param trackNumber 新的磁道号
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    /**
     * 获取到达时间。
     * @return 到达时间
     */
    public int getArrivalTime() {
        return arrivalTime;
    }

    /**
     * 设置到达时间。
     * @param arrivalTime 新的到达时间
     */
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * 判断请求是否已被处理。
     * @return 如果已处理返回 true，否则返回 false
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * 设置请求的处理状态。
     * @param processed 新的处理状态
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * 获取处理顺序。
     *
     * @return 处理顺序
     */
    public int getProcessingOrder() {
        return processingOrder;
    }

    /**
     * 设置处理顺序。
     *
     * @param processingOrder 新的处理顺序
     */
    public void setProcessingOrder(int processingOrder) {
        this.processingOrder = processingOrder;
    }

    /**
     * 重写 toString 方法，用于方便打印磁盘请求的信息。
     *
     * @return 包含磁道号和到达时间的字符串
     */
    @Override
    public String toString() {
        return "Track: " + trackNumber + ", Arrival: " + arrivalTime;
    }
}