package i.valerii_timakov.serial_monitor.services;

public interface ByteArrayMessageConsumer {
    void consume(byte[] src, int count, boolean incoming);
}
