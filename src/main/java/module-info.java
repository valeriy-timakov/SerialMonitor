module i.valerii_timakov.serial_monitor.serial_monitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires javatuples;


    opens i.valerii_timakov.serial_monitor.serial_monitor to javafx.fxml;
    exports i.valerii_timakov.serial_monitor.serial_monitor;
}
