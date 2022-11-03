package i.valerii_timakov.serial_monitor.serial_monitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class SerialMonitorApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        log.info("Creating stage...");
        FXMLLoader fxmlLoader = new FXMLLoader(SerialMonitorApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 640);
        stage.setTitle("Monitor");
        stage.setScene(scene);
        SerialMonitorController controller = fxmlLoader.getController();
        stage.setOnHidden(e -> controller.closeCurrentPort());
        stage.show();
        log.info("Stage shown");
    }

    public static void main(String[] args) {
        try {
            log.info("Starting...");
            log.debug("Debug test...");
            launch();
            log.info("Exiting...");
        } catch (Throwable t) {
            log.fatal("Fatal application exception! Exiting...", t);
        }
    }
}
