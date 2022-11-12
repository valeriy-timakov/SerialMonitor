package i.valerii_timakov.serial_monitor.exceptions;

public class PortException extends Exception {
    public PortException(String message) {
        super(message);
    }

    public PortException(String message, Throwable cause) {
        super(message, cause);
    }
}
