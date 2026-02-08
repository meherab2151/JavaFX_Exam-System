module com.example.exam_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.exam_system to javafx.fxml;
    exports com.example.exam_system;
}