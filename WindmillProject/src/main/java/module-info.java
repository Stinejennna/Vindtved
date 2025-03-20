module org.example.windmillproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.sql;


    opens org.example.windmillproject to javafx.fxml;
    exports org.example.windmillproject;
}