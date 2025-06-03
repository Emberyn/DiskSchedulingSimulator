package org.example.diskschedulingsimulator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiskSchedulingController {
    
    @FXML private TextField initialPositionField;
    @FXML private Button generateButton;
    @FXML private Button simulateButton;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private Canvas visualizationCanvas;
    @FXML private TableView<SchedulingResult> resultsTable;
    @FXML private TableColumn<SchedulingResult, String> algorithmColumn;
    @FXML private TableColumn<SchedulingResult, Integer> totalDistanceColumn;
    @FXML private TableColumn<SchedulingResult, Double> averageTimeColumn;
    @FXML private TextArea requestsTextArea;
    @FXML private Label statusLabel;
    @FXML private Button playPauseButton;
    @FXML private Button stepButton;
    @FXML private Slider speedSlider;
    
    private List<DiskRequest> currentRequests;
    private int initialHeadPosition;
    private List<DiskSchedulingAlgorithm> algorithms;
    
    // 动画相关变量
    private Timeline animationTimeline;
    private int currentAnimationStep = 0;
    private SchedulingResult currentResult;
    private boolean animationPaused = true;
    
    @FXML
    public void initialize() {
        setupAlgorithms();
        setupTable();
        setupComboBox();
        initialPositionField.setText("750");
        statusLabel.setText("就绪 - 点击生成请求开始");
        
        // 初始化动画控制
        if (speedSlider != null) {
            speedSlider.setMin(1);
            speedSlider.setMax(10);
            speedSlider.setValue(5);
        }
    }
    
    private void setupAlgorithms() {
        algorithms = new ArrayList<>();
        algorithms.add(new FCFSAlgorithm());
        algorithms.add(new SSTFAlgorithm());
        algorithms.add(new SCANAlgorithm());
        algorithms.add(new CSCANAlgorithm());
    }
    
    private void setupTable() {
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        totalDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalSeekDistance"));
        averageTimeColumn.setCellValueFactory(new PropertyValueFactory<>("averageSeekTime"));
    }
    
    private void setupComboBox() {
        ObservableList<String> algorithmNames = FXCollections.observableArrayList();
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            algorithmNames.add(algorithm.getAlgorithmName());
        }
        algorithmComboBox.setItems(algorithmNames);
        algorithmComboBox.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void onGenerateRequests() {
        try {
            initialHeadPosition = Integer.parseInt(initialPositionField.getText());
            if (initialHeadPosition < 0 || initialHeadPosition >= 1500) {
                showAlert("错误", "初始位置必须在0-1499之间");
                return;
            }
            
            currentRequests = RequestGenerator.generateRequests();
            
            // 创建请求分布统计
            int range1Count = 0; // 0-499
            int range2Count = 0; // 500-999
            int range3Count = 0; // 1000-1499
            
            for (DiskRequest request : currentRequests) {
                int track = request.getTrackNumber();
                if (track < 500) range1Count++;
                else if (track < 1000) range2Count++;
                else range3Count++;
            }
            
            // 构建请求显示文本
            StringBuilder sb = new StringBuilder();
            sb.append("生成了 ").append(currentRequests.size()).append(" 个磁盘请求:\n");
            sb.append("分布情况: 0-499: ").append(range1Count).append("个 (")
              .append(String.format("%.1f%%", (range1Count * 100.0 / currentRequests.size())))
              .append("), 500-999: ").append(range2Count).append("个 (")
              .append(String.format("%.1f%%", (range2Count * 100.0 / currentRequests.size())))
              .append("), 1000-1499: ").append(range3Count).append("个 (")
              .append(String.format("%.1f%%", (range3Count * 100.0 / currentRequests.size())))
              .append(")\n\n");
            
            // 分组显示所有请求
            sb.append("0-499范围内的请求:\n");
            int count = 0;
            for (DiskRequest request : currentRequests) {
                if (request.getTrackNumber() < 500) {
                    sb.append(request.getTrackNumber()).append(" ");
                    count++;
                    if (count % 20 == 0) sb.append("\n"); // 每行显示20个请求
                }
            }
            
            sb.append("\n\n500-999范围内的请求:\n");
            count = 0;
            for (DiskRequest request : currentRequests) {
                if (request.getTrackNumber() >= 500 && request.getTrackNumber() < 1000) {
                    sb.append(request.getTrackNumber()).append(" ");
                    count++;
                    if (count % 20 == 0) sb.append("\n");
                }
            }
            
            sb.append("\n\n1000-1499范围内的请求:\n");
            count = 0;
            for (DiskRequest request : currentRequests) {
                if (request.getTrackNumber() >= 1000) {
                    sb.append(request.getTrackNumber()).append(" ");
                    count++;
                    if (count % 20 == 0) sb.append("\n");
                }
            }
            
            requestsTextArea.setText(sb.toString());
            statusLabel.setText("请求已生成 - 可以开始模拟");
            simulateButton.setDisable(false);
            
        } catch (NumberFormatException e) {
            showAlert("错误", "请输入有效的初始位置数字");
        }
    }
    
    @FXML
    private void onSimulate() {
        if (currentRequests == null || currentRequests.isEmpty()) {
            showAlert("错误", "请先生成磁盘请求");
            return;
        }
        
        String selectedAlgorithm = algorithmComboBox.getSelectionModel().getSelectedItem();
        if (selectedAlgorithm == null) {
            showAlert("错误", "请选择一个算法");
            return;
        }
        
        DiskSchedulingAlgorithm algorithm = null;
        for (DiskSchedulingAlgorithm algo : algorithms) {
            if (algo.getAlgorithmName().equals(selectedAlgorithm)) {
                algorithm = algo;
                break;
            }
        }
        
        if (algorithm != null) {
            statusLabel.setText("正在模拟 " + selectedAlgorithm + "...");
            simulateButton.setDisable(true);
            
            DiskSchedulingAlgorithm finalAlgorithm = algorithm;
            CompletableFuture.runAsync(() -> {
                SchedulingResult result = finalAlgorithm.schedule(currentRequests, initialHeadPosition);
                
                Platform.runLater(() -> {
                    // 保存结果用于动画
                    currentResult = result;
                    currentAnimationStep = 0;
                    
                    // 初始化画布
                    GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
                    drawCoordinateSystem(gc);
                    
                    // 添加结果到表格
                    addResultToTable(result);
                    
                    // 准备动画控制
                    prepareAnimation();
                    
                    statusLabel.setText("模拟完成 - 点击播放按钮开始动画");
                    simulateButton.setDisable(false);
                    if (playPauseButton != null) {
                        playPauseButton.setDisable(false);
                    }
                    if (stepButton != null) {
                        stepButton.setDisable(false);
                    }
                });
            });
        }
    }
    
    // 准备动画
    private void prepareAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        
        animationTimeline = new Timeline();
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        
        // 根据速度滑块设置动画速度
        double speed = speedSlider != null ? speedSlider.getValue() : 5.0;
        Duration duration = Duration.millis(1000.0 / speed);
        
        KeyFrame keyFrame = new KeyFrame(duration, event -> {
            if (currentResult != null && currentAnimationStep < currentResult.getSeekSequence().size()) {
                drawTrajectoryToStep(currentAnimationStep);
                currentAnimationStep++;
                
                // 动画结束时显示统计信息
                if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
                    drawStatistics(visualizationCanvas.getGraphicsContext2D(), currentResult);
                    animationTimeline.stop();
                    if (playPauseButton != null) {
                        playPauseButton.setText("播放");
                    }
                    animationPaused = true;
                }
            }
        });
        
        animationTimeline.getKeyFrames().add(keyFrame);
        animationPaused = true;
    }
    
    // 绘制到特定步骤
    private void drawTrajectoryToStep(int step) {
        if (currentResult == null || step < 0) return;
        
        List<Integer> sequence = currentResult.getSeekSequence();
        if (sequence.isEmpty()) return;
        
        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
        drawCoordinateSystem(gc);
        
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        
        double canvasWidth = visualizationCanvas.getWidth();
        double canvasHeight = visualizationCanvas.getHeight();
        
        // 绘制已完成的轨迹
        int maxStep = Math.min(step + 1, sequence.size());
        for (int i = 0; i < maxStep - 1; i++) {
            double x1 = 50 + (sequence.get(i) / 1500.0) * (canvasWidth - 100);
            double y1 = canvasHeight - 100 - (i / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (canvasWidth - 100);
            double y2 = canvasHeight - 100 - ((i + 1) / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            gc.strokeLine(x1, y1, x2, y2);
            
            gc.setFill(Color.RED);
            gc.fillOval(x1 - 2, y1 - 2, 4, 4);
        }
        
        // 绘制当前点
        if (maxStep > 0 && maxStep <= sequence.size()) {
            double x = 50 + (sequence.get(maxStep - 1) / 1500.0) * (canvasWidth - 100);
            double y = canvasHeight - 100 - ((maxStep - 1) / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            gc.setFill(Color.RED);
            gc.fillOval(x - 3, y - 3, 6, 6);
        }
        
        // 显示当前步骤信息
        gc.setFill(Color.BLACK);
        gc.fillText("算法: " + currentResult.getAlgorithmName(), 60, 30);
        gc.fillText("当前步骤: " + maxStep + "/" + sequence.size(), 200, 30);
        
        // 如果是最后一步，显示完整统计信息
        if (maxStep >= sequence.size()) {
            drawStatistics(gc, currentResult);
        }
    }
    
    // 播放/暂停动画
    @FXML
    private void onPlayPause() {
        if (currentResult == null) {
            showAlert("错误", "请先模拟一个算法");
            return;
        }
        
        if (animationPaused) {
            // 如果动画已结束，重新开始
            if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
                currentAnimationStep = 0;
            }
            
            // 更新动画速度
            if (animationTimeline != null) {
                animationTimeline.stop();
                prepareAnimation();
            }
            
            animationTimeline.play();
            animationPaused = false;
            if (playPauseButton != null) {
                playPauseButton.setText("暂停");
            }
        } else {
            animationTimeline.pause();
            animationPaused = true;
            if (playPauseButton != null) {
                playPauseButton.setText("播放");
            }
        }
    }
    
    // 单步执行动画
    @FXML
    private void onStep() {
        if (currentResult == null) {
            showAlert("错误", "请先模拟一个算法");
            return;
        }
        
        // 暂停当前动画
        if (animationTimeline != null && !animationPaused) {
            animationTimeline.pause();
            animationPaused = true;
            if (playPauseButton != null) {
                playPauseButton.setText("播放");
            }
        }
        
        // 如果动画已结束，重新开始
        if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
            currentAnimationStep = 0;
        }
        
        // 绘制下一步
        drawTrajectoryToStep(currentAnimationStep);
        currentAnimationStep++;
    }
    
    @FXML
    private void onSimulateAll() {
        if (currentRequests == null || currentRequests.isEmpty()) {
            showAlert("错误", "请先生成磁盘请求");
            return;
        }
        
        statusLabel.setText("正在模拟所有算法...");
        simulateButton.setDisable(true);
        generateButton.setDisable(true);
        
        CompletableFuture.runAsync(() -> {
            List<SchedulingResult> results = new ArrayList<>();
            
            for (DiskSchedulingAlgorithm algorithm : algorithms) {
                SchedulingResult result = algorithm.schedule(currentRequests, initialHeadPosition);
                results.add(result);
            }
            
            Platform.runLater(() -> {
                resultsTable.getItems().clear();
                for (SchedulingResult result : results) {
                    addResultToTable(result);
                }
                
                SchedulingResult bestResult = results.stream()
                    .min((r1, r2) -> Integer.compare(r1.getTotalSeekDistance(), r2.getTotalSeekDistance()))
                    .orElse(results.get(0));
                
                visualizeResult(bestResult);
                
                statusLabel.setText("所有算法模拟完成");
                simulateButton.setDisable(false);
                generateButton.setDisable(false);
            });
        });
    }
    
    private void visualizeResult(SchedulingResult result) {
        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
        
        drawCoordinateSystem(gc);
        drawTrajectory(gc, result);
        drawStatistics(gc, result);
    }
    
    private void drawCoordinateSystem(GraphicsContext gc) {
        double canvasWidth = visualizationCanvas.getWidth();
        double canvasHeight = visualizationCanvas.getHeight();
        
        // 绘制背景
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        // 设置坐标轴
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(2);
        gc.strokeLine(50, canvasHeight - 50, canvasWidth - 30, canvasHeight - 50); // X轴
        gc.strokeLine(50, 50, 50, canvasHeight - 50); // Y轴
        
        // 添加坐标轴标签
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(new javafx.scene.text.Font("Microsoft YaHei", 14));
        gc.fillText("磁道号", canvasWidth / 2, canvasHeight - 20);
        
        // 旋转文字绘制Y轴标签
        gc.save();
        gc.translate(20, canvasHeight / 2);
        gc.rotate(-90);
        gc.fillText("时间/步骤", 0, 0);
        gc.restore();
        
        // 绘制网格背景
        gc.setStroke(Color.web("#ecf0f1"));
        gc.setLineWidth(0.5);
        
        // 水平网格线
        for (int i = 0; i <= 1500; i += 150) {
            double x = 50 + (i / 1500.0) * (canvasWidth - 80);
            gc.strokeLine(x, 50, x, canvasHeight - 50);
        }
        
        // 垂直网格线
        int verticalLines = 10;
        for (int i = 0; i < verticalLines; i++) {
            double y = 50 + (i * (canvasHeight - 100) / verticalLines);
            gc.strokeLine(50, y, canvasWidth - 30, y);
        }
        
        // 主刻度 - 每300个磁道一个刻度
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(1.5);
        gc.setFont(new javafx.scene.text.Font("Microsoft YaHei", 12));
        for (int i = 0; i <= 1500; i += 300) {
            double x = 50 + (i / 1500.0) * (canvasWidth - 80);
            gc.strokeLine(x, canvasHeight - 50, x, canvasHeight - 40);
            gc.fillText(String.valueOf(i), x - 15, canvasHeight - 25);
        }
        
        // 次刻度 - 每150个磁道一个小刻度
        gc.setLineWidth(0.8);
        for (int i = 150; i < 1500; i += 150) {
            if (i % 300 != 0) { // 避免与主刻度重复
                double x = 50 + (i / 1500.0) * (canvasWidth - 80);
                gc.strokeLine(x, canvasHeight - 50, x, canvasHeight - 45);
                gc.fillText(String.valueOf(i), x - 10, canvasHeight - 25);
            }
        }
    }
    
    private void drawTrajectory(GraphicsContext gc, SchedulingResult result) {
        List<Integer> sequence = result.getSeekSequence();
        if (sequence.size() <= 1) return;
        
        double canvasWidth = visualizationCanvas.getWidth();
        double canvasHeight = visualizationCanvas.getHeight();
        
        // 绘制轨迹线
        gc.setStroke(Color.web("#3498db"));
        gc.setLineWidth(2.5);
        
        for (int i = 0; i < sequence.size() - 1; i++) {
            double x1 = 50 + (sequence.get(i) / 1500.0) * (canvasWidth - 80);
            double y1 = canvasHeight - 100 - (i / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (canvasWidth - 80);
            double y2 = canvasHeight - 100 - ((i + 1) / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            gc.strokeLine(x1, y1, x2, y2);
        }
        
        // 绘制轨迹点
        for (int i = 0; i < sequence.size(); i++) {
            double x = 50 + (sequence.get(i) / 1500.0) * (canvasWidth - 80);
            double y = canvasHeight - 100 - (i / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            // 绘制点的阴影效果
            gc.setFill(Color.web("#34495e", 0.3));
            gc.fillOval(x - 4, y - 4 + 2, 8, 8);
            
            // 绘制实际点
            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(x - 4, y - 4, 8, 8);
        }
        
        // 标记起始点
        double startX = 50 + (sequence.get(0) / 1500.0) * (canvasWidth - 80);
        double startY = canvasHeight - 100 - (0 / (double)(sequence.size() - 1)) * (canvasHeight - 150);
        gc.setFill(Color.web("#2ecc71"));
        gc.fillOval(startX - 6, startY - 6, 12, 12);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(startX - 6, startY - 6, 12, 12);
    }
    
    private void drawStatistics(GraphicsContext gc, SchedulingResult result) {
        double canvasWidth = visualizationCanvas.getWidth();
        
        // 绘制半透明背景面板
        gc.setFill(Color.web("#ffffff", 0.85));
        gc.fillRoundRect(canvasWidth - 250, 10, 220, 100, 10, 10);
        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(canvasWidth - 250, 10, 220, 100, 10, 10);
        
        // 设置文本样式
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(new javafx.scene.text.Font("Microsoft YaHei", 14));
        gc.fillText("算法: " + result.getAlgorithmName(), canvasWidth - 230, 35);
        
        gc.setFont(new javafx.scene.text.Font("Microsoft YaHei", 13));
        gc.fillText("总寻道距离: " + result.getTotalSeekDistance(), canvasWidth - 230, 65);
        gc.fillText("平均寻道时间: " + String.format("%.2f", result.getAverageSeekTime()), canvasWidth - 230, 95);
    }
    
    private void addResultToTable(SchedulingResult result) {
        resultsTable.getItems().add(result);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void onReset() {
        // 停止动画
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        
        currentRequests = null;
        currentResult = null;
        currentAnimationStep = 0;
        requestsTextArea.clear();
        
        if (visualizationCanvas != null) {
            GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
        }
        
        if (resultsTable != null) {
            resultsTable.getItems().clear();
        }
        
        initialPositionField.setText("750");
        statusLabel.setText("已重置 - 点击生成请求开始");
        simulateButton.setDisable(true);
        generateButton.setDisable(false);
        
        if (playPauseButton != null) {
            playPauseButton.setDisable(true);
            playPauseButton.setText("播放");
        }
        
        if (stepButton != null) {
            stepButton.setDisable(true);
        }
        
        if (algorithmComboBox != null && !algorithmComboBox.getItems().isEmpty()) {
            algorithmComboBox.getSelectionModel().selectFirst();
        }
        
        animationPaused = true;
    }
}