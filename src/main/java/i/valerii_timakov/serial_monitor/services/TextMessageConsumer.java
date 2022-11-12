package i.valerii_timakov.serial_monitor.services;

public interface TextMessageConsumer {
    void consume(String message, boolean incoming);
}
