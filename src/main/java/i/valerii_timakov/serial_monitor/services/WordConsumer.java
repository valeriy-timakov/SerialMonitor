package i.valerii_timakov.serial_monitor.services;

public interface WordConsumer {
    void consume(byte value);
    void consume(short value);
    void consume(int value);
    void consume(long value);
}
