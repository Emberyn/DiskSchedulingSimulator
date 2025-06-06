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

/**
 * 磁盘调度模拟器的控制器类
 * 负责处理用户界面交互、算法执行和结果可视化
 */
public class DiskSchedulingController {

    // FXML注入的UI组件
    @FXML private TextField initialPositionField;         // 初始磁头位置输入框
    @FXML private Button generateButton;                 // 生成请求按钮
    @FXML private Button simulateButton;                 // 模拟算法按钮
    @FXML private ComboBox<String> algorithmComboBox;    // 算法选择下拉框
    @FXML private Canvas visualizationCanvas;            // 可视化结果的画布
    @FXML private TableView<SchedulingResult> resultsTable; // 显示算法结果的表格
    @FXML private TableColumn<SchedulingResult, String> algorithmColumn; // 算法名列
    @FXML private TableColumn<SchedulingResult, Integer> totalDistanceColumn; // 总寻道距离列
    @FXML private TableColumn<SchedulingResult, Double> averageTimeColumn; // 平均寻道时间列
    @FXML private TextArea requestsTextArea;             // 显示请求信息的文本区域
    @FXML private Label statusLabel;                     // 状态信息标签
    @FXML private Button playPauseButton;                // 控制动画播放/暂停的按钮
    @FXML private Button stepButton;                     // 单步执行按钮
    @FXML private Slider speedSlider;                    // 动画速度控制滑块
    @FXML private Button quickPreviewButton;             // 快速预览按钮

    // 核心数据
    private List<DiskRequest> currentRequests;           // 当前生成的磁盘请求列表
    private int initialHeadPosition;                     // 初始磁头位置
    private List<DiskSchedulingAlgorithm> algorithms;    // 支持的磁盘调度算法列表

    // 动画控制相关变量
    private Timeline animationTimeline;                  // 控制动画播放的时间线
    private int currentAnimationStep = 0;                // 当前动画步骤
    private SchedulingResult currentResult;              // 当前算法模拟结果
    private boolean animationPaused = true;              // 动画暂停状态

