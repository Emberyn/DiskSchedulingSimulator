package org.example.diskschedulingsimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("disk-scheduling-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 780);  // 增加窗口大小
        stage.setTitle("磁盘调度算法模拟器");
        stage.setScene(scene);
        stage.setMinWidth(1000);  // 增加最小宽度
        stage.setMinHeight(700);  // 增加最小高度
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}



