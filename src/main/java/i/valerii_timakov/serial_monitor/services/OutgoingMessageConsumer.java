package i.valerii_timakov.serial_monitor.services;

import java.io.IOException;

public interface OutgoingMessageConsumer {
    void consume(byte[] src, int length) throws IOException;
}
