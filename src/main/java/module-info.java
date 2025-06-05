/**
 * 该模块定义了磁盘调度模拟器的依赖和访问权限。
 * 它指定了该模块需要 JavaFX 的控件和 FXML 功能，并定义了哪些包可以被其他模块访问。
 */
module org.example.diskschedulingsimulator {
    // 声明该模块依赖于 JavaFX 的控件库
    requires javafx.controls;
    // 声明该模块依赖于 JavaFX 的 FXML 库
    requires javafx.fxml;

    // 允许 JavaFX FXML 访问 org.example.diskschedulingsimulator 包中的类
    opens org.example.diskschedulingsimulator to javafx.fxml;
    // 导出 org.example.diskschedulingsimulator 包，使得其他模块可以访问该包中的公共类
    exports org.example.diskschedulingsimulator;
}