    /**
     * 初始化方法，在FXML加载后自动调用
     * 设置UI组件和初始状态
     */
    @FXML
    public void initialize() {
        setupAlgorithms();           // 初始化支持的算法列表
        setupTable();                // 设置结果表格
        setupComboBox();             // 设置算法选择下拉框
        initialPositionField.setText("750");
        statusLabel.setText("就绪 - 点击生成请求开始");

        // 初始化动画速度控制
        if (speedSlider != null) {
            speedSlider.setMin(1);
            speedSlider.setMax(100);
            speedSlider.setValue(50);
            
            // 添加监听器，当滑块值变化时更新动画速度
            speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                updateAnimationSpeed();
            });
        }
    }

    /**
     * 设置支持的磁盘调度算法
     * 添加四种主要算法：FCFS、SSTF、SCAN和C-SCAN
     */
    private void setupAlgorithms() {
        algorithms = new ArrayList<>();
        algorithms.add(new FCFSAlgorithm());
        algorithms.add(new SSTFAlgorithm());
        algorithms.add(new SCANAlgorithm());
        algorithms.add(new CSCANAlgorithm());
    }

    /**
     * 设置结果表格的列属性
     * 将表格列与SchedulingResult对象的属性绑定
     */
    private void setupTable() {
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        totalDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalSeekDistance"));
        averageTimeColumn.setCellValueFactory(new PropertyValueFactory<>("averageSeekTime"));
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * 设置算法选择下拉框
     * 将所有支持的算法添加到下拉框中
     */
    private void setupComboBox() {
        ObservableList<String> algorithmNames = FXCollections.observableArrayList();

        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            algorithmNames.add(algorithm.getAlgorithmName());
        }

        algorithmComboBox.setItems(algorithmNames);
        algorithmComboBox.getSelectionModel().selectFirst();
    }

    /**
     * 处理生成请求按钮点击事件
     * 生成磁盘请求并显示相关信息
     */
    @FXML
    private void onGenerateRequests() {
        try {
            // 从输入框获取初始磁头位置
            initialHeadPosition = Integer.parseInt(initialPositionField.getText());

            // 验证输入的初始位置是否在有效范围内
            if (initialHeadPosition < 0 || initialHeadPosition >= 1500) {
                showAlert("错误", "初始位置必须在0-1499之间");
                return;
            }

            // 生成磁盘请求
            currentRequests = RequestGenerator.generateRequests();

            // 统计不同范围的请求数量
            int range1Count = 0;
            int range2Count = 0;
            int range3Count = 0;

            // 遍历请求列表，统计各范围的请求数
            for (DiskRequest request : currentRequests) {
                int track = request.getTrackNumber();
                if (track < 500) range1Count++;
                else if (track < 1000) range2Count++;
                else range3Count++;
            }

            // 构建请求信息文本
            StringBuilder sb = new StringBuilder();
            sb.append("=== 磁盘请求生成报告 ===\n");
            sb.append("总请求数: ").append(currentRequests.size()).append("\n");
            sb.append("初始磁头位置: ").append(initialHeadPosition).append("\n\n");

            sb.append("=== 分布统计 ===\n");
            sb.append("0-499范围: ").append(range1Count).append("个 (").append(String.format("%.1f%%", (range1Count * 100.0 / currentRequests.size()))).append(")\n");
            sb.append("500-999范围: ").append(range2Count).append("个 (").append(String.format("%.1f%%", (range2Count * 100.0 / currentRequests.size()))).append(")\n");
            sb.append("1000-1499范围: ").append(range3Count).append("个 (").append(String.format("%.1f%%", (range3Count * 100.0 / currentRequests.size()))).append(")\n\n");

            sb.append("=== 请求序列（按生成顺序） ===\n");
            // 格式化显示请求序列，每行显示10个请求
            for (int i = 0; i < currentRequests.size(); i++) {
                sb.append(String.format("%3d", currentRequests.get(i).getTrackNumber()));
                if ((i + 1) % 10 == 0) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
            }
            if (currentRequests.size() % 10 != 0) {
                sb.append("\n");  // 确保最后一行换行
            }

            // 在文本区域显示请求信息
            requestsTextArea.setText(sb.toString());
            statusLabel.setText("请求已生成 - 可以开始模拟");

            // 启用模拟按钮和快速预览按钮
            simulateButton.setDisable(false);
            if (quickPreviewButton != null) {
                quickPreviewButton.setDisable(false);
            }
        } catch (NumberFormatException e) {
            // 处理输入非数字的情况
            showAlert("输入错误", "请输入有效的初始磁头位置");
        }
    }

    /**
     * 处理快速预览按钮点击事件
     * 快速显示选定算法的模拟结果，不使用动画
     */
    @FXML
    private void onQuickPreview() {
        // 检查是否已生成请求
        if (currentRequests == null || currentRequests.isEmpty()) {
            showAlert("错误", "请先生成磁盘请求");
            return;
        }

        // 获取选中的算法
        String selectedAlgorithm = algorithmComboBox.getSelectionModel().getSelectedItem();
        if (selectedAlgorithm == null) {
            showAlert("错误", "请选择一个算法");
            return;
        }

        // 查找对应的算法实现
        DiskSchedulingAlgorithm algorithm = null;
        for (DiskSchedulingAlgorithm algo : algorithms) {
            if (algo.getAlgorithmName().equals(selectedAlgorithm)) {
                algorithm = algo;
                break;
            }
        }

        if (algorithm != null) {
            statusLabel.setText("正在生成 " + selectedAlgorithm + " 预览图...");

            // 直接在UI线程执行算法（快速预览不需要异步）
            SchedulingResult result = algorithm.schedule(currentRequests, initialHeadPosition);

            // 清空画布并绘制结果
            GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
            drawCoordinateSystem(gc);     // 绘制坐标系
            drawTrajectory(gc, result);   // 绘制磁头移动轨迹
            drawStatistics(gc, result);   // 绘制统计信息i

            // 将结果添加到表格中
            addResultToTable(result);

            statusLabel.setText("预览完成 - " + selectedAlgorithm + " 算法");
        }
    }

    /**
     * 处理模拟按钮点击事件
     * 异步执行选定的算法并准备动画展示
     */
    @FXML
    private void onSimulate() {
        // 检查是否已生成请求
        if (currentRequests == null || currentRequests.isEmpty()) {
            showAlert("错误", "请先生成磁盘请求");
            return;
        }

        // 获取选中的算法
        String selectedAlgorithm = algorithmComboBox.getSelectionModel().getSelectedItem();
        if (selectedAlgorithm == null) {
            showAlert("错误", "请选择一个算法");
            return;
        }

        // 查找对应的算法实现
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

            // 使用CompletableFuture在后台线程执行算法，避免阻塞UI
            DiskSchedulingAlgorithm finalAlgorithm = algorithm;
            CompletableFuture.runAsync(() -> {
                SchedulingResult result = finalAlgorithm.schedule(currentRequests, initialHeadPosition);

                // 在UI线程更新结果和准备动画
                Platform.runLater(() -> {
                    // 保存结果用于动画展示
                    currentResult = result;
                    currentAnimationStep = 0;

                    // 初始化画布
                    GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
                    drawCoordinateSystem(gc);

                    // 将结果添加到表格中
                    addResultToTable(result);

                    // 准备动画控制
                    prepareAnimation();

                    statusLabel.setText("模拟完成 - 点击播放按钮开始动画");
                    simulateButton.setDisable(false);  // 重新启用模拟按钮

                    // 启用动画控制按钮
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

    /**
     * 准备动画
     * 设置动画时间线和速度
     */
    private void prepareAnimation() {
        // 停止任何正在运行的动画
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        // 创建新的动画时间线
        animationTimeline = new Timeline();
        animationTimeline.setCycleCount(Timeline.INDEFINITE);  // 无限循环直到手动停止

        // 根据速度滑块设置动画速度
        double speed = speedSlider != null ? speedSlider.getValue(): 50.0;
        Duration duration = Duration.millis(1000.0 / speed);  // 计算每帧持续时间

        // 定义每帧的动画逻辑
        KeyFrame keyFrame = new KeyFrame(duration, event -> {
            if (currentResult != null && currentAnimationStep < currentResult.getSeekSequence().size()) {
                drawTrajectoryToStep(currentAnimationStep);  // 绘制当前步骤
                currentAnimationStep++;  // 增加步骤计数

                // 动画结束时显示统计信息并停止动画
                if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
                    drawStatistics(visualizationCanvas.getGraphicsContext2D(), currentResult);
                    animationTimeline.stop();
                    if (playPauseButton != null) {
                        playPauseButton.setText("播放");  // 恢复按钮文本
                    }
                    animationPaused = true;
                }
            }
        });

        animationTimeline.getKeyFrames().add(keyFrame);
        animationPaused = true;  // 初始状态为暂停
    }

    /**
     * 绘制到指定步骤的轨迹
     * @param step 要绘制的步骤
     */
    private void drawTrajectoryToStep(int step) {
        if (currentResult == null || step < 0) return;

        List<Integer> sequence = currentResult.getSeekSequence();
        if (sequence.isEmpty()) return;

        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, visualizationCanvas.getWidth(), visualizationCanvas.getHeight());
        drawCoordinateSystem(gc);  // 绘制坐标系

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);

        double canvasWidth = visualizationCanvas.getWidth();
        double canvasHeight = visualizationCanvas.getHeight();

        // 绘制已完成的轨迹
        int maxStep = Math.min(step + 1, sequence.size());
        for (int i = 0; i < maxStep - 1; i++) {
            // 计算当前点和下一点的坐标
            double x1 = 50 + (sequence.get(i) / 1500.0) * (canvasWidth - 100);
            double y1 = canvasHeight - 50 - (i / (double)(sequence.size() - 1)) * (canvasHeight - 100);

            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (canvasWidth - 100);
            double y2 = canvasHeight - 50 - ((i + 1) / (double)(sequence.size() - 1)) * (canvasHeight - 100);

            // 绘制轨迹线
            gc.strokeLine(x1, y1, x2, y2);

            // 绘制轨迹点
            gc.setFill(Color.RED);
            gc.fillOval(x1 - 2, y1 - 2, 4, 4);
        }

        // 绘制当前位置点
        if (maxStep > 0 && maxStep <= sequence.size()) {
            double x = 50 + (sequence.get(maxStep - 1) / 1500.0) * (canvasWidth - 100);
            double y = sequence.size() > 1 ?
                    canvasHeight - 50 - ((maxStep - 1) / (double)(sequence.size() - 1)) * (canvasHeight - 100) :
                    canvasHeight - 50; // 单点情况
            gc.setFill(Color.RED);
            gc.fillOval(x - 3, y - 3, 6, 6);  // 当前点稍大一些
        }

        // 显示当前步骤信息
        gc.setFill(Color.BLACK);
        gc.fillText("算法: " + currentResult.getAlgorithmName(), 230, 20);
        gc.fillText("当前步骤: " + maxStep + "/" + sequence.size(), 300, 20);

        // 如果是最后一步，显示完整统计信息
        if (maxStep >= sequence.size()) {
            // 清除文本区域（根据实际文本位置调整参数）
            gc.clearRect(200, 5, 400, 25);
            drawStatistics(gc, currentResult);
        }
    }

    /**
     * 绘制坐标系
     * @param gc 图形上下文
     */
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

        // X轴刻度 (磁道号)
        for (int i = 0; i <= 1500; i += 300) {
            double x = 50 + (i / 1500.0) * (width - 100);
            gc.strokeLine(x, height - 50, x, height - 45);  // 绘制刻度线
            gc.fillText(String.valueOf(i), x - 10, height - 30);  // 绘制刻度值
        }

        // Y轴刻度 (请求顺序)
        if (currentResult != null && !currentResult.getSeekSequence().isEmpty()) {
            int steps = currentResult.getSeekSequence().size();
            for (int i = 0; i <= 10; i++) {
                double y = height - 50 - (i / 10.0) * (height - 100);
                gc.strokeLine(50, y, 55, y);  // 绘制刻度线
                gc.fillText(String.valueOf(i * steps / 10), 28, y + 5);  // 绘制刻度值
            }
        }

        // 绘制轴标签
        gc.fillText("磁道号", width - 60, height - 10);
        gc.fillText("请求顺序", 20, 40);
    }

    /**
     * 绘制完整的轨迹
     * @param gc 图形上下文
     * @param result 调度结果
     */
    private void drawTrajectory(GraphicsContext gc, SchedulingResult result) {
        List<Integer> sequence = result.getSeekSequence();
        if (sequence.isEmpty()) return;

        gc.setStroke(Color.BLUE);  // 设置轨迹线颜色
        gc.setLineWidth(2);        // 设置轨迹线宽度

        double width = visualizationCanvas.getWidth();
        double height = visualizationCanvas.getHeight();

        // 绘制完整轨迹
        for (int i = 0; i < sequence.size() - 1; i++) {
            // 计算当前点和下一点的坐标
            double x1 = 50 + (sequence.get(i) / 1500.0) * (width - 100);
            double y1 = height - 50 - (i / (double)(sequence.size() - 1)) * (height - 100);

            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (width - 100);
            double y2 = height - 50 - ((i + 1) / (double)(sequence.size() - 1)) * (height - 100);

            // 绘制轨迹线
            gc.strokeLine(x1, y1, x2, y2);

            // 绘制轨迹点
            gc.setFill(Color.RED);
            gc.fillOval(x1 - 2, y1 - 2, 4, 4);
        }

        // 绘制最后一个点
        if (!sequence.isEmpty()) {
            double x = 50 + (sequence.get(sequence.size() - 1) / 1500.0) * (width - 100);
            double y = height - 50 - ((sequence.size() - 1) / (double)(sequence.size() - 1)) * (height - 100);
            gc.setFill(Color.RED);
            gc.fillOval(x - 3, y - 3, 6, 6);  // 最后一点稍大一些
        }

        // 绘制统计信息
        drawStatistics(gc, result);
    }

    /**
     * 绘制统计信息
     * @param gc 图形上下文
     * @param result 调度结果
     */
    private void drawStatistics(GraphicsContext gc, SchedulingResult result) {
        gc.setFill(Color.BLACK);
        gc.fillText("算法: " + result.getAlgorithmName(), 150, 20);
        gc.fillText("总寻道距离: " + result.getTotalSeekDistance(), 250, 20);
        gc.fillText("平均寻道时间: " + String.format("%.2f", result.getAverageSeekTime()), 380, 20);
    }

    /**
     * 将结果添加到结果表格
     * @param result 调度结果
     */
    private void addResultToTable(SchedulingResult result) {
        resultsTable.getItems().add(result);
    }

    /**
     * 显示错误警告对话框
     * @param title 对话框标题
     * @param message 对话框内容
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 处理播放/暂停按钮点击事件
     * 控制动画的播放和暂停状态
     */
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

    /**
     * 处理单步执行按钮点击事件
     * 逐帧执行动画
     */
    @FXML
    private void onStep() {
        if (currentResult == null) return;

        if (currentAnimationStep < currentResult.getSeekSequence().size()) {
            drawTrajectoryToStep(currentAnimationStep);  // 绘制当前步骤
            currentAnimationStep++;  // 增加步骤计数
        }

        // 如果是最后一步，显示完整统计信息
        if (currentAnimationStep >= currentResult.getSeekSequence().size()) {
            drawStatistics(visualizationCanvas.getGraphicsContext2D(), currentResult);
        }
    }

    /**
     * 处理重置按钮点击事件
     * 重置所有状态和UI元素
     */
    @FXML
    private void onReset() {
        // 停止任何正在运行的动画
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        // 重置数据
        currentRequests = null;
        currentResult = null;
        currentAnimationStep = 0;
        animationPaused = true;

        // 清空文本区域
        requestsTextArea.clear();

        // 重置算法选择
        if (algorithmComboBox != null && !algorithmComboBox.getItems().isEmpty()) {
            algorithmComboBox.getSelectionModel().selectFirst();
        }

        animationPaused = true;  // 确保动画处于暂停状态
    }
    
    // 添加新方法：更新动画速度
    private void updateAnimationSpeed() {
        if (animationTimeline != null) {
            double speed = speedSlider.getValue();
            Duration duration = Duration.millis(1000.0 / speed);
            
            // 更新动画时间线的帧率
            KeyFrame keyFrame = animationTimeline.getKeyFrames().get(0);
            animationTimeline.stop();
            animationTimeline.getKeyFrames().clear();
            
            // 创建新的关键帧，保持原有的事件处理逻辑
            KeyFrame newKeyFrame = new KeyFrame(duration, keyFrame.getOnFinished());
            animationTimeline.getKeyFrames().add(newKeyFrame);
            
            // 如果动画正在播放，则继续播放
            if (!animationPaused) {
                animationTimeline.play();
            }
        }
    }
}