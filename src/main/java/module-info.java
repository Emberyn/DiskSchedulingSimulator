module org.example.diskschedulingsimulator {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.diskschedulingsimulator to javafx.fxml;
    exports org.example.diskschedulingsimulator;
}