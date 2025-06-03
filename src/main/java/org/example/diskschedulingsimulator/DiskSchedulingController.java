package org.example.diskschedulingsimulator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

// 确保这些导入存在且没有错误
import org.example.diskschedulingsimulator.DiskRequest; 
import org.example.diskschedulingsimulator.RequestGenerator;
import org.example.diskschedulingsimulator.DiskSchedulingAlgorithm;
import org.example.diskschedulingsimulator.FCFSAlgorithm;
import org.example.diskschedulingsimulator.SSTFAlgorithm;
import org.example.diskschedulingsimulator.SCANAlgorithm;
import org.example.diskschedulingsimulator.CSCANAlgorithm;
import org.example.diskschedulingsimulator.SchedulingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiskSchedulingController {
    
    @FXML private TextField initialPositionField;
    @FXML private Button generateButton;
    @FXML private Button simulateButton;
    @FXML private Button simulateAllButton;
    @FXML private Button resetButton;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private Canvas visualizationCanvas;
    @FXML private TableView<SchedulingResult> resultsTable;
    @FXML private TableColumn<SchedulingResult, String> algorithmColumn;
    @FXML private TableColumn<SchedulingResult, Integer> totalDistanceColumn;
    @FXML private TableColumn<SchedulingResult, Double> averageTimeColumn;
    @FXML private TextArea requestsTextArea;
    @FXML private Label statusLabel;
    
    private List<DiskRequest> currentRequests;
    private int initialHeadPosition;
    private List<DiskSchedulingAlgorithm> algorithms;
    
    @FXML
    public void initialize() {
        setupAlgorithms();
        setupTable();
        setupComboBox();
        initialPositionField.setText("750");
        statusLabel.setText("就绪 - 点击生成请求开始");
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
            
            StringBuilder sb = new StringBuilder();
            sb.append("生成了 ").append(currentRequests.size()).append(" 个磁盘请求:\n");
            for (int i = 0; i < Math.min(10, currentRequests.size()); i++) {
                sb.append(currentRequests.get(i).getTrackNumber()).append(" ");
            }
            if (currentRequests.size() > 10) {
                sb.append("\n... 还有 ").append(currentRequests.size() - 10).append(" 个请求");
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
                    visualizeResult(result);
                    addResultToTable(result);
                    statusLabel.setText("模拟完成 - " + selectedAlgorithm);
                    simulateButton.setDisable(false);
                });
            });
        }
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
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(50, canvasHeight - 50, canvasWidth - 50, canvasHeight - 50);
        gc.strokeLine(50, 50, 50, canvasHeight - 50);
        
        // 简化刻度
        for (int i = 0; i <= 1500; i += 500) {
            double x = 50 + (i / 1500.0) * (canvasWidth - 100);
            gc.strokeLine(x, canvasHeight - 50, x, canvasHeight - 40);
            gc.fillText(String.valueOf(i), x - 10, canvasHeight - 25);
        }
    }
    
    private void drawTrajectory(GraphicsContext gc, SchedulingResult result) {
        List<Integer> sequence = result.getSeekSequence();
        if (sequence.size() <= 1) return;
        
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        
        double canvasWidth = visualizationCanvas.getWidth();
        double canvasHeight = visualizationCanvas.getHeight();
        
        for (int i = 0; i < sequence.size() - 1; i++) {
            double x1 = 50 + (sequence.get(i) / 1500.0) * (canvasWidth - 100);
            double y1 = canvasHeight - 100 - (i / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            double x2 = 50 + (sequence.get(i + 1) / 1500.0) * (canvasWidth - 100);
            double y2 = canvasHeight - 100 - ((i + 1) / (double)(sequence.size() - 1)) * (canvasHeight - 150);
            
            gc.strokeLine(x1, y1, x2, y2);
            
            gc.setFill(Color.RED);
            gc.fillOval(x1 - 2, y1 - 2, 4, 4);
        }
        
        // 最后一个点
        double lastX = 50 + (sequence.get(sequence.size()-1) / 1500.0) * (canvasWidth - 100);
        double lastY = canvasHeight - 100 - ((sequence.size()-1) / (double)(sequence.size() - 1)) * (canvasHeight - 150);
        gc.setFill(Color.RED);
        gc.fillOval(lastX - 2, lastY - 2, 4, 4);
    }
    
    private void drawStatistics(GraphicsContext gc, SchedulingResult result) {
        gc.setFill(Color.BLACK);
        gc.fillText("算法: " + result.getAlgorithmName(), 60, 30);
        gc.fillText("总寻道距离: " + result.getTotalSeekDistance(), 200, 30);
        gc.fillText("平均寻道时间: " + String.format("%.2f", result.getAverageSeekTime()), 380, 30);
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
        currentRequests = null;
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
        
        if (algorithmComboBox != null && !algorithmComboBox.getItems().isEmpty()) {
            algorithmComboBox.getSelectionModel().selectFirst();
        }
    }
}