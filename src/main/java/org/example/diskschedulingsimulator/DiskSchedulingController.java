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
    @FXML private Button quickPreviewButton;

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
            sb.append("=== 磁盘请求生成报告 ===\n");
            sb.append("总请求数: ").append(currentRequests.size()).append("\n");
            sb.append("初始磁头位置: ").append(initialHeadPosition).append("\n\n");

            sb.append("=== 分布统计 ===\n");
            sb.append("0-499范围: ").append(range1Count).append("个 (").append(String.format("%.1f%%", (range1Count * 100.0 / currentRequests.size()))).append(")\n");
            sb.append("500-999范围: ").append(range2Count).append("个 (").append(String.format("%.1f%%", (range2Count * 100.0 / currentRequests.size()))).append(")\n");
            sb.append("1000-1499范围: ").append(range3Count).append("个 (").append(String.format("%.1f%%", (range3Count * 100.0 / currentRequests.size()))).append("\n\n");

            sb.append("=== 请求序列（按生成顺序） ===\n");
            for (int i = 0; i < currentRequests.size(); i++) {
                sb.append(String.format("%3d", currentRequests.get(i).getTrackNumber()));
                if ((i + 1) % 20 == 0) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
            }
            if (currentRequests.size() % 20 != 0) {
                sb.append("\n");
            }

            requestsTextArea.setText(sb.toString());
            statusLabel.setText("请求已生成 - 可以开始模拟");
            simulateButton.setDisable(false);
            if (quickPreviewButton != null) {
                quickPreviewButton.setDisable(false);
            }
        } catch (NumberFormatException e) {
            showAlert("输入错误", "请输入有效的初始磁头位置");
        }
    }

    @FXML
    private void onQuickPreview() {
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
            statusLabel.setText("正在生成 " + selectedAlgorithm + " 预览图...");

            // 直接在UI线程中执行，快速生成
            SchedulingResult result = algorithm.schedule(currentRequests, initialHeadPosition);

            // 清除画布并绘制完整结果
            GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
            drawCoordinateSystem(gc);
            drawTrajectory(gc, result);
            drawStatistics(gc, result);

            // 添加结果到表格
            addResultToTable(result);

            statusLabel.setText("预览完成 - " + selectedAlgorithm + " 算法");
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

    // === 新增方法 ===
    @FXML
    private void onSimulateAll() {
        // 这里可以添加模拟所有算法的逻辑
        // 目前只需空实现来解决FXML绑定问题
        System.out.println("onSimulateAll called");
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
            double y = sequence.size() > 1 ?
                    canvasHeight - 100 - ((maxStep - 1) / (double)(sequence.size() - 1)) * (canvasHeight - 150) :
                    canvasHeight - 100; // 单点情况
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

    // 绘制坐标系
    private void drawCoordinateSystem(GraphicsContext gc) {
        double width = visualizationCanvas.getWidth();
        double height = visualizationCanvas.getHeight();

        // 清空画布
        gc.clearRect(0, 0, width, height);

        // 绘制坐标轴
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // X轴 (磁道号)
        gc.strokeLine(50, height - 50, width - 50, height - 50);
        // Y轴 (请求顺序)
        gc.strokeLine(50, height - 50, 50, 50);

        // 绘制刻度
        gc.setFill(Color.BLACK);
        // X轴刻度
        for (int i = 0; i <= 1500; i += 300) {
            double x = 50 + (i / 1500.0) * (width - 100);
            gc.strokeLine(x, height - 50, x, height - 45);
            gc.fillText(String.valueOf(i), x - 10, height - 30);
        }

        // Y轴刻度
        if (currentResult != null && !currentResult.getSeekSequence().isEmpty()) {
            int steps = currentResult.getSeekSequence().size();
            for (int i = 0; i <= 10; i++) {
                double y = height - 50 - (i / 10.0) * (height - 100);
                gc.strokeLine(50, y, 55, y);
                gc.fillText(String.valueOf(i * steps / 10), 30, y + 5);
            }
        }

        // 轴标签
        gc.fillText("磁道号", width - 40, height - 30);
        gc.fillText("请求顺序", 20, 40);
    }

    // 绘制完整轨迹
    private void drawTrajectory(GraphicsContext gc, SchedulingResult result) {
        List<Integer> sequence = result.getSeekSequence();
        if (sequence.isEmpty()) return;

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);

        double width = visualizationCanvas.getWidth();
        double height = visualizationCanvas.getHeight();

        for (int i = 0; i < sequence.size() - 1; i++) {
            double x1 = 50 + (sequence.get(i) / 1500.0) * (width - 100);
            double y1 = height - 100 - (i / (double)(sequence.size() - 1)) * (height - 150);

            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (width - 100);
            double y2 = height - 100 - ((i + 1) / (double)(sequence.size() - 1)) * (height - 150);

            gc.strokeLine(x1, y1, x2, y2);

            // 绘制点
            gc.setFill(Color.RED);
            gc.fillOval(x1 - 2, y1 - 2, 4, 4);
        }

        // 绘制最后一个点
        if (!sequence.isEmpty()) {
            double x = 50 + (sequence.get(sequence.size() - 1) / 1500.0) * (width - 100);
            double y = height - 100 - ((sequence.size() - 1) / (double)(sequence.size() - 1)) * (height - 150);
            gc.setFill(Color.RED);
            gc.fillOval(x - 3, y - 3, 6, 6);
        }

        drawStatistics(gc, result);
    }

    // 绘制统计信息
    private void drawStatistics(GraphicsContext gc, SchedulingResult result) {
        gc.setFill(Color.BLACK);
        gc.fillText("算法: " + result.getAlgorithmName(), 60, 30);
        gc.fillText("总寻道距离: " + result.getTotalSeekDistance(), 200, 30);
        gc.fillText("平均寻道时间: " + String.format("%.2f", result.getAverageSeekTime()), 350, 30);
    }

    // 添加结果到表格
    private void addResultToTable(SchedulingResult result) {
        resultsTable.getItems().add(result);
    }

    // 显示警告对话框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onPlayPause() {
        if (animationTimeline == null) return;

        if (animationPaused) {
            animationTimeline.play();
            playPauseButton.setText("暂停");
        } else {
            animationTimeline.pause();
            playPauseButton.setText("播放");
        }
        animationPaused = !animationPaused;
    }

    @FXML
    private void onStep() {
        if (currentResult == null) return;

        if (currentAnimationStep < currentResult.getSeekSequence().size()) {
            drawTrajectoryToStep(currentAnimationStep);
            currentAnimationStep++;
        }

        // 如果是最后一步，显示完整统计信息
        if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
            drawStatistics(visualizationCanvas.getGraphicsContext2D(), currentResult);
        }
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
        animationPaused = true;

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

        // 重置按钮状态
        simulateButton.setDisable(true);
        generateButton.setDisable(false);

        if (quickPreviewButton != null) {
            quickPreviewButton.setDisable(true);
        }

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