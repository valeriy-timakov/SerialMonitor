package i.valerii_timakov.serial_monitor.serial_monitor;

import com.fazecast.jSerialComm.SerialPort;
import i.valerii_timakov.serial_monitor.serial_monitor.exceptions.PortException;

import java.io.*;
import java.nio.CharBuffer;
import java.util.function.Consumer;

public class OpenedPortWrapper implements AutoCloseable, Runnable {

    private static final int PORT_CLOSE_TIME_OUT_MS = 10000;

    private static final int MAX_READ_TRY_COUNT = 10;

    private final SerialPort port;
    private boolean stop = false;
    private InputStream is;
    private OutputStream os;
    private Consumer<String> inputConsumer;
    private StringBuffer inputBuffer;
    private Runnable onClose;
    private boolean running = false;
    private Thread thread;

    public OpenedPortWrapper(SerialPort port) {
        assert port != null;
        this.port = port;
    }

    public void init() throws PortException {
        if (!port.openPort()) {
            port.closePort();
            throw new PortException("Open port error!");
        }
        is = port.getInputStream();
        os = port.getOutputStream();

        thread = new Thread(this);
        thread.start();
    }

    public SerialPort getPort() {
        return port;
    }

    @Override
    public void run() {
        running = true;
        try (this) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            CharBuffer buffer = CharBuffer.allocate(100);
            int readTryCount = 0;
            while (!stop && port.isOpen()) {
                try {
                    while (true) {
                        if (reader.ready()) {
                            break;
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                    reader.read(buffer);
                    if (inputConsumer != null) {
                        inputConsumer.accept(buffer.toString());
                    }
                    if (inputBuffer != null) {
                        inputBuffer.append(buffer);
                    }
                    buffer.clear();
                    readTryCount = 0;
                } catch (IOException e) {
                    try {
                        readTryCount++;
                        if (readTryCount >= MAX_READ_TRY_COUNT) {
                            return;
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        return;
                    }

                }
/*
                byte[] readBuffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");*/
            }
        } catch (Exception closeException) {

        } finally {
            running = false;
            if (onClose != null) {
                onClose.run();
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {

            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {

            }
        }
        port.closePort();
    }

    public void stop() {
        this.stop = true;
        try {
            thread.join(PORT_CLOSE_TIME_OUT_MS);
        } catch (InterruptedException e) {

        }
        if (thread.isAlive()) {
            thread.interrupt();
        }
    }

    public void setInputConsumer(Consumer<String> inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    public void setInputBuffer(StringBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public void print(String value) throws IOException {
        os.write(value.getBytes());
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public boolean isRunning() {
        return running;
    }
}
