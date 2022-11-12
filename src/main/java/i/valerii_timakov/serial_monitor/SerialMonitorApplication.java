package i.valerii_timakov.serial_monitor;

import i.valerii_timakov.serial_monitor.controllers.SerialMonitorController;
import i.valerii_timakov.serial_monitor.utils.Log;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SerialMonitorApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Log.debug("Creating stage...");
            FXMLLoader fxmlLoader = new FXMLLoader(SerialMonitorApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 920, 640);
            stage.setTitle("Serial monitor");
            stage.setScene(scene);
            SerialMonitorController controller = fxmlLoader.getController();
            ServicesFactory servicesFactory = new ServicesFactory();
            controller.init(stage, servicesFactory);

            stage.setOnHidden(e -> {
                servicesFactory.getPortWrapperService().closeCurrentPort();
                servicesFactory.getSettingsService().save();
            });
            stage.show();
            Log.debug("Stage shown");
        } catch (Throwable t) {
            Log.error("Error starting application!", t);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fatal error ocured!");
            alert.setContentText("Error ocured ant application will be closed! Error: " + t.getMessage());
            alert.show();
        }
    }

    public static void main(String[] args) {
        try {
            Log.debug("Starting!");
            launch();
            Log.debug("Exiting...");
        } catch (Throwable t) {
            Log.error("Fatal application exception! Exiting...", t);
        } finally {
            Log.debug("By by");
        }
    }
}
