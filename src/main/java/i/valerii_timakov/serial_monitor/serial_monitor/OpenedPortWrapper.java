package i.valerii_timakov.serial_monitor.serial_monitor;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.serial_monitor.exceptions.PortException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Log4j2
public class OpenedPortWrapper implements AutoCloseable, Runnable {

    private static final int PORT_CLOSE_TIME_OUT_MS = 10000;

    private static final int MAX_READ_TRY_COUNT = 10;

    @Getter
    @NonNull
    private final SerialPort port;
    private final int bufferSize;
    @Setter
    private Charset charset = Charset.defaultCharset();
    private boolean stop = false;
    @Setter
    private Consumer<String> inputConsumer;
    @Setter
    private StringBuffer inputBuffer;
    @Setter
    private Runnable onClose;
    @Getter
    private boolean running = false;
    private Thread thread;

    public OpenedPortWrapper(SerialPort port) {
        this(port, 100);
    }

    public void init() throws PortException {
        if (!port.openPort()) {
            port.closePort();
            throw new PortException("Open port error!");
        }
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        running = true;
        try (this) {
            byte[] buffer = new byte[bufferSize];
            int readTryCount = 0;
            while (!stop && port.isOpen()) {
                try {
                    if (port.bytesAvailable() > 0) {
                        int numRead = port.readBytes(buffer, buffer.length);
                        String readed = new String(buffer, 0, numRead, charset);
                        if (inputConsumer != null) {
                            inputConsumer.accept(readed);
                        }
                        if (inputBuffer != null) {
                            inputBuffer.append(readed);
                        }
                        readTryCount = 0;
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        log.error("Waiting data interrupted! Returning...");
                        return;
                    }
                } catch (Throwable e) {
                    try {
                        readTryCount++;
                        if (readTryCount >= MAX_READ_TRY_COUNT) {
                            log.error("Maximum read tries count reached! Returning...");
                            return;
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        log.error("Waiting data interrupted! Returning...");
                        return;
                    }

                }
            }
        } catch (Exception e) {
            log.error("Port close exception!", e);
        } finally {
            running = false;
            if (onClose != null) {
                onClose.run();
            }
        }
    }

    @Override
    public void close() {
        port.closePort();
    }

    public void stop() {
        this.stop = true;
        if (thread != null) {
            try {
                thread.join(PORT_CLOSE_TIME_OUT_MS);
            } catch (InterruptedException e) {
                log.error("Waiting stop thread interrupted! Returning...");
            }
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    public void print(String value) throws IOException {
        write(value.getBytes(charset));
    }

    public void write(byte[] value) throws IOException {
        port.writeBytes(value, value.length);
    }


}
