package i.valerii_timakov.serial_monitor.serial_monitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SerialMonitorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SerialMonitorApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 640);
        stage.setTitle("Monitor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
