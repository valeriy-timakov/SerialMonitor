package i.valerii_timakov.serial_monitor.services;

import java.io.IOException;

public interface UnidirectedMessageConsumer {
    void consume(String message) throws IOException;
    void consume(byte[] src, int length) throws IOException;
}
