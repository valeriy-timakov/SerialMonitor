package i.valerii_timakov.serial_monitor.services;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.exceptions.PortException;
import i.valerii_timakov.serial_monitor.utils.Log;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.Charset;

@RequiredArgsConstructor
@Log4j2
public class OpenedPortWrapper implements AutoCloseable, Runnable {

    private static final int PORT_CLOSE_TIME_OUT_MS = 10000;

    private static final int MAX_READ_TRY_COUNT = 10;

    @Getter
    @NonNull
    private final SerialPort port;
    private final int bufferSize;
    @Getter
    @Setter
    private Charset charset = Charset.defaultCharset();
    private boolean stop = false;
    @Setter
    private TextMessageConsumer textMessageConsumer;
    @Setter
    private ByteArrayMessageConsumer byteArrayMessageConsumer;
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
                        if (byteArrayMessageConsumer != null) {
                            Log.debug("Calling byteArrayInputConsumer");
                            byteArrayMessageConsumer.consume(buffer, numRead, true);
                            Log.debug("byteArrayInputConsumer finished");
                        }
                        String readed = new String(buffer, 0, numRead, charset);
                        Log.debug("OpenedPortWrapper: %1$d bytes readed \"%2$s\"", numRead, readed);
                        if (textMessageConsumer != null) {
                            Log.debug("Calling stringInputConsumer");
                            textMessageConsumer.consume(readed, true);
                            Log.debug("stringInputConsumer finished");
                        }
                        readTryCount = 0;
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.error("Waiting data interrupted! Returning...");
                        return;
                    }
                } catch (Throwable e) {
                    Log.error("Error reading data from port!", e);
                    try {
                        readTryCount++;
                        if (readTryCount >= MAX_READ_TRY_COUNT) {
                            Log.error("Maximum read tries count reached! Returning...");
                            return;
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Log.error("Waiting data interrupted! Returning...");
                        return;
                    }

                }
            }
        } catch (Exception e) {
            Log.error("Port close exception!", e);
        } finally {
            Log.debug("exiting OpenedPortWrapper");
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
        Log.debug("stop OpenedPortWrapper entered");
        this.stop = true;
        if (thread != null) {
            try {
                thread.join(PORT_CLOSE_TIME_OUT_MS);
            } catch (InterruptedException e) {
                Log.error("Waiting stop thread interrupted! Returning...");
            }
            if (thread.isAlive()) {
                Log.debug("interrupting OpenedPortWrapper");
                thread.interrupt();
            }
        }
    }

    public void print(String value) throws IOException {
        Log.debug("sending string data: " + value);
        byte[] bytes = value.getBytes(charset);
        write(bytes, bytes.length);
    }

    public void write(byte[] value, int length) throws IOException {
        Log.debug("sending byte array data: " + value);
        try {
            port.writeBytes(value, length);
        } catch (Throwable t) {
            throw new IOException("Error sending data to port!", t);
        }
    }

}
