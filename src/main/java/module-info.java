module i.valerii_timakov.serial_monitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires org.apache.logging.log4j;
    requires static lombok;
    requires org.apache.commons.lang3;


    exports i.valerii_timakov.serial_monitor;
    opens i.valerii_timakov.serial_monitor to javafx.fxml;
    exports i.valerii_timakov.serial_monitor.services;
    opens i.valerii_timakov.serial_monitor.services to javafx.fxml;
    exports i.valerii_timakov.serial_monitor.controllers;
    opens i.valerii_timakov.serial_monitor.controllers to javafx.fxml;
}